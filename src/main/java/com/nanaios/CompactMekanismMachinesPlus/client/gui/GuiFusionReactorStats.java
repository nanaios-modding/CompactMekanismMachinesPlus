package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
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
    protected void drawForegroundText(@NotNull PoseStack poseStack, int mouseX, int mouseY) {



        renderTitleText(poseStack);
        drawString(poseStack, GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN), 6, 26, titleTextColor());
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getMinInjectionRate(false)), 16, 36, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getIgnitionTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 46, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxPlasmaTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 56, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxCasingTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 66, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getPassiveGeneration(false, false))),
                  16, 76, titleTextColor(), 156);

        drawString(poseStack, GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE), 6, 92, titleTextColor());
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getMinInjectionRate(true)), 16, 102, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getIgnitionTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 112, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxPlasmaTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 122, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getMaxCasingTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 132, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getPassiveGeneration(true, false))),
                  16, 142, titleTextColor(), 156);
        drawTextScaledBound(poseStack, GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(TextUtils.format(tile.getSteamPerTick(false))),
                  16, 152, titleTextColor(), 156);

        super.drawForegroundText(poseStack, mouseX, mouseY);
    }
}