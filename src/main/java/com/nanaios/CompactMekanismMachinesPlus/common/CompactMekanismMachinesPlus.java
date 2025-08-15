package com.nanaios.CompactMekanismMachinesPlus.common;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CompactMekanismMachinesPlus.MODID)
public class CompactMekanismMachinesPlus {
    public static final String MODID = "compactmekanismmachinesplus";

    public  CompactMekanismMachinesPlus(FMLJavaModLoadingContext context) {

        IEventBus modEventBus = context.getModEventBus();
        
    }
}
