package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.slot.ReactorInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import org.jetbrains.annotations.Nullable;

public class TileEntityCompactFusionReactor extends TileEntityConfigurableMachine {

    public static final String HEAT_TAB = FusionReactorMultiblockData.HEAT_TAB;
    public static final String FUEL_TAB =  FusionReactorMultiblockData.FUEL_TAB;
    public static final String STATS_TAB = FusionReactorMultiblockData.STATS_TAB;

    @ContainerSync(tags = HEAT_TAB)
    public IExtendedFluidTank waterTank;

    @ContainerSync(tags = HEAT_TAB)
    public IGasTank steamTank;

    @ContainerSync
    private boolean burning = false;

    @ContainerSync
    public IEnergyContainer energyContainer;
    public IHeatCapacitor heatCapacitor;

    @ContainerSync
    private double lastCaseTemperature;

    @ContainerSync(tags = HEAT_TAB)
    private double lastPlasmaTemperature;

    private double biomeAmbientTemp;

    @ContainerSync
    public double lastTransferLoss;

    @ContainerSync
    public double lastEnvironmentLoss;

    @ContainerSync(tags = FUEL_TAB)
    public IGasTank deuteriumTank;

    @ContainerSync(tags = FUEL_TAB)
    public IGasTank tritiumTank;

    @ContainerSync(tags = FUEL_TAB)
    public IGasTank fuelTank;

    private int maxWater;
    private long maxSteam;

    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB}, getter = "getInjectionRate", setter = "setInjectionRate")
    private int injectionRate = 2;
    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB})
    private long lastBurned;

    final ReactorInventorySlot reactorSlot;

    public double plasmaTemperature;

    public TileEntityCompactFusionReactor(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_FUSION_REACTOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY);

        biomeAmbientTemp = HeatAPI.getAmbientTemp(this.getLevel(),pos);
        lastPlasmaTemperature = biomeAmbientTemp;
        lastCaseTemperature = biomeAmbientTemp;
        plasmaTemperature = biomeAmbientTemp;

        gasTanks.add(fuelTank = MultiblockChemicalTankBuilder.GAS.input(this, MekanismGeneratorsConfig.generators.fusionFuelCapacity,
                GeneratorTags.Gases.FUSION_FUEL_LOOKUP::contains, createSaveAndComparator()));
        gasTanks.add(steamTank = MultiblockChemicalTankBuilder.GAS.output(this, this::getMaxSteam, gas -> gas == MekanismGases.STEAM.getChemical(), this));
        fluidTanks.add(waterTank = VariableCapacityFluidTank.input(this, this::getMaxWater, fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()), this));
        heatCapacitors.add(heatCapacitor = VariableHeatCapacitor.create(caseHeatCapacity, FusionReactorMultiblockData::getInverseConductionCoefficient,
                () -> inverseInsulation, () -> biomeAmbientTemp, this));
        inventorySlots.add(reactorSlot = ReactorInventorySlot.at(stack -> stack.getItem() instanceof ItemHohlraum, this, 80, 39));

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,()-> FloatingLong.create(Long.MAX_VALUE));
        ejectorComponent.setOutputData(configComponent, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY)
                .setCanEject(type -> MekanismUtils.canFunction(this));
    }

    public FloatingLong getPassiveGeneration(boolean active, boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(active);
        return FloatingLong.create(MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get() *
                MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * temperature);
    }

    public double getLastCaseTemp() {
        return lastCaseTemperature;
    }

    public double getMaxCasingTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        long injectionRate = Math.max(this.injectionRate, lastBurned);
        return MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(injectionRate)
                .divide(k + MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get()).doubleValue();
    }

    public int getMinInjectionRate(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0.0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double aMin = 2.0E7 * (k + caseAirConductivity) / (((FloatingLong)MekanismGeneratorsConfig.generators.energyPerFusionFuel.get()).doubleValue() * 1.0 * (0.2 + k + caseAirConductivity) - 0.2 * (k + caseAirConductivity));
        return (int)(2.0 * Math.ceil(aMin / 2.0));
    }

    public double getMaxPlasmaTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0.0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        long injectionRate = Math.max((long)this.injectionRate, this.lastBurned);
        return (double)injectionRate * ((FloatingLong)MekanismGeneratorsConfig.generators.energyPerFusionFuel.get()).doubleValue() / 0.2 * (0.2 + k + caseAirConductivity) / (k + caseAirConductivity);
    }

    public double getIgnitionTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0.0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double energyPerFusionFuel = ((FloatingLong)MekanismGeneratorsConfig.generators.energyPerFusionFuel.get()).doubleValue();
        return 1.0E8 * energyPerFusionFuel * 1.0 * (0.2 + k + caseAirConductivity) / (energyPerFusionFuel * 1.0 * (0.2 + k + caseAirConductivity) - 0.2 * (k + caseAirConductivity));
    }

    public long getSteamPerTick(boolean current) {
        double temperature = current ? this.getLastCaseTemp() : this.getMaxCasingTemperature(true);
        return MathUtils.clampToLong(HeatUtils.getSteamEnergyEfficiency() * MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() * temperature / HeatUtils.getWaterThermalEnthalpy());
    }

    public void setInjectionRate(int rate) {
        if (injectionRate != rate) {
            injectionRate = rate;
            maxWater = injectionRate * MekanismGeneratorsConfig.generators.fusionWaterPerInjection.get();
            maxSteam = injectionRate * MekanismGeneratorsConfig.generators.fusionSteamPerInjection.get();
            if (getLevel() != null && !isRemote()) {
                if (!waterTank.isEmpty()) {
                    waterTank.setStackSize(Math.min(waterTank.getFluidAmount(), waterTank.getCapacity()), Action.EXECUTE);
                }
                if (!steamTank.isEmpty()) {
                    steamTank.setStackSize(Math.min(steamTank.getStored(), steamTank.getCapacity()), Action.EXECUTE);
                }
            }
            //markDirty();
        }
    }

    public int getInjectionRate() {
        return injectionRate;
    }

    public double getLastPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    public double getCaseTemp() {
        return heatCapacitor.getTemperature();
    }

    public int getMaxWater() {
        return maxWater;
    }

    public long getMaxSteam() {
        return maxSteam;
    }

    public boolean isBurning() {return burning;}

    @Override
    public @Nullable IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection,this::getConfig);
        builder.addTank(deuteriumTank = ChemicalTankBuilder.GAS.input(MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(), gas -> gas == GeneratorsGases.DEUTERIUM.getChemical(),listener));
        builder.addTank(tritiumTank = ChemicalTankBuilder.GAS.input(MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(), gas -> gas == GeneratorsGases.TRITIUM.getChemical(),listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.output(MekanismGeneratorsConfig.generators.fusionEnergyCapacity.get(),listener));
        return builder.build();
    }

    public  IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}
