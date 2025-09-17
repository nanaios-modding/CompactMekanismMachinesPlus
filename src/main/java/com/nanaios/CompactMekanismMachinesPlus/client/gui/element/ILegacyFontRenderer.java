package com.nanaios.CompactMekanismMachinesPlus.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.IFancyFontRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface ILegacyFontRenderer extends IFancyFontRenderer {
    default int drawString(GuiGraphics guiGraphics, Component component, int x, int y, int color) {
        return guiGraphics.drawString(font(), component, x, y, color, false);
    }

    default void drawCenteredText(GuiGraphics guiGraphics, Component component, float xStart, float areaWidth, float y, int color) {
        int textWidth = getStringWidth(component);
        float centerX = xStart + (areaWidth / 2F) - (textWidth / 2F);
        drawTextExact(guiGraphics, component, centerX, y, color);
    }

    default int getStringWidth(Component component) {
        return font().width(component);
    }

    default void drawTextExact(GuiGraphics guiGraphics, Component text, float x, float y, int color) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        drawString(guiGraphics, text, 0, 0, color);
        pose.popPose();
    }
}
