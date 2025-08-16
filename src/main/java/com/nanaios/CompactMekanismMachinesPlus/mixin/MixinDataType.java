package com.nanaios.CompactMekanismMachinesPlus.mixin;

import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DataType.class, remap = false)
public class MixinDataType {
    @Shadow
    @Final
    @Mutable
    public static DataType NONE;
    //INPUT_3(MekanismLang.SIDE_DATA_INPUT_2, EnumColor.ORANGE);

    @Inject(method = "<clinit>",at=@At(value = "INVOKE", target = "Lmekanism/common/tile/component/config/DataType;values()[Lmekanism/common/tile/component/config/DataType;", ordinal = 0))
    private static void MixinDataType(CallbackInfo ci) {
        System.out.println("mixin init!");
    }
}
