package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import java.util.List;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFusionReactorController extends GuiConfigurableTile<TileEntityCompactFusionReactor, MekanismTileContainer<TileEntityCompactFusionReactor>> {

    public GuiFusionReactorController(MekanismTileContainer<TileEntityCompactFusionReactor> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            return List.of(MekanismLang.STORING.translate(EnergyDisplay.of(tile.energyContainer)),
                    GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getPassiveGeneration(false, true))));
        }));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component transfer = MekanismUtils.getTemperatureDisplay(tile.lastTransferLoss, TemperatureUnit.KELVIN, false);
            Component environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return List.of(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }));

        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));

        System.out.println("init Compact Controller Gui ");
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        drawString(guiGraphics, MekanismLang.MULTIBLOCK_FORMED.translate(), 8, 16, titleTextColor());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}