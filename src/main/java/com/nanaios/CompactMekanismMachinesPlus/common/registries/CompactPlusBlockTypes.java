package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.registries.GeneratorsSounds;

public class CompactPlusBlockTypes {
    private CompactPlusBlockTypes() {}

    public static final Generator<TileEntityCompactFusionReactor> COMPACT_FUSION_REACTOR;

    static {
        COMPACT_FUSION_REACTOR = Generator.GeneratorBuilder.createGenerator(() ->CompactPlusTileEntityTypes.COMPACT_FISSION_REACTOR, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_CONTROLLER)
                .withEnergyConfig(() -> MekanismGeneratorsConfig.generators.fusionEnergyCapacity.get())
                .withGui(() -> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR, GeneratorsLang.FUSION_REACTOR)
                .withSound(GeneratorsSounds.FUSION_REACTOR)
                .build();
    }
}
