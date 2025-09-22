package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiBoilerTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiCompactBase;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactPlusLang;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiInteract;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GuiBoilerConfig extends GuiCompactBase<TileEntityCompactThermoelectricBoiler, EmptyTileContainer<TileEntityCompactThermoelectricBoiler>> {

    private GuiTextField superHeatingElementsField;
    private GuiTextField dispersersYField;

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

        dispersersYField = addRenderableWidget(new GuiTextField(this,28,41,26,11));
        dispersersYField.setInputValidator(InputValidator.DIGIT)
                .setEnterHandler(this::setDispersersY)
                .setMaxLength(2);

        superHeatingElementsField = addRenderableWidget(new GuiTextField(this, 28, 91, 46, 11));
        superHeatingElementsField.setInputValidator(InputValidator.DIGIT)
                .setEnterHandler(this::setSuperHeatingElements)
                .setMaxLength(4);
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        renderTitleText(poseStack);

        drawString(poseStack, CompactPlusLang.DISPERSERS_Y.translate(tile.getDispersersY()), 8,26,titleTextColor());
        drawString(poseStack ,Component.literal("1 <"),8,43,titleTextColor());
        drawString(poseStack ,Component.literal("< " + TileEntityCompactThermoelectricBoiler.BOILER_HEIGHT),58,43,titleTextColor());
        drawString(poseStack, MekanismLang.BOILER_HEATERS.translate(tile.getSuperHeatingElements()), 8,76,titleTextColor());
        drawString(poseStack ,Component.literal("0 ≦"),8,93,titleTextColor());
        drawString(poseStack ,Component.literal("≦ " + tile.maxSuperHeatingElements),78,93,titleTextColor());

        super.drawForegroundText(poseStack, mouseX, mouseY);
    }

    private void setSuperHeatingElements() {
        if(!superHeatingElementsField.getText().isEmpty()) {
            CompactMekanismMachinesPlus.packetHandler().sendToServer(new PacketCompactPlusGuiInteract(PacketCompactPlusGuiInteract.CompactPlusGuiInteraction.SUPER_HEATING_ELEMENTS, tile, Integer.parseInt(superHeatingElementsField.getText())));
            superHeatingElementsField.setText("");
        }
    }
    private void setDispersersY() {
        if(!dispersersYField.getText().isEmpty()) {
            CompactMekanismMachinesPlus.packetHandler().sendToServer(new PacketCompactPlusGuiInteract(PacketCompactPlusGuiInteract.CompactPlusGuiInteraction.DISPERSERS_Y, tile, Integer.parseInt(dispersersYField.getText())));
            dispersersYField.setText("");
        }
    }
}
