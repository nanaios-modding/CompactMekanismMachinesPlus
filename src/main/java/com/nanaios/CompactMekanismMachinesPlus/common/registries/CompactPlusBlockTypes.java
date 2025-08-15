package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.api.math.FloatingLong;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.blocktype.Generator;

public class CompactPlusBlockTypes {
    private CompactPlusBlockTypes() {}

    public static final Generator<TileEntityCompactFusionReactor> COMPACT_FUSION_REACTOR;

    static {
        COMPACT_FUSION_REACTOR = Generator.GeneratorBuilder.createGenerator(() ->CompactPlusTileEntityTypes.COMPACT_FISSION_REACTOR, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_CONTROLLER)
                .withEnergyConfig(() -> FloatingLong.create(4000000))
                .build();
    }
}
