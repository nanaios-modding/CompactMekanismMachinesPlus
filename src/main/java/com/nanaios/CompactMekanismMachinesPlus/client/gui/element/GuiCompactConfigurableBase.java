package com.nanaios.CompactMekanismMachinesPlus.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiCompactConfigurableBase<TILE extends TileEntityMekanism & ISideConfiguration, CONTAINER extends MekanismTileContainer<TILE>> extends GuiConfigurableTile<TILE,CONTAINER> {
    protected GuiCompactConfigurableBase(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void renderBg(@NotNull PoseStack matrix, float partialTick, int mouseX, int mouseY) {
        MekanismRenderer.resetColor();
        if (this.width >= 8 && this.height >= 8) {
            GuiUtils.renderBackgroundTexture(matrix, BASE_BACKGROUND, 4, 4, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 256, 256);
        } else {
            Mekanism.logger.warn("Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", this.getClass().getSimpleName());
        }
    }
}
