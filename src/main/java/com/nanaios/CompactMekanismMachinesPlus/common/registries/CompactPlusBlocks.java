package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.common.content.blocktype.Generator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public class CompactPlusBlocks {
    private CompactPlusBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final BlockRegistryObject<
            BlockTile.BlockTileModel<TileEntityCompactFusionReactor, Generator<TileEntityCompactFusionReactor>>,
            ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityCompactFusionReactor, Generator<TileEntityCompactFusionReactor>>>
            > COMPACT_FUSION_REACTOR;

    public static final BlockRegistryObject<
            BlockTile.BlockTileModel<TileEntityCompactSPS, Machine<TileEntityCompactSPS>>,
            ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityCompactSPS, Machine<TileEntityCompactSPS>>>
            > COMPACT_SPS;

    public static final BlockRegistryObject<
            BlockTile.BlockTileModel<TileEntityCompactThermoelectricBoiler, Machine<TileEntityCompactThermoelectricBoiler>>,
            ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityCompactThermoelectricBoiler, Machine<TileEntityCompactThermoelectricBoiler>>>
            > COMPACT_THERMOELECTRIC_BOILER;

    static {
        COMPACT_FUSION_REACTOR = registerTooltipBlock(
                "compact_fusion_reactor",
                () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_FUSION_REACTOR, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY))
        );
        COMPACT_SPS = registerTooltipBlock(
                "compact_sps",
                () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_SPS, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY))
        );
        COMPACT_THERMOELECTRIC_BOILER = registerTooltipBlock(
                "compact_thermoelectric_boiler",
                () -> new BlockTile.BlockTileModel<>(CompactPlusBlockTypes.COMPACT_THERMOELECTRIC_BOILER, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY))
        );
    }

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.register(name, blockCreator, ItemBlockTooltip::new);
    }
}
