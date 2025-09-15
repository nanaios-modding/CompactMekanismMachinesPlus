package com.nanaios.CompactMekanismMachinesPlus.common.network;

import com.nanaios.CompactMekanismMachinesPlus.common.network.to_server.PacketCompactPlusGuiInteract;
import mekanism.common.lib.Version;
import mekanism.common.network.BasePacketHandler;
import net.neoforged.bus.api.IEventBus;

public class CompactPlusPacketHandler extends BasePacketHandler {
    protected CompactPlusPacketHandler(IEventBus modEventBus, Version version) {
        super(modEventBus, version);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketCompactPlusGuiInteract.TYPE,PacketCompactPlusGuiInteract.STREAM_CODEC);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {

    }
}
