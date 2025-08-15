package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityCompactFusionReactor extends TileEntityConfigurableMachine {

    IGasTank deuteriumTank;
    IGasTank tritiumTank;
    IGasTank fuelTank;

    private BasicEnergyContainer energyContainer;

    public TileEntityCompactFusionReactor(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_FUSION_REACTOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY);

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,()-> FloatingLong.create(Long.MAX_VALUE));
        ejectorComponent.setOutputData(configComponent, TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.ENERGY)
                .setCanEject(type -> MekanismUtils.canFunction(this));
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.output(FloatingLong.create(4000000),listener));
        return builder.build();
    }

    public  BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}
