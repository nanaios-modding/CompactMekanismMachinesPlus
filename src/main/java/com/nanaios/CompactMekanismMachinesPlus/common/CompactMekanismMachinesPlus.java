package com.nanaios.CompactMekanismMachinesPlus.common;

import com.nanaios.CompactMekanismMachinesPlus.common.network.CompactPlusPacketHandler;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusCreativeTabs;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusTileEntityTypes;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;

//TODO 1.21.1対応

@Mod(CompactMekanismMachinesPlus.MODID)
public class CompactMekanismMachinesPlus implements IModModule {

    public static CompactMekanismMachinesPlus instance;

    public static final Logger LOGGER = LogManager.getLogger();

    public final Version versionNumber;

    public static final String MODID = "compactmekanismmachinesplus";

    private final CompactPlusPacketHandler packetHandler;

    public  CompactMekanismMachinesPlus(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;

        //eventBusに登録
        CompactPlusBlocks.BLOCKS.register(modEventBus);
        CompactPlusCreativeTabs.CREATIVE_TABS.register(modEventBus);
        CompactPlusContainerTypes.CONTAINER_TYPES.register(modEventBus);
        CompactPlusTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);

        //情報をセット
        versionNumber = new Version(modContainer);
        packetHandler = new CompactPlusPacketHandler(modEventBus,versionNumber);
    }

    public static CompactPlusPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(CompactMekanismMachinesPlus.MODID, path);
    }

    public Version getVersion() {return versionNumber;}

    public String getName() {return "CompactMekanismMachinesPlus";}
}
