package com.nanaios.CompactMekanismMachinesPlus.common.network;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiButtonPress;
import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiInteract;
import mekanism.common.lib.Version;
import mekanism.common.network.BasePacketHandler;
import net.neoforged.bus.api.IEventBus;

//TODO 1.21.1対応

public class CompactPlusPacketHandler extends BasePacketHandler {
    private final SimpleChannel netHandler = createChannel(CompactMekanismMachinesPlus.rl(CompactMekanismMachinesPlus.MODID), CompactMekanismMachinesPlus.instance.versionNumber);

    protected CompactPlusPacketHandler(IEventBus modEventBus, Version version) {
        super(modEventBus, version);
    }

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        registerClientToServer(PacketCompactPlusGuiButtonPress.class, PacketCompactPlusGuiButtonPress::decode);
        registerClientToServer(PacketCompactPlusGuiInteract.class, PacketCompactPlusGuiInteract::decode);
        //Server to client messages
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {

    }
}
