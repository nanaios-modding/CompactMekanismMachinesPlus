package com.nanaios.CompactMekanismMachinesPlus.common.mixin;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.michanide.mekanismneutronactivator.common.config.MNAConfig;
import net.michanide.mekanismneutronactivator.common.tile.machine.TileEntityFusionNeutronActivator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityFusionNeutronActivator.class,remap = false)
public class MixinTileEntityFusionNeutronActivator {
    @Inject(method = "recalculateProductionRate", at =@At("HEAD"), cancellable = true)
    private void moreRecalculateProductionRate(CallbackInfoReturnable<Float> cir) {
        TileEntityFusionNeutronActivator that = (TileEntityFusionNeutronActivator) (Object) this;
        Level worldCompactPlusMixin = that.getLevel();
        if (worldCompactPlusMixin == null || !MekanismUtils.canFunction(that)) {
            cir.setReturnValue(0F);
        }

        BlockPos dstBlockCompactPlusMixin = that.getBlockPos().above(2);
        BlockEntity aboveEntityCompactPlusMixin = WorldUtils.getTileEntity(worldCompactPlusMixin, dstBlockCompactPlusMixin);
        if(aboveEntityCompactPlusMixin instanceof TileEntityCompactFusionReactor tile){
            if(tile.isBurning()){
                Long lastFuelBurnedCompactPlusMixin = that.getFuelBurned();
                float productionCompactPlusMixin = (float) MNAConfig.general.fusionNeutronActivatorMultiplier.get() * (float) lastFuelBurnedCompactPlusMixin;
                cir.setReturnValue(productionCompactPlusMixin);
            }
        }  //CompactMekanismMachinesPlus.LOGGER.info("mixin upper entity = {}",aboveEntityCompactPlusMixin.getType());

    }
}
