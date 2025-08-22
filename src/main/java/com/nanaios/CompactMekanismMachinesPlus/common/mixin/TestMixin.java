package com.nanaios.CompactMekanismMachinesPlus.common.mixin;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import mekanism.common.content.boiler.BoilerMultiblockData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoilerMultiblockData.class,remap = false)
public class TestMixin {
    @Inject(method = "onCreated",at = @At("TAIL"))
    void showHeatCapacityWithTest(Level world, CallbackInfo ci) {
        BoilerMultiblockData thatTest = (BoilerMultiblockData) (Object) this;

        CompactMekanismMachinesPlus.LOGGER.info("capacity of heat = {}",thatTest.heatCapacitor.getHeatCapacity());
    }
}
