package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;

//TODO 1.21.1対応

public class CompactPlusTileEntityTypes {
    private CompactPlusTileEntityTypes() {}
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final TileEntityTypeRegistryObject<TileEntityCompactFusionReactor> COMPACT_FISSION_REACTOR;
    public static final TileEntityTypeRegistryObject<TileEntityCompactSPS> COMPACT_SPS;
    public static final TileEntityTypeRegistryObject<TileEntityCompactThermoelectricBoiler> COMPACT_THERMOELECTRIC_BOILER;

    static {
        COMPACT_FISSION_REACTOR = TILE_ENTITY_TYPES.register(CompactPlusBlocks.COMPACT_FUSION_REACTOR, TileEntityCompactFusionReactor::new);
        COMPACT_SPS = TILE_ENTITY_TYPES.register(CompactPlusBlocks.COMPACT_SPS,TileEntityCompactSPS::new);
        COMPACT_THERMOELECTRIC_BOILER = TILE_ENTITY_TYPES.register(CompactPlusBlocks.COMPACT_THERMOELECTRIC_BOILER, TileEntityCompactThermoelectricBoiler::new);
    }
}
