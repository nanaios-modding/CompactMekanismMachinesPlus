package com.nanaios.CompactMekanismMachinesPlus.common;

import com.nanaios.CompactMekanismMachinesPlus.common.network.CompactPlusPacketHandler;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusCreativeTabs;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusTileEntityTypes;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import mekanism.generators.common.network.GeneratorsPacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactMekanismMachinesPlus.MODID)
public class CompactMekanismMachinesPlus implements IModModule {

    public static CompactMekanismMachinesPlus instance;

    public static final Logger LOGGER = LogManager.getLogger();

    public final Version versionNumber;

    public static final String MODID = "compactmekanismmachinesplus";

    private final CompactPlusPacketHandler packetHandler;

    @SuppressWarnings("removal")
    public  CompactMekanismMachinesPlus(FMLJavaModLoadingContext context) {
        instance = this;

        IEventBus modEventBus = context.getModEventBus();

        //listenerを登録
        modEventBus.addListener(this::commonSetup);

        //eventBusに登録
        CompactPlusBlocks.BLOCKS.register(modEventBus);
        CompactPlusCreativeTabs.CREATIVE_TABS.register(modEventBus);
        CompactPlusContainerTypes.CONTAINER_TYPES.register(modEventBus);
        CompactPlusTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);

        //情報をセット
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
        packetHandler = new CompactPlusPacketHandler();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        packetHandler.initialize();
    }

    public static CompactPlusPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    @SuppressWarnings("removal")
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(CompactMekanismMachinesPlus.MODID, path);
    }

    public Version getVersion() {return versionNumber;}

    public String getName() {return "CompactMekanismMachinesPlus";}

    public void resetClient() {

    }
}
