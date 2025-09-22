package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import java.util.Collections;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiCompactBase;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab.BoilerTab;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.graph.GuiLongGraph;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiBoilerStats extends GuiCompactBase<TileEntityCompactThermoelectricBoiler, EmptyTileContainer<TileEntityCompactThermoelectricBoiler>> {

    private GuiLongGraph boilGraph;
    private GuiLongGraph maxGraph;

    public GuiBoilerStats(EmptyTileContainer<TileEntityCompactThermoelectricBoiler> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiBoilerTab(this, tile, BoilerTab.MAIN));
        addRenderableWidget(new GuiBoilerTab(this, tile, BoilerTab.CONFIG));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
        boilGraph = addRenderableWidget(new GuiLongGraph(this, 7, 82, 162, 38, MekanismLang.BOIL_RATE::translate));
        maxGraph = addRenderableWidget(new GuiLongGraph(this, 7, 121, 162, 38, MekanismLang.MAX_BOIL_RATE::translate));
        maxGraph.enableFixedScale(MathUtils.clampToLong((MekanismConfig.general.superheatingHeatTransfer.get() * tile.superHeatingElements) / HeatUtils.getWaterThermalEnthalpy()));
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        drawCenteredText(poseStack, title, 0, imageWidth, titleLabelY, titleTextColor());
        drawString(poseStack, MekanismLang.BOILER_MAX_WATER.translate(TextUtils.format(tile.waterTank.getCapacity())), 8, 26, titleTextColor());
        drawString(poseStack, MekanismLang.BOILER_MAX_STEAM.translate(TextUtils.format(tile.steamTank.getCapacity())), 8, 35, titleTextColor());
        drawString(poseStack, MekanismLang.BOILER_HEAT_TRANSFER.translate(), 8, 49, subheadingTextColor());
        drawString(poseStack, MekanismLang.BOILER_HEATERS.translate(tile.superHeatingElements), 14, 58, titleTextColor());
        drawString(poseStack, MekanismLang.BOILER_CAPACITY.translate(TextUtils.format(tile.getBoilCapacity())), 8, 72, titleTextColor());
        super.drawForegroundText(poseStack, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        boilGraph.addData(tile.lastBoilRate);
        maxGraph.addData(tile.lastMaxBoil);
    }
}