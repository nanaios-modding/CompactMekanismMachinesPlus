package com.nanaios.CompactMekanismMachinesPlus.mixin;

import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.tile.component.config.DataType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

//TODO なんとかしろ、未来の私

@Mixin(value = DataType.class, remap = false)
public class MixinDataType {
    @Shadow
    @Final
    @Mutable
    private static DataType[] $VALUES;

    //@Unique
    //public static final MixinDataType INPUT_3 = mixinDataType$addVariant(CompactPlusLang.SIDE_DATA_INPUT_3,EnumColor.PINK);

    //INPUT_3(MekanismLang.SIDE_DATA_INPUT_2, EnumColor.ORANGE);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mixinDataType$init(CallbackInfo ci) {
        ArrayList<DataType> variants = new ArrayList<DataType>(Arrays.asList(MixinDataType.$VALUES));

        System.out.println("mixin GO!!");
        System.out.println($VALUES.length);
        //variants.add();

        $VALUES = variants.toArray($VALUES);
        return;
    }
}
