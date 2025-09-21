package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusNBTConstants;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.*;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTankBuilder;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.common.content.boiler.BoilerMultiblockData;

import java.util.List;

public class TileEntityCompactThermoelectricBoiler extends TileEntityCompactPlusBase {
    //public static final Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 50;
    private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100_000;
    private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    private static final double COOLANT_COOLING_EFFICIENCY = 0.4;

    //ボイラーの底面積。18 * 18
    public static final int BOILER_BASE_AREA = 324;
    //ボイラーの高さ
    public static final int BOILER_HEIGHT = 18;

    public static final int MAX_DISPERSERS_Y = BOILER_HEIGHT - 1;
    public static final int MIN_DISPERSERS_Y = 2;

    @ContainerSync
    public IGasTank superheatedCoolantTank;

    @ContainerSync
    public IGasTank cooledCoolantTank;

    @ContainerSync
    public VariableCapacityFluidTank waterTank;

    @ContainerSync
    public IGasTank steamTank;

    @ContainerSync
    public VariableHeatCapacitor heatCapacitor;

    private final double biomeAmbientTemp;

    @ContainerSync
    public double lastEnvironmentLoss;

    @ContainerSync
    public int lastBoilRate;

    @ContainerSync
    public int lastMaxBoil;

    @ContainerSync
    public int superHeatingElements;

    @ContainerSync
    public int dispersersY;

    @ContainerSync
    public int maxSuperHeatingElements;

    @ContainerSync(setter = "setWaterVolume")
    private int waterVolume;

    @ContainerSync(setter = "setSteamVolume")
    private int steamVolume;

    private int waterTankCapacity;
    private long superheatedCoolantCapacity, steamTankCapacity, cooledCoolantCapacity;

    public float prevWaterScale;
    public float prevSteamScale;


    public TileEntityCompactThermoelectricBoiler(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_THERMOELECTRIC_BOILER, pos, state);

        setDispersersY(16);
        setSuperHeatingElements(256);
        setWaterVolume((BOILER_BASE_AREA * (dispersersY - 1)  - superHeatingElements));
        setSteamVolume(BOILER_BASE_AREA * (BOILER_HEIGHT - dispersersY));

        biomeAmbientTemp = HeatAPI.getAmbientTemp(this.getLevel(), this.getTilePos());

        configComponent = new TileComponentConfig(this,TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.HEAT);

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if(gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT,new ChemicalSlotInfo.GasSlotInfo(true,false,superheatedCoolantTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_1,new ChemicalSlotInfo.GasSlotInfo(false,true,cooledCoolantTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_2,new ChemicalSlotInfo.GasSlotInfo(false,true,steamTank));

            gasConfig.setDataType(DataType.INPUT, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.OUTPUT_1, RelativeSide.BOTTOM);
            gasConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);

            gasConfig.setEjecting(true);
        }

        ConfigInfo fluidConfig =configComponent.getConfig(TransmissionType.FLUID);
        if(fluidConfig != null) {
            fluidConfig.addSlotInfo(DataType.INPUT,new FluidSlotInfo(true,false,waterTank));
            fluidConfig.setDataType(DataType.INPUT,RelativeSide.values());

            fluidConfig.setCanEject(false);
        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if(heatConfig != null) {
            heatConfig.addSlotInfo(DataType.INPUT,new HeatSlotInfo(true,false, List.of(heatCapacitor)));
            heatConfig.setDataType(DataType.INPUT,RelativeSide.values());

            heatConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this,() -> Long.MAX_VALUE,() -> Integer.MAX_VALUE, () -> FloatingLong.MAX_VALUE);
        ejectorComponent.setOutputData(configComponent,TransmissionType.GAS,TransmissionType.FLUID);
        ejectorComponent.setCanEject(type ->  MekanismUtils.canFunction(this));

    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        //hotMap.put(inventoryID, getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            superheatedCoolantTank.getStack().ifAttributePresent(HeatedCoolant.class, coolantType -> {
                long toCool = 471449600;// = Math.round(TileEntityCompactThermoelectricBoiler.COOLANT_COOLING_EFFICIENCY * superheatedCoolantTank.getStored());

                double nowTemp = heatCapacitor.getTemperature();

                toCool = MathUtils.clampToLong(toCool * (1 - nowTemp/ HeatUtils.HEATED_COOLANT_TEMP));
                GasStack cooledCoolant = coolantType.getCooledGas().getStack(toCool);
                toCool = Math.min(toCool, toCool - cooledCoolantTank.insert(cooledCoolant, Action.EXECUTE, AutomationType.INTERNAL).getAmount());

                cooledCoolantTank.onContentsChanged();

                if (toCool > 0) {
                    double heatEnergy = toCool * coolantType.getThermalEnthalpy();
                    heatCapacitor.handleHeat(heatEnergy);
                    superheatedCoolantTank.shrinkStack(toCool, Action.EXECUTE);
                }
            });
        }
        // handle water heat transfer
        if (getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !waterTank.isEmpty()) {
            setActive(true);
            double heatAvailable = getHeatAvailable();
            lastMaxBoil = (int) Math.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());

            int amountToBoil = Math.min(lastMaxBoil, waterTank.getFluidAmount());
            amountToBoil = Math.min(amountToBoil, MathUtils.clampToInt(steamTank.getNeeded()));
            if (!waterTank.isEmpty()) {
                waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
            }
            if (steamTank.isEmpty()) {
                steamTank.setStack(MekanismGases.STEAM.getStack(amountToBoil));
            } else {
                steamTank.growStack(amountToBoil, Action.EXECUTE);
            }

            heatCapacitor.handleHeat(-amountToBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
            lastBoilRate = amountToBoil;
        } else {
            setActive(false);
            lastBoilRate = 0;
            lastMaxBoil = 0;
        }
        float waterScale = MekanismUtils.getScale(prevWaterScale, waterTank);
        if (waterScale != prevWaterScale) {
            //needsPacket = true;
            prevWaterScale = waterScale;
        }
        float steamScale = MekanismUtils.getScale(prevSteamScale, steamTank);
        if (steamScale != prevSteamScale) {
            //needsPacket = true;
            prevSteamScale = steamScale;
        }
        //return needsPacket;
    }

    private double getHeatAvailable() {
        double heatAvailable = (heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP) * (heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get());
        return Math.min(heatAvailable, MekanismConfig.general.superheatingHeatTransfer.get() * superHeatingElements);
    }

    public void setWaterVolume(int volume) {
        if (waterVolume != volume) {
            waterVolume = volume;
            waterTankCapacity = volume * MekanismConfig.general.boilerWaterPerTank.get();
            superheatedCoolantCapacity = volume * MekanismConfig.general.boilerHeatedCoolantPerTank.get();

            waterTank.setStackSize(Math.min(waterTank.getFluidAmount(),waterTank.getCapacity()),Action.EXECUTE);
            superheatedCoolantTank.setStackSize(Math.min(superheatedCoolantTank.getStored(),superheatedCoolantTank.getCapacity()),Action.EXECUTE);
        }
    }

    public void setSteamVolume(int volume) {
        if (steamVolume != volume) {
            steamVolume = volume;
            steamTankCapacity = volume * MekanismConfig.general.boilerSteamPerTank.get();
            cooledCoolantCapacity = volume * MekanismConfig.general.boilerCooledCoolantPerTank.get();

            steamTank.setStackSize(Math.min(steamTank.getStored(),steamTank.getCapacity()),Action.EXECUTE);
            cooledCoolantTank.setStackSize(Math.min(cooledCoolantTank.getStored(),cooledCoolantTank.getCapacity()),Action.EXECUTE);
        }
    }

    @Override
    public double simulateEnvironment() {
        double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + (CASING_INVERSE_INSULATION_COEFFICIENT + CASING_INVERSE_CONDUCTION_COEFFICIENT);
        double tempToTransfer = (heatCapacitor.getTemperature() - biomeAmbientTemp) / invConduction;
        heatCapacitor.handleHeat(-tempToTransfer * heatCapacitor.getHeatCapacity());
        return Math.max(tempToTransfer, 0);
    }

    public long getBoilCapacity() {
        double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * superHeatingElements / HeatUtils.getWaterThermalEnthalpy();
        return MathUtils.clampToLong(boilCapacity * HeatUtils.getSteamEnergyEfficiency());
    }

    public void setSuperHeatingElements(int count) {
        if(count > maxSuperHeatingElements || superHeatingElements > maxSuperHeatingElements ) {
            superHeatingElements = maxSuperHeatingElements;
        } else {
            superHeatingElements = count;
        }

        setVolumes();
    }
    public void setDispersersY(int y) {
        dispersersY = y;
        maxSuperHeatingElements = 256 * (dispersersY - 2);

        setVolumes();
    }

    public void setVolumes() {
        int waterVolume = (BOILER_BASE_AREA * (dispersersY - 1)  - superHeatingElements);
        int steamVolume = BOILER_BASE_AREA * (BOILER_HEIGHT - dispersersY);

        setWaterVolume(waterVolume);
        setSteamVolume(steamVolume);


    }

    public int getDispersersY() {
        return dispersersY;
    }

    public int getSuperHeatingElements() {
        return superHeatingElements;
    }

    //以下保存系統
    @Override
    public void load(@NotNull CompoundTag tag) {

        super.load(tag);
        NBTUtils.setIntIfPresent(tag,CompactPlusNBTConstants.DISPERSERS_Y,this::setDispersersY);
        NBTUtils.setIntIfPresent(tag,CompactPlusNBTConstants.SUPER_HEATING_ELEMENTS,this::setSuperHeatingElements);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevWaterScale = scale);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevSteamScale = scale);

        //readValves(tag);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(CompactPlusNBTConstants.DISPERSERS_Y,dispersersY);
        tag.putInt(CompactPlusNBTConstants.SUPER_HEATING_ELEMENTS, superHeatingElements);

        tag.putFloat(NBTConstants.SCALE, prevWaterScale);
        tag.putFloat(NBTConstants.SCALE_ALT, prevSteamScale);
        //writeValves(tag);
    }

    //以下パケット系統
    public void setSuperHeatingElementsFromPacket(int count) {
        setSuperHeatingElements(Mth.clamp(count,0,maxSuperHeatingElements));
        markForSave();
    }
    public void setDispersersYFromPacket(int y) {
        setDispersersY(Mth.clamp(y,MIN_DISPERSERS_Y,MAX_DISPERSERS_Y));
        markForSave();
    }


    //以下初期化系統
    @Override
    public @Nullable IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection,this::getConfig);
        builder.addTank(superheatedCoolantTank = VariableCapacityChemicalTankBuilder.GAS.create(
                () -> superheatedCoolantCapacity,
                ChemicalTankBuilder.GAS.notExternal,
                ChemicalTankBuilder.GAS.alwaysTrueBi,
                gas -> gas.has(HeatedCoolant.class),
                ChemicalAttributeValidator.ALWAYS_ALLOW,
                listener
        ));
        builder.addTank(cooledCoolantTank = VariableCapacityChemicalTankBuilder.GAS.output(
                () -> cooledCoolantCapacity,
                gas -> gas.has(CooledCoolant.class),
                listener
        ));
        builder.addTank(steamTank = VariableCapacityChemicalTankBuilder.GAS.output(
                () -> steamTankCapacity,
                gas -> gas == MekanismGases.STEAM.getChemical(),
                listener
        ));
        return builder.build();
    }

    @NotNull
    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener){
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection,this::getConfig);
        builder.addTank(waterTank = VariableCapacityFluidTank.input(() -> waterTankCapacity, fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()), listener));
        return builder.build();
    }

    public IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature){
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = VariableHeatCapacitor.create(CASING_HEAT_CAPACITY, () -> CASING_INVERSE_CONDUCTION_COEFFICIENT, () -> CASING_INVERSE_INSULATION_COEFFICIENT, () -> biomeAmbientTemp, listener));
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * 1736, true);

        return builder.build();
    }
}
