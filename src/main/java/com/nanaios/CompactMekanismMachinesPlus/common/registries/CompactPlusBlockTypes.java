package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.api.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registries.MekanismSounds;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.registries.GeneratorsSounds;

import java.util.EnumSet;

public class CompactPlusBlockTypes {
    private CompactPlusBlockTypes() {}

    public static final Generator<TileEntityCompactFusionReactor> COMPACT_FUSION_REACTOR;
    public static final Machine<TileEntityCompactSPS> COMPACT_SPS;
    public static final Machine<TileEntityCompactThermoelectricBoiler> COMPACT_THERMOELECTRIC_BOILER;

    static {
        COMPACT_FUSION_REACTOR = Generator.GeneratorBuilder.createGenerator(() ->CompactPlusTileEntityTypes.COMPACT_FISSION_REACTOR, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_CONTROLLER)
                .withEnergyConfig(() -> MekanismGeneratorsConfig.generators.fusionEnergyCapacity.get())
                .withGui(() -> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR, GeneratorsLang.FUSION_REACTOR)
                .withSound(GeneratorsSounds.FUSION_REACTOR)
                .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
                .build();
        COMPACT_SPS = Machine.MachineBuilder.createMachine(() -> CompactPlusTileEntityTypes.COMPACT_SPS, MekanismLang.DESCRIPTION_SPS_CASING)
                .withGui(() -> CompactPlusContainerTypes.COMPACT_SPS,MekanismLang.SPS)
                .withSound(MekanismSounds.SPS)
                .withEnergyConfig(MekanismConfig.storage.spsPort)
                .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
                .build();
        COMPACT_THERMOELECTRIC_BOILER = Machine.MachineBuilder.createMachine(() -> CompactPlusTileEntityTypes.COMPACT_THERMOELECTRIC_BOILER,MekanismLang.DESCRIPTION_BOILER_CASING)
                .build();
    }
}
