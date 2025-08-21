package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Collections;

public class GuiBoilerConfig extends GuiMekanismTile<TileEntityCompactThermoelectricBoiler, EmptyTileContainer<TileEntityCompactThermoelectricBoiler>> {
    public GuiBoilerConfig(EmptyTileContainer<TileEntityCompactThermoelectricBoiler> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiBoilerTab(this, tile, GuiBoilerTab.BoilerTab.MAIN));
        addRenderableWidget(new GuiBoilerTab(this, tile, GuiBoilerTab.BoilerTab.STAT));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, UnitDisplayUtils.TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
    }
}
