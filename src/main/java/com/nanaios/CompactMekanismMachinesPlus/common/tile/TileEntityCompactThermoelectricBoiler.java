package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusNBTConstants;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.api.*;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.heat.HeatAPI;
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
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.common.content.boiler.BoilerValidator;

public class TileEntityCompactThermoelectricBoiler extends TileEntityConfigurableMachine {
    //public static final Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 50;
    private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100_000;
    private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    private static final double COOLANT_COOLING_EFFICIENCY = 0.4;

    //ボイラーの底面積。18 * 18
    public static final int BOILER_BASE_AREA = 324;
    //
    public static final int BOILER_HEIGHT = 18;

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

    private double biomeAmbientTemp;

    @ContainerSync
    public double lastEnvironmentLoss;

    @ContainerSync
    public int lastBoilRate;

    @ContainerSync
    public int lastMaxBoil;

    @ContainerSync
    public int superheatingElements = 256;

    @ContainerSync
    public int dispersersY = 16;

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

        setWaterVolume((BOILER_BASE_AREA * (dispersersY - 1)  - superheatingElements));
        setSteamVolume(BOILER_BASE_AREA * (BOILER_HEIGHT - dispersersY));

        biomeAmbientTemp = HeatAPI.getAmbientTemp(this.getLevel(), this.getTilePos());

        configComponent = new TileComponentConfig(this,TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.HEAT);

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if(gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT,new ChemicalSlotInfo.GasSlotInfo(true,false,superheatedCoolantTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_1,new ChemicalSlotInfo.GasSlotInfo(false,true,cooledCoolantTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_2,new ChemicalSlotInfo.GasSlotInfo(false,true,steamTank));

            gasConfig.setDataType(DataType.INPUT, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.OUTPUT_1, RelativeSide.TOP);
            gasConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);

            gasConfig.setEjecting(true);
        }

        ConfigInfo fluidConfig =configComponent.getConfig(TransmissionType.FLUID);
        if(fluidConfig != null) {
            fluidConfig.addSlotInfo(DataType.INPUT,new FluidSlotInfo(true,false,waterTank));
            fluidConfig.setDataType(DataType.INPUT,RelativeSide.BACK);

            fluidConfig.setCanEject(false);
        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if(heatConfig != null) {

        }

        ejectorComponent = new TileComponentEjector(this,() -> Long.MAX_VALUE,() -> Integer.MAX_VALUE, () -> FloatingLong.MAX_VALUE);
        ejectorComponent.setOutputData(configComponent,TransmissionType.GAS,TransmissionType.FLUID);
        ejectorComponent.setCanEject(type ->  MekanismUtils.canFunction(this));
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        //hotMap.put(inventoryID, getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            superheatedCoolantTank.getStack().ifAttributePresent(HeatedCoolant.class, coolantType -> {
                long toCool = Math.round(TileEntityCompactThermoelectricBoiler.COOLANT_COOLING_EFFICIENCY * superheatedCoolantTank.getStored());
                toCool = MathUtils.clampToLong(toCool * (1 - heatCapacitor.getTemperature() / HeatUtils.HEATED_COOLANT_TEMP));
                GasStack cooledCoolant = coolantType.getCooledGas().getStack(toCool);
                toCool = Math.min(toCool, toCool - cooledCoolantTank.insert(cooledCoolant, Action.EXECUTE, AutomationType.INTERNAL).getAmount());
                if (toCool > 0) {
                    double heatEnergy = toCool * coolantType.getThermalEnthalpy();
                    heatCapacitor.handleHeat(heatEnergy);
                    superheatedCoolantTank.shrinkStack(toCool, Action.EXECUTE);
                }
            });
        }
        // handle water heat transfer
        if (getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !waterTank.isEmpty()) {
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
            lastBoilRate = 0;
            lastMaxBoil = 0;
        }
        float waterScale = MekanismUtils.getScale(prevWaterScale, waterTank);
        if (waterScale != prevWaterScale) {
            prevWaterScale = waterScale;
        }
        float steamScale = MekanismUtils.getScale(prevSteamScale, steamTank);
        if (steamScale != prevSteamScale) {
            prevSteamScale = steamScale;
        }
    }

    private double getHeatAvailable() {
        double heatAvailable = (heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP) * (heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get());
        return Math.min(heatAvailable, MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements);
    }

    public void setWaterVolume(int volume) {
        if (waterVolume != volume) {
            waterVolume = volume;
            waterTankCapacity = volume * MekanismConfig.general.boilerWaterPerTank.get();
            superheatedCoolantCapacity = volume * MekanismConfig.general.boilerHeatedCoolantPerTank.get();
        }
    }

    public void setSteamVolume(int volume) {
        if (steamVolume != volume) {
            steamVolume = volume;
            steamTankCapacity = volume * MekanismConfig.general.boilerSteamPerTank.get();
            cooledCoolantCapacity = volume * MekanismConfig.general.boilerCooledCoolantPerTank.get();
        }
    }

    public long getBoilCapacity() {
        double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements / HeatUtils.getWaterThermalEnthalpy();
        return MathUtils.clampToLong(boilCapacity * HeatUtils.getSteamEnergyEfficiency());
    }

    public void setSuperHeatingElements(int count) {
        this.superheatingElements = count;

        int waterVolume = (BOILER_BASE_AREA * (dispersersY - 1)  - superheatingElements);
        int steamVolume = BOILER_BASE_AREA * (BOILER_HEIGHT - dispersersY);

        setWaterVolume(waterVolume);
        setSteamVolume(steamVolume);

    }
    public void setDispersersY(int y) {
        this.dispersersY = y;
    }

    //以下保存系統
    @Override
    public void load(@NotNull CompoundTag tag) {

        CompactMekanismMachinesPlus.LOGGER.info("loading of Boiler");

        super.load(tag);
        NBTUtils.setIntIfPresent(tag,CompactPlusNBTConstants.DISPERSERS_Y,this::setDispersersY);
        NBTUtils.setIntIfPresent(tag,CompactPlusNBTConstants.SUPER_HEATING_ELEMENTS,this::setSuperHeatingElements);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevWaterScale = scale);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevSteamScale = scale);

        //readValves(tag);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {

        CompactMekanismMachinesPlus.LOGGER.info("saving of Boiler");
        super.saveAdditional(tag);
        tag.putInt(CompactPlusNBTConstants.DISPERSERS_Y,dispersersY);
        tag.putInt(CompactPlusNBTConstants.SUPER_HEATING_ELEMENTS,superheatingElements);

        tag.putFloat(NBTConstants.SCALE, prevWaterScale);
        tag.putFloat(NBTConstants.SCALE_ALT, prevSteamScale);
        //writeValves(tag);
    }

    //以下パケット系統
    public void setSuperHeatingElementsFromPacket(int count) {
        setSuperHeatingElements(count);
        markForSave();
    }
    public void setDispersersYFromPacket(int y) {
        setDispersersY(y);
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
        return builder.build();
    }
}
