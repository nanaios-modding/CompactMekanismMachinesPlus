package com.nanaios.CompactMekanismMachinesPlus.common;

import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

public enum CompactPlusLang implements ILangEntry {
    SIDE_DATA_INPUT_3("side_data", "input_3"),
    COMPACTMEKANISMMACHINESPLUS("other","mod_name");

    private final String key;

    CompactPlusLang(String type, String path) {
        this(Util.makeDescriptionId(type, CompactMekanismMachinesPlus.rl(path)));
    }

    CompactPlusLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
