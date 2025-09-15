package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

public class CompactPlusTileEntityTypes {
    private CompactPlusTileEntityTypes() {}
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final TileEntityTypeRegistryObject<TileEntityCompactFusionReactor> COMPACT_FISSION_REACTOR;
    public static final TileEntityTypeRegistryObject<TileEntityCompactSPS> COMPACT_SPS;
    public static final TileEntityTypeRegistryObject<TileEntityCompactThermoelectricBoiler> COMPACT_THERMOELECTRIC_BOILER;

    static {
        COMPACT_FISSION_REACTOR = TILE_ENTITY_TYPES.mekBuilder(CompactPlusBlocks.COMPACT_FUSION_REACTOR,TileEntityCompactFusionReactor::new)
                .clientTicker(TileEntityMekanism::tickClient)
                .serverTicker(TileEntityMekanism::tickServer)
                .withSimple(Capabilities.CONFIG_CARD)
                .withSimple(Capabilities.EVAPORATION_SOLAR)
                .build();
        COMPACT_SPS = TILE_ENTITY_TYPES.mekBuilder(CompactPlusBlocks.COMPACT_SPS,TileEntityCompactSPS::new)
                .clientTicker(TileEntityMekanism::tickClient)
                .serverTicker(TileEntityMekanism::tickServer)
                .withSimple(Capabilities.CONFIG_CARD)
                .withSimple(Capabilities.EVAPORATION_SOLAR)
                .build();
        COMPACT_THERMOELECTRIC_BOILER = TILE_ENTITY_TYPES.mekBuilder(CompactPlusBlocks.COMPACT_THERMOELECTRIC_BOILER, TileEntityCompactThermoelectricBoiler::new)
                .clientTicker(TileEntityMekanism::tickClient)
                .serverTicker(TileEntityMekanism::tickServer)
                .withSimple(Capabilities.CONFIG_CARD)
                .withSimple(Capabilities.EVAPORATION_SOLAR)
                .build();
    }
}
