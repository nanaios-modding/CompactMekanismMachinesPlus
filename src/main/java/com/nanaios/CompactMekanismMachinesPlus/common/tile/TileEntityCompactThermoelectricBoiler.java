package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCompactThermoelectricBoiler extends TileEntityConfigurableMachine {
    public TileEntityCompactThermoelectricBoiler(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_THERMOELECTRIC_BOILER, pos, state);

        configComponent = new TileComponentConfig(this,TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.HEAT);
    }
}
