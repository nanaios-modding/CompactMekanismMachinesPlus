package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import java.util.List;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.ILegacyFontRenderer;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class GuiFusionReactorInfo extends GuiMekanismTile<TileEntityCompactFusionReactor, EmptyTileContainer<TileEntityCompactFusionReactor>> implements ILegacyFontRenderer {

    protected GuiFusionReactorInfo(EmptyTileContainer<TileEntityCompactFusionReactor> container, Inventory inv, Component title) {
        super(container, inv, title);
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new MekanismImageButton(this, 6, 6, 14, getButtonLocation("back"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketTileButtonPress(PacketTileButtonPress.ClickedTileButton.BACK_BUTTON, ((GuiFusionReactorInfo) element.gui()).tile))));
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            return List.of(MekanismLang.STORING.translate(EnergyDisplay.of(tile.energyContainer)),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getPassiveGeneration(false, true))));
        }));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component transfer = MekanismUtils.getTemperatureDisplay(tile.lastTransferLoss, TemperatureUnit.KELVIN, false);
            Component environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return List.of(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
    }
}