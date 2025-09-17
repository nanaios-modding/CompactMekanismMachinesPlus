package com.nanaios.CompactMekanismMachinesPlus.client.gui.element;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusTileButtonPress;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusTileButtonPress.ClickedCompactPlusTileButton;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab.BoilerTab;
import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiBoilerTab extends GuiTabElementType<TileEntityCompactThermoelectricBoiler, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityCompactThermoelectricBoiler tile, BoilerTab type) {
        super(gui, tile, type);
    }

    public enum BoilerTab implements TabType<TileEntityCompactThermoelectricBoiler> {
        MAIN("gases.png", MekanismLang.MAIN_TAB, 6,ClickedCompactPlusTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT("stats.png", MekanismLang.BOILER_STATS, 34,ClickedCompactPlusTileButton.TAB_STATS, SpecialColors.TAB_CONFIGURATION),
        CONFIG("radial/wrench.png", MekanismLang.BUTTON_CONFIG,62, ClickedCompactPlusTileButton.TAB_CONFIG, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ColorRegistryObject colorRO;
        private final ClickedCompactPlusTileButton button;
        private final ILangEntry description;
        private final String path;
        private final int yPos;

        BoilerTab(String path, ILangEntry description,int yPos, ClickedCompactPlusTileButton button, ColorRegistryObject colorRO) {
            this.path = path;
            this.description = description;
            this.yPos = yPos;
            this.button = button;
            this.colorRO = colorRO;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI, path);
        }

        @Override
        public void onClick(TileEntityCompactThermoelectricBoiler tile) {
            CompactMekanismMachinesPlus.packetHandler().sendToServer(new PacketCompactPlusTileButtonPress(button, tile.getBlockPos()));
        }

        @Override
        public Component getDescription() {
            return description.translate();
        }

        @Override
        public int getYPos() {
            return yPos;
        }

        @Override
        public ColorRegistryObject getTabColor() {
            return colorRO;
        }
    }
}