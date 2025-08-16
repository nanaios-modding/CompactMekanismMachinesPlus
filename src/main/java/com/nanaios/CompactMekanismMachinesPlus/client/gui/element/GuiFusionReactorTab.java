package com.nanaios.CompactMekanismMachinesPlus.client.gui.element;

import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactGuiButtonPress;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactGuiButtonPress.ClickedCompactTileButton;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.GeneratorsSpecialColors;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiFusionReactorTab extends GuiTabElementType<TileEntityCompactFusionReactor,FusionReactorTab> {

    public GuiFusionReactorTab(IGuiWrapper gui, TileEntityCompactFusionReactor tile, FusionReactorTab type) {
        super(gui, tile, type);
    }

    public enum FusionReactorTab implements TabType<TileEntityCompactFusionReactor> {
        HEAT(MekanismUtils.getResource(ResourceType.GUI, "heat.png"), GeneratorsLang.HEAT_TAB, 6, ClickedCompactTileButton.TAB_HEAT, GeneratorsSpecialColors.TAB_MULTIBLOCK_HEAT),
        FUEL(MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "fuel.png"), GeneratorsLang.FUEL_TAB, 34, ClickedCompactTileButton.TAB_FUEL, GeneratorsSpecialColors.TAB_MULTIBLOCK_FUEL),
        STAT(MekanismUtils.getResource(ResourceType.GUI, "stats.png"), GeneratorsLang.STATS_TAB, 62, ClickedCompactTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ClickedCompactTileButton button;
        private final ColorRegistryObject colorRO;
        private final ILangEntry description;
        private final ResourceLocation path;
        private final int yPos;

        FusionReactorTab(ResourceLocation path, ILangEntry description, int y, ClickedCompactTileButton button, ColorRegistryObject colorRO) {
            this.path = path;
            this.description = description;
            this.yPos = y;
            this.button = button;
            this.colorRO = colorRO;
        }

        @Override
        public ResourceLocation getResource() {
            return path;
        }

        public void onClick(TileEntityCompactFusionReactor tile) {
            MekanismGenerators.packetHandler().sendToServer(new PacketCompactGuiButtonPress(button, tile.getBlockPos()));
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