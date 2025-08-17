package com.nanaios.CompactMekanismMachinesPlus.mixin;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusLang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.tile.component.config.DataType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = DataType.class, remap = false)
public class MixinDataType {
    @Shadow
    @Final
    @Mutable
    public static DataType[] $VALUES;

    @Invoker("<init>")
    private DataType mixinDataType$invokeInit(String langEntry, int color, ILangEntry par3, EnumColor par4, CallbackInfo ci) {
        throw new AssertionError();
    }

    //@Unique
    //public static final MixinDataType INPUT_3 = mixinDataType$addVariant(CompactPlusLang.SIDE_DATA_INPUT_3,EnumColor.PINK);

    //INPUT_3(MekanismLang.SIDE_DATA_INPUT_2, EnumColor.ORANGE);

    @Inject(method = "<clinit>", at = @At("RETURN"))
    public static void mixinDataType$clinit(CallbackInfo ci) {
        ArrayList<DataType> variants = new ArrayList<DataType>(Arrays.asList(MixinDataType.$VALUES));

        variants.add(mixinDataType$create(CompactPlusLang.SIDE_DATA_INPUT_3,EnumColor.PINK));

        $VALUES = variants.toArray($VALUES);
        return;
    }
}
