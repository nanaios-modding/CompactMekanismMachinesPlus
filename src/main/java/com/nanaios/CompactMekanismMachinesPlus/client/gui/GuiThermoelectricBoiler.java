package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import java.util.Collections;
import java.util.List;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab.BoilerTab;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.window.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.window.GuiTransporterConfigTab;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiThermoelectricBoiler extends GuiConfigurableTile<TileEntityCompactThermoelectricBoiler, MekanismTileContainer<TileEntityCompactThermoelectricBoiler>> {

    public GuiThermoelectricBoiler(MekanismTileContainer<TileEntityCompactThermoelectricBoiler> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageWidth += 40;
        inventoryLabelY += 2;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 60, 23, 96, 40, () -> {
            return List.of(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTotalTemperature(), TemperatureUnit.KELVIN, true)),
                  MekanismLang.BOIL_RATE.translate(TextUtils.format(tile.lastBoilRate)), MekanismLang.MAX_BOIL_RATE.translate(TextUtils.format(tile.lastMaxBoil)));
        }).jeiCategories(MekanismJEIRecipeType.BOILER));
        addRenderableWidget(new GuiBoilerTab(this, tile, BoilerTab.STAT));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.BOIL_RATE.translate(TextUtils.format(tile.lastBoilRate));
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.lastBoilRate / (double) tile.lastMaxBoil);
            }
        }, 44, 13));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.MAX_BOIL_RATE.translate(TextUtils.format(tile.lastMaxBoil));
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.lastMaxBoil * HeatUtils.getWaterThermalEnthalpy() /
                                   (tile.superHeatingElements * MekanismConfig.general.superheatingHeatTransfer.get()));
            }
        }, 164, 13));
        addRenderableWidget(new GuiGasGauge(() -> tile.superheatedCoolantTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 6, 13)
              .setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addRenderableWidget(new GuiFluidGauge(() -> tile.waterTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 26, 13)
              .setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)));
        addRenderableWidget(new GuiGasGauge(() -> tile.steamTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 172, 13)
              .setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)));
        addRenderableWidget(new GuiGasGauge(() -> tile.cooledCoolantTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 192, 13)
              .setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));

        renderables.forEach(element -> {
            if(element instanceof GuiSideConfigurationTab<?> tab) {
                tab.move(-26,0);
            }
        });
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        drawString(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}