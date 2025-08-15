package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.generators.common.content.blocktype.Generator;

public class CompactPlusBlocks {
    private CompactPlusBlocks() {}
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityCompactFusionReactor, Generator<TileEntityCompactFusionReactor>>, ItemBlockMachine> COMPACT_FUSION_REACTOR;

    static {
        COMPACT_FUSION_REACTOR = BLOCKS.register("compact_fusion_reactor", () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_FUSION_REACTOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor()).strength(0.2F)), ItemBlockMachine::new);
    }
}
