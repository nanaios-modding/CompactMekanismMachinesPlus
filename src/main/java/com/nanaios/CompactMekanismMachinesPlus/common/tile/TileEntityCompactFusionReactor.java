package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;

public class TileEntityCompactFusionReactor extends TileEntityConfigurableMachine {

    public static final String HEAT_TAB = FusionReactorMultiblockData.HEAT_TAB;
    public static final String FUEL_TAB =  FusionReactorMultiblockData.FUEL_TAB;
    public static final String STATS_TAB = FusionReactorMultiblockData.STATS_TAB;

    @ContainerSync
    private boolean burning = false;

    @ContainerSync
    public IEnergyContainer energyContainer;
    public IHeatCapacitor heatCapacitor;

    @ContainerSync
    private double lastCaseTemperature;

    @ContainerSync
    public double lastTransferLoss;

    @ContainerSync
    public double lastEnvironmentLoss;

    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB}, getter = "getInjectionRate", setter = "setInjectionRate")
    private int injectionRate = 2;
    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB})
    private long lastBurned;

    public TileEntityCompactFusionReactor(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_FUSION_REACTOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY);

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

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.output(FloatingLong.create(4000000),listener));
        return builder.build();
    }

    public  IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}
