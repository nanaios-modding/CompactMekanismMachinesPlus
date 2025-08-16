package com.nanaios.CompactMekanismMachinesPlus.client;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.GuiFusionReactorController;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.GuiFusionReactorFuel;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.GuiFusionReactorHeat;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.GuiFusionReactorStats;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import mekanism.client.ClientRegistrationUtil;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = CompactMekanismMachinesPlus.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompactPlusClientRegistration {
    private CompactPlusClientRegistration(){}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerContainers(RegisterEvent event) {
        event.register(Registries.MENU, helper -> {
            ClientRegistrationUtil.registerScreen(CompactPlusContainerTypes.COMPACT_FUSION_REACTOR, GuiFusionReactorController::new);
            ClientRegistrationUtil.registerScreen(CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_FUEL, GuiFusionReactorFuel::new);
            ClientRegistrationUtil.registerScreen(CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_HEAT, GuiFusionReactorHeat::new);
            ClientRegistrationUtil.registerScreen(CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_STATS, GuiFusionReactorStats::new);
        });
    }
}
