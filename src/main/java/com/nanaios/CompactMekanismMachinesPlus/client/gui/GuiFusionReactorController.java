package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiCompactBase;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiCompactConfigurableBase;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusLang;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.window.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.window.GuiTransporterConfigTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFusionReactorController extends GuiCompactConfigurableBase<TileEntityCompactFusionReactor, MekanismTileContainer<TileEntityCompactFusionReactor>> {

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

        //configとtransporterのタブを左に動かす
        renderables.forEach(element -> {
            if(element instanceof GuiSideConfigurationTab<?> tab) {
                tab.move(-26,0);
            } else if(element instanceof GuiTransporterConfigTab<?> tab) {
                tab.move(-26,0);
            }
        });
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        renderTitleText(poseStack);
        drawString(poseStack, MekanismLang.MULTIBLOCK_FORMED.translate(), 8, 16, titleTextColor());
        if(tile.isBurning()) {
            drawString(poseStack, CompactPlusLang.FUSION_REACTOR_ACTIVE.translate(),8, 26, titleTextColor());
        }
        super.drawForegroundText(poseStack, mouseX, mouseY);
    }
}