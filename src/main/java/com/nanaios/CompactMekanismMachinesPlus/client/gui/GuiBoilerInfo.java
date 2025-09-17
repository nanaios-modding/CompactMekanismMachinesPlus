package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.ILegacyFontRenderer;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiBoilerInfo extends GuiMekanismTile<TileEntityCompactThermoelectricBoiler, EmptyTileContainer<TileEntityCompactThermoelectricBoiler>> implements ILegacyFontRenderer {
    protected GuiBoilerInfo(EmptyTileContainer<TileEntityCompactThermoelectricBoiler> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    protected void showTabs(GuiBoilerTab.BoilerTab selectedTab) {
        for (GuiBoilerTab.BoilerTab tab : GuiBoilerTab.BoilerTab.values()) {
            if(tab.equals(selectedTab)) continue;
            addRenderableWidget(new GuiBoilerTab(this, tile, tab));
        }
    }
}
