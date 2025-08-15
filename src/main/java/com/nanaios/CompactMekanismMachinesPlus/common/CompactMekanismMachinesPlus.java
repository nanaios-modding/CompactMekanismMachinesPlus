package com.nanaios.CompactMekanismMachinesPlus.common;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusCreativeTabs;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusTileEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CompactMekanismMachinesPlus.MODID)
public class CompactMekanismMachinesPlus {
    public static final String MODID = "compactmekanismmachinesplus";

    public  CompactMekanismMachinesPlus(FMLJavaModLoadingContext context) {

        IEventBus modEventBus = context.getModEventBus();

        //eventBusに登録
        CompactPlusBlocks.BLOCKS.register(modEventBus);
        CompactPlusCreativeTabs.CREATIVE_TABS.register(modEventBus);
        CompactPlusContainerTypes.CONTAINER_TYPES.register(modEventBus);
        CompactPlusTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);

    }

    @SuppressWarnings("removal")
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(CompactMekanismMachinesPlus.MODID, path);
    }
}
