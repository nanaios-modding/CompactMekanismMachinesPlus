package com.nanaios.CompactMekanismMachinesPlus.common.network;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactGuiButtonPress;
import mekanism.common.network.BasePacketHandler;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import net.minecraftforge.network.simple.SimpleChannel;

public class CompactPlusPacketHandler extends BasePacketHandler {
    private final SimpleChannel netHandler = createChannel(CompactMekanismMachinesPlus.rl(CompactMekanismMachinesPlus.MODID), CompactMekanismMachinesPlus.instance.versionNumber);

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        registerClientToServer(PacketCompactGuiButtonPress.class, PacketCompactGuiButtonPress::decode);
        registerClientToServer(PacketGeneratorsGuiInteract.class, PacketGeneratorsGuiInteract::decode);
        //Server to client messages
    }
}
