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
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.slot.ReactorInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TileEntityCompactFusionReactor extends TileEntityConfigurableMachine {

    public static final String HEAT_TAB = FusionReactorMultiblockData.HEAT_TAB;
    public static final String FUEL_TAB =  FusionReactorMultiblockData.FUEL_TAB;
    public static final String STATS_TAB = FusionReactorMultiblockData.STATS_TAB;

    public static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static final double burnTemperature = 100_000_000;
    private static final double burnRatio = 1;
    //Thermal characteristics
    private static final double plasmaHeatCapacity = 100;
    private static final double caseHeatCapacity = 1;
    private static final double inverseInsulation = 100_000;
    //Heat transfer metrics
    private static final double plasmaCaseConductivity = 0.2;

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

    private ReactorInventorySlot reactorSlot;

    public double plasmaTemperature;

    public TileEntityCompactFusionReactor(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_FUSION_REACTOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY);

        biomeAmbientTemp = HeatAPI.getAmbientTemp(this.getLevel(),pos);
        lastPlasmaTemperature = biomeAmbientTemp;
        lastCaseTemperature = biomeAmbientTemp;
        plasmaTemperature = biomeAmbientTemp;

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,()-> FloatingLong.create(Long.MAX_VALUE));
        ejectorComponent.setOutputData(configComponent, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY)
                .setCanEject(type -> MekanismUtils.canFunction(this));
    }

    //以下融合炉メソッド

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

    private static double getInverseConductionCoefficient() {
        return 1 / MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
    }

    public void setInjectionRateFromPacket(int rate) {
        this.setInjectionRate(Mth.clamp(rate - (rate % 2), 0, FusionReactorMultiblockData.MAX_INJECTION));
        markForSave();
    }

    //以下初期化系メソッド

    @Override
    public @Nullable IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection,this::getConfig);
        builder.addTank(deuteriumTank = ChemicalTankBuilder.GAS.input(MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(), gas -> gas == GeneratorsGases.DEUTERIUM.getChemical(),listener));
        builder.addTank(tritiumTank = ChemicalTankBuilder.GAS.input(MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(), gas -> gas == GeneratorsGases.TRITIUM.getChemical(),listener));
        builder.addTank(fuelTank = ChemicalTankBuilder.GAS.input(MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(), gas -> gas == GeneratorsGases.FUSION_FUEL.getChemical(),createSaveAndComparator()));
        builder.addTank(steamTank = ChemicalTankBuilder.GAS.output(maxSteam,listener));
        return builder.build();
    }

    @NotNull
    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener){
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection,this::getConfig);
        builder.addTank(waterTank = VariableCapacityFluidTank.input( this::getMaxWater, fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()), this));
        return builder.build();
    }

    public IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature){
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = VariableHeatCapacitor.create(caseHeatCapacity, TileEntityCompactFusionReactor::getInverseConductionCoefficient, () -> inverseInsulation, () -> biomeAmbientTemp, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.output(MekanismGeneratorsConfig.generators.fusionEnergyCapacity.get(),listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(reactorSlot = ReactorInventorySlot.at(stack -> stack.getItem() instanceof ItemHohlraum, listener, 80, 39));
        return builder.build();
    }

    //その他

    protected IContentsListener createSaveAndComparator() {
        return this.createSaveAndComparator(this);
    }

    protected IContentsListener createSaveAndComparator(IContentsListener contentsListener) {
        return () -> {
            contentsListener.onContentsChanged();
            if (!this.isRemote()) {
                this.markDirtyComparator();
            }

        };
    }

    public  IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}
