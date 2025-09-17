package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusLang;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiInteract;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GuiBoilerConfig extends GuiBoilerInfo{
    private GuiTextField superHeatingElementsField;
    private GuiTextField dispersersYField;

    public GuiBoilerConfig(EmptyTileContainer<TileEntityCompactThermoelectricBoiler> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        showTabs(GuiBoilerTab.BoilerTab.CONFIG);
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, UnitDisplayUtils.TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));

        dispersersYField = addRenderableWidget(new GuiTextField(this,28,41,26,11));
        dispersersYField.setInputValidator(InputValidator.DIGIT)
                .setEnterHandler(this::setDispersersY)
                .setMaxLength(2);

        superHeatingElementsField = addRenderableWidget(new GuiTextField(this, 33, 91, 46, 11));
        superHeatingElementsField.setInputValidator(InputValidator.DIGIT)
                .setEnterHandler(this::setSuperHeatingElements)
                .setMaxLength(4);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);

        drawString(guiGraphics, CompactPlusLang.DISPERSERS_Y.translate(tile.getDispersersY()), 8,26,titleTextColor());
        drawString(guiGraphics ,Component.literal("1 <"),8,43,titleTextColor());
        drawString(guiGraphics ,Component.literal("< " + TileEntityCompactThermoelectricBoiler.BOILER_HEIGHT),58,43,titleTextColor());
        drawString(guiGraphics, MekanismLang.BOILER_HEATERS.translate(tile.getSuperHeatingElements()), 8,76,titleTextColor());
        drawString(guiGraphics ,Component.literal("-1 <"),8,93,titleTextColor());
        drawString(guiGraphics ,Component.literal("< " + (tile.maxSuperHeatingElements + 1)),88,93,titleTextColor());

        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private void setSuperHeatingElements() {
        if(!superHeatingElementsField.getText().isEmpty()) {
            PacketUtils.sendToServer(new PacketCompactPlusGuiInteract(PacketCompactPlusGuiInteract.CompactPlusGuiInteraction.SUPER_HEATING_ELEMENTS, tile, Integer.parseInt(superHeatingElementsField.getText())));
            superHeatingElementsField.setText("");
        }
    }
    private void setDispersersY() {
        if(!dispersersYField.getText().isEmpty()) {
            PacketUtils.sendToServer(new PacketCompactPlusGuiInteract(PacketCompactPlusGuiInteract.CompactPlusGuiInteraction.DISPERSERS_Y, tile, Integer.parseInt(dispersersYField.getText())));
            dispersersYField.setText("");
        }
    }
}
