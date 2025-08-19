package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;

public class CompactPlusTileEntityTypes {
    private CompactPlusTileEntityTypes() {}
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final TileEntityTypeRegistryObject<TileEntityCompactFusionReactor> COMPACT_FISSION_REACTOR;
    public static final TileEntityTypeRegistryObject<TileEntityCompactSPS> COMPACT_SPS;

    static {
        COMPACT_FISSION_REACTOR = TILE_ENTITY_TYPES.register(CompactPlusBlocks.COMPACT_FUSION_REACTOR,TileEntityCompactFusionReactor::new);
        COMPACT_SPS = TILE_ENTITY_TYPES.register(CompactPlusBlocks.COMPACT_SPS,TileEntityCompactSPS::new);
    }
}
