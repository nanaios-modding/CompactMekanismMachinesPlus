package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityCompactPlusBase extends TileEntityConfigurableMachine {
    public TileEntityCompactPlusBase(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public @NotNull Component getName() {
        return this.hasCustomName() ? this.getCustomName() : TextComponentUtil.build(this.getBlockType());
    }
}
