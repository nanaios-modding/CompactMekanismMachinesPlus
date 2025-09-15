package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusNBTConstants;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.*;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
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
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismChemicals;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static mekanism.common.content.boiler.BoilerMultiblockData.IS_COOLED_COOLANT;
import static mekanism.common.content.boiler.BoilerMultiblockData.IS_HEATED_COOLANT;

public class TileEntityCompactThermoelectricBoiler extends TileEntityConfigurableMachine {
    public static final double CASING_HEAT_CAPACITY = 50;
    private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100_000;
    private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    public static final int BOILER_BASE_AREA = 324;
    public static final int BOILER_HEIGHT = 18;

    public static final int MAX_DISPERSERS_Y = BOILER_HEIGHT - 1;
    public static final int MIN_DISPERSERS_Y = 2;

    public IChemicalTank superheatedCoolantTank;
    public IChemicalTank cooledCoolantTank;
    public VariableCapacityFluidTank waterTank;
    public IChemicalTank steamTank;
    public VariableHeatCapacitor heatCapacitor;

    private final double biomeAmbientTemp;
    public double lastEnvironmentLoss;

    public int lastBoilRate, lastMaxBoil;

    public int superHeatingElements,dispersersY,maxSuperHeatingElements;

    private int waterVolume, steamVolume;

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

        biomeAmbientTemp = HeatAPI.getAmbientTemp(this.getLevel(), this.getBlockPos());

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if(gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT,new ChemicalSlotInfo(true,false,superheatedCoolantTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_1,new ChemicalSlotInfo(false,true,cooledCoolantTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_2,new ChemicalSlotInfo(false,true,steamTank));

            gasConfig.setDataType(DataType.INPUT, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.OUTPUT_1, RelativeSide.BOTTOM);
            gasConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);

            gasConfig.setEjecting(true);
        }

        ConfigInfo fluidConfig =configComponent.getConfig(TransmissionType.FLUID);
        if(fluidConfig != null) {
            fluidConfig.addSlotInfo(DataType.INPUT,new FluidSlotInfo(true,false,waterTank));
            for (RelativeSide side: RelativeSide.values()) {
                fluidConfig.setDataType(DataType.INPUT,side);
            }

            fluidConfig.setCanEject(false);
        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if(heatConfig != null) {
            heatConfig.addSlotInfo(DataType.INPUT,new HeatSlotInfo(true,false, List.of(heatCapacitor)));
            for (RelativeSide side: RelativeSide.values()) {
                heatConfig.setDataType(DataType.INPUT,side);
            }

            heatConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this,() -> Long.MAX_VALUE,() -> Integer.MAX_VALUE, () ->Long.MAX_VALUE);
        ejectorComponent.setOutputData(configComponent,TransmissionType.CHEMICAL,TransmissionType.FLUID);
        ejectorComponent.setCanTankEject(tank -> (tank == steamTank) || (tank == cooledCoolantTank));
    }

    @Nullable
    @SuppressWarnings("removal")
    private HeatedCoolant getHeatedCoolant() {
        ChemicalStack stack = superheatedCoolantTank.getStack();
        if (stack.isEmpty()) {
            return null;
        }
        HeatedCoolant coolant = stack.getData(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant());
        if (coolant == null) {
            ChemicalAttributes.HeatedCoolant legacyCoolant = stack.getLegacy(ChemicalAttributes.HeatedCoolant.class);
            if (legacyCoolant != null) {
                return legacyCoolant.asModern();
            }
        }
        return coolant;
    }

    @Override
    public boolean onUpdateServer() {
        boolean needsPacket = super.onUpdateServer();
        //hotMap.put(inventoryID, getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            HeatedCoolant coolantType = getHeatedCoolant();
            if (coolantType != null) {
                double portionToCool = coolantType.conductivity() * superheatedCoolantTank.getStored();
                long toCool = Math.round(portionToCool * (1 - heatCapacitor.getTemperature() / coolantType.temperature()));
                ChemicalStack cooledCoolant = coolantType.cool(toCool);
                long amountCooled = toCool - cooledCoolantTank.insert(cooledCoolant, Action.EXECUTE, AutomationType.INTERNAL).getAmount();
                if (amountCooled > 0) {
                    double heatEnergy = amountCooled * coolantType.thermalEnthalpy();
                    heatCapacitor.handleHeat(heatEnergy);
                    superheatedCoolantTank.shrinkStack(amountCooled, Action.EXECUTE);
                }
            }
        }
        // handle water heat transfer
        if (getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !waterTank.isEmpty()) {
            double heatAvailable = getHeatAvailable();
            lastMaxBoil = Mth.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());

            int amountToBoil = Math.min(lastMaxBoil, waterTank.getFluidAmount());
            amountToBoil = Math.min(amountToBoil, MathUtils.clampToInt(steamTank.getNeeded()));
            if (!waterTank.isEmpty()) {
                waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
            }
            if (steamTank.isEmpty()) {
                steamTank.setStack(MekanismChemicals.STEAM.asStack(amountToBoil));
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
        if (MekanismUtils.scaleChanged(waterScale, prevWaterScale)) {
            needsPacket = true;
            prevWaterScale = waterScale;
        }
        float steamScale = MekanismUtils.getScale(prevSteamScale, steamTank);
        if (MekanismUtils.scaleChanged(steamScale, prevSteamScale)) {
            needsPacket = true;
            prevSteamScale = steamScale;
        }
        return needsPacket;
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

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);

        NBTUtils.setIntIfPresent(nbt,CompactPlusNBTConstants.DISPERSERS_Y,this::setDispersersY);
        NBTUtils.setIntIfPresent(nbt,CompactPlusNBTConstants.SUPER_HEATING_ELEMENTS,this::setSuperHeatingElements);
        NBTUtils.setFloatIfPresent(nbt, SerializationConstants.SCALE, scale -> prevWaterScale = scale);
        NBTUtils.setFloatIfPresent(nbt, SerializationConstants.SCALE_ALT, scale -> prevSteamScale = scale);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putInt(CompactPlusNBTConstants.DISPERSERS_Y,dispersersY);
        nbtTags.putInt(CompactPlusNBTConstants.SUPER_HEATING_ELEMENTS, superHeatingElements);

        nbtTags.putFloat(SerializationConstants.SCALE, prevWaterScale);
        nbtTags.putFloat(SerializationConstants.SCALE_ALT, prevSteamScale);
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
    public @Nullable IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);

        builder.addTank(superheatedCoolantTank = VariableCapacityChemicalTank.create(
                () -> superheatedCoolantCapacity,
                ConstantPredicates.notExternal(),
                ConstantPredicates.alwaysTrueBi(),
                IS_HEATED_COOLANT,
                listener
        ));
        builder.addTank(cooledCoolantTank = VariableCapacityChemicalTank.output(
                () -> cooledCoolantCapacity,
                IS_COOLED_COOLANT,
                listener
        ));
        builder.addTank(steamTank = VariableCapacityChemicalTank.output(
                () -> steamTankCapacity,
                chemical -> chemical.is(MekanismChemicals.STEAM),
                listener
        ));

        return builder.build();
    }

    @NotNull
    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener){
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        builder.addTank(waterTank = VariableCapacityFluidTank.input(() -> waterTankCapacity, fluid -> fluid.is(FluidTags.WATER), listener));
        return builder.build();
    }

    public IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature){
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = VariableHeatCapacitor.create(CASING_HEAT_CAPACITY, () -> CASING_INVERSE_CONDUCTION_COEFFICIENT, () -> CASING_INVERSE_INSULATION_COEFFICIENT, () -> biomeAmbientTemp, listener));
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * 1736, true);

        return builder.build();
    }
}
