package com.nanaios.CompactMekanismMachinesPlus.common.registries;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusLang;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismCreativeTabs;
import net.minecraft.world.item.CreativeModeTab;

//TODO 1.21.1対応

public class CompactPlusCreativeTabs {
    private CompactPlusCreativeTabs() {}

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(CompactMekanismMachinesPlus.MODID);

    public static final MekanismDeferredHolder<CreativeModeTab,CreativeModeTab> TAB;

    static {
        TAB = CREATIVE_TABS.registerMain(CompactPlusLang.COMPACTMEKANISMMACHINESPLUS, CompactPlusBlocks.COMPACT_FUSION_REACTOR, builder ->
                builder.withTabsBefore(MekanismCreativeTabs.MEKANISM.getKey())
                        .displayItems((displayParameters, output) -> {
                            CreativeTabDeferredRegister.addToDisplay(CompactPlusBlocks.BLOCKS, output);
                        })
        );
    }

}
