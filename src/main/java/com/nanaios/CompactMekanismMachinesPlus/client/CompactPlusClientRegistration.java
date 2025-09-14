package com.nanaios.CompactMekanismMachinesPlus.client;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.*;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import mekanism.client.ClientRegistrationUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CompactMekanismMachinesPlus.MODID, value = Dist.CLIENT)
public class CompactPlusClientRegistration {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.COMPACT_FUSION_REACTOR, GuiFusionReactorController::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_FUEL, GuiFusionReactorFuel::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_HEAT, GuiFusionReactorHeat::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_STATS, GuiFusionReactorStats::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.COMPACT_SPS, GuiSPS::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.COMPACT_THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.BOILER_STATS, GuiBoilerStats::new);
        ClientRegistrationUtil.registerScreen(event,CompactPlusContainerTypes.BOILER_CONFIG, GuiBoilerConfig::new);
    }
}
