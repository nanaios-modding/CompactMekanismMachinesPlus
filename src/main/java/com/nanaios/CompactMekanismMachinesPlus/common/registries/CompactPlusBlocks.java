package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.generators.common.content.blocktype.Generator;
import net.minecraft.world.level.material.MapColor;

//TODO 1.21.1対応

public class CompactPlusBlocks {
    private CompactPlusBlocks() {}
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityCompactFusionReactor, Generator<TileEntityCompactFusionReactor>>, ItemBlockMachine> COMPACT_FUSION_REACTOR;
    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityCompactSPS, Machine<TileEntityCompactSPS>>, ItemBlockMachine> COMPACT_SPS;
    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityCompactThermoelectricBoiler, Machine<TileEntityCompactThermoelectricBoiler>>, ItemBlockMachine> COMPACT_THERMOELECTRIC_BOILER;

    static {
        COMPACT_FUSION_REACTOR = BLOCKS.register("compact_fusion_reactor", () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_FUSION_REACTOR, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)), ItemBlockMachine::new);
        COMPACT_SPS = BLOCKS.register("compact_sps", () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_SPS, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)), ItemBlockMachine::new);
        COMPACT_THERMOELECTRIC_BOILER = BLOCKS.register("compact_thermoelectric_boiler", () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_THERMOELECTRIC_BOILER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockMachine::new);
    }
}
