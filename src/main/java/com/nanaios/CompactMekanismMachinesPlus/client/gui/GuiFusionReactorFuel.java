package com.nanaios.CompactMekanismMachinesPlus.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiInteract.CompactPlusGuiInteraction;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiInteract;
import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.text.InputValidator;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFusionReactorFuel extends GuiFusionReactorInfo {

    private GuiTextField injectionRateField;

    public GuiFusionReactorFuel(EmptyTileContainer<TileEntityCompactFusionReactor> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiGasGauge(() -> tile.deuteriumTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 25, 64));
        addRenderableWidget(new GuiGasGauge(() -> tile.fuelTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 79, 50));
        addRenderableWidget(new GuiGasGauge(() -> tile.tritiumTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 133, 64));
        addRenderableWidget(new GuiProgress(() -> tile.isBurning(), ProgressType.SMALL_RIGHT, this, 47, 76));
        addRenderableWidget(new GuiProgress(() -> tile.isBurning(), ProgressType.SMALL_LEFT, this, 101, 76));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
        injectionRateField = addRenderableWidget(new GuiTextField(this, 98, 115, 26, 11));
        injectionRateField.changeFocus(true);
        injectionRateField.setInputValidator(InputValidator.DIGIT)
              .setEnterHandler(this::setInjection)
              .setMaxLength(2);
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        renderTitleText(poseStack);
        drawCenteredText(poseStack, GeneratorsLang.REACTOR_INJECTION_RATE.translate(tile.getInjectionRate()), 0, imageWidth, 35, titleTextColor());
        drawString(poseStack, GeneratorsLang.REACTOR_EDIT_RATE.translate(), 50, 117, titleTextColor());
        super.drawForegroundText(poseStack, mouseX, mouseY);
    }

    private void setInjection() {
        if (!injectionRateField.getText().isEmpty()) {
            CompactMekanismMachinesPlus.packetHandler().sendToServer(new PacketCompactPlusGuiInteract(CompactPlusGuiInteraction.INJECTION_RATE, tile, Integer.parseInt(injectionRateField.getText())));
            injectionRateField.setText("");
        }
    }
}