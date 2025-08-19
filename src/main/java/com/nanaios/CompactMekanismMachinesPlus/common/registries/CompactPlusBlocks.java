package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.generators.common.content.blocktype.Generator;
import net.minecraft.world.level.material.MapColor;

public class CompactPlusBlocks {
    private CompactPlusBlocks() {}
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityCompactFusionReactor, Generator<TileEntityCompactFusionReactor>>, ItemBlockMachine> COMPACT_FUSION_REACTOR;
    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityCompactSPS, Machine<TileEntityCompactSPS>>, ItemBlockMachine> COMPACT_SPS;


    static {
        COMPACT_FUSION_REACTOR = BLOCKS.register("compact_fusion_reactor", () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_FUSION_REACTOR, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)), ItemBlockMachine::new);
        COMPACT_SPS = BLOCKS.register("compact_sps", () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_SPS, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)), ItemBlockMachine::new);
    }
}
