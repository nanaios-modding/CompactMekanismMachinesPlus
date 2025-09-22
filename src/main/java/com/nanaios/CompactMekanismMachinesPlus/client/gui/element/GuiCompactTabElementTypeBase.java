package com.nanaios.CompactMekanismMachinesPlus.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class GuiCompactTabElementTypeBase<TILE extends BlockEntity, TAB extends Enum<?> & TabType<TILE>> extends GuiTabElementType<TILE,TAB> {
    public GuiCompactTabElementTypeBase(IGuiWrapper gui, TILE tile, TAB type) {
        super(gui, tile, type);
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
}
