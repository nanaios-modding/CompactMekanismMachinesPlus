package com.nanaios.CompactMekanismMachinesPlus.common.mixin;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import net.michanide.mekanismneutronactivator.common.tile.machine.TileEntityFusionNeutronActivator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityFusionNeutronActivator.class,remap = false)
public class MixinTileEntityFusionNeutronActivator {
    @Inject(method = "recalculateProductionRate",at= @At(value = "HEAD"))
    private void moreRecalculateProductionRate(CallbackInfoReturnable<Float> cir) {
        CompactMekanismMachinesPlus.LOGGER.info("this is inner of mixin function log!");
    }
}
