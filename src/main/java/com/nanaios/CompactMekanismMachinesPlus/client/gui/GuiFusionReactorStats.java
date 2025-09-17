package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.api.text.EnumColor;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFusionReactorStats extends GuiFusionReactorInfo {

    public GuiFusionReactorStats(EmptyTileContainer<TileEntityCompactFusionReactor> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        int indentation = 4;
        int textArea = getXSize() - indentation;
        drawScrollingString(guiGraphics,GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN),
                0, 26, TextAlignment.LEFT, titleTextColor(), 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getMinInjectionRate(false)),
                indentation, 36, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getIgnitionTemperature(false), TemperatureUnit.KELVIN, true)),
                indentation, 46, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxPlasmaTemperature(false), TemperatureUnit.KELVIN, true)),
                indentation, 56, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxCasingTemperature(false), TemperatureUnit.KELVIN, true)),
                indentation, 66, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getPassiveGeneration(false, false))),
                indentation, 76, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);

        drawScrollingString(guiGraphics,GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE),
                0, 92, TextAlignment.LEFT, titleTextColor(), 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getMinInjectionRate(true)),
                indentation, 102, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getIgnitionTemperature(true), TemperatureUnit.KELVIN, true)),
                indentation, 112, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxPlasmaTemperature(true), TemperatureUnit.KELVIN, true)),
                indentation, 122, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxCasingTemperature(true), TemperatureUnit.KELVIN, true)),
                indentation, 132, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getPassiveGeneration(true, false))),
                indentation, 142, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(TextUtils.format(tile.getSteamPerTick(false))),
                indentation, 152, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);

        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}