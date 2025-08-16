package com.nanaios.CompactMekanismMachinesPlus.common.network.to_server;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.BiFunction;

public class PacketCompactGuiButtonPress implements IMekanismPacket {

    private final ClickedCompactTileButton tileButton;
    private final int extra;
    private final BlockPos tilePosition;

    public PacketCompactGuiButtonPress(ClickedCompactTileButton buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketCompactGuiButtonPress(ClickedCompactTileButton buttonClicked, BlockPos tilePosition, int extra) {
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {//If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), tilePosition);
            if (tile != null) {
                MenuProvider provider = tileButton.getProvider(tile, extra);
                if (provider != null) {
                    //Ensure valid data
                    NetworkHooks.openScreen(player, provider, buf -> {
                        buf.writeBlockPos(tilePosition);
                        buf.writeVarInt(extra);
                    });
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(tileButton);
        buffer.writeBlockPos(tilePosition);
        buffer.writeVarInt(extra);
    }

    public static PacketCompactGuiButtonPress decode(FriendlyByteBuf buffer) {
        return new PacketCompactGuiButtonPress(buffer.readEnum(ClickedCompactTileButton.class), buffer.readBlockPos(), buffer.readVarInt());
    }

    public enum ClickedCompactTileButton {
        TAB_HEAT((tile, extra) -> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_HEAT.getProvider(GeneratorsLang.FUSION_REACTOR, tile)),
        TAB_FUEL((tile, extra) -> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_FUEL.getProvider(GeneratorsLang.FUSION_REACTOR, tile)),
        TAB_STATS((tile, extra) -> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_STATS.getProvider(GeneratorsLang.FUSION_REACTOR, tile));

        private final BiFunction<TileEntityMekanism, Integer, MenuProvider> providerFromTile;

        ClickedCompactTileButton(BiFunction<TileEntityMekanism, Integer, MenuProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public MenuProvider getProvider(TileEntityMekanism tile, int extra) {
            return providerFromTile.apply(tile, extra);
        }
    }
}
