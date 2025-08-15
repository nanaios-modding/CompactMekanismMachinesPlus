package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public class CompactPlusContainerTypes {
    public CompactPlusContainerTypes() {}

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityCompactFusionReactor>> COMPACT_FUSION_REACTOR;

    static {
        COMPACT_FUSION_REACTOR = CONTAINER_TYPES.register(CompactPlusBlocks.COMPACT_FUSION_REACTOR, TileEntityCompactFusionReactor.class);
    }

}
