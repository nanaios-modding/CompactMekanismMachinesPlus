package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactSPS;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSPS extends GuiConfigurableTile<TileEntityCompactSPS, MekanismTileContainer<TileEntityCompactSPS>> {

    public GuiSPS(MekanismTileContainer<TileEntityCompactSPS> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 16;
        inventoryLabelY = imageHeight - 92;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiChemicalGauge(() -> tile.inputTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 7, 17));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.outputTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 151, 17));
        addRenderableWidget(new GuiInnerScreen(this, 27, 17, 122, 60, () -> {
            List<Component> list = new ArrayList<>();
            list.add(MekanismLang.SPS_ENERGY_INPUT.translate(EnergyDisplay.of(tile.lastReceivedEnergy)));
            list.add(MekanismLang.PROCESS_RATE_MB.translate(tile.getProcessRate()));
            return list;
        }).recipeViewerCategories(RecipeViewerRecipeType.SPS));
        addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(tile.getScaledProgress()));
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getScaledProgress());
            }
        }, 7, 79, 160, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
