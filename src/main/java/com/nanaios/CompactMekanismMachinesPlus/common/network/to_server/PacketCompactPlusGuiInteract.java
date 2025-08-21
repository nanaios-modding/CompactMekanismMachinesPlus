package com.nanaios.CompactMekanismMachinesPlus.common.network.to_server;

import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import mekanism.api.functions.TriConsumer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketCompactPlusGuiInteract implements IMekanismPacket {

    private final CompactPlusGuiInteraction interaction;
    private final BlockPos tilePosition;
    private final double extra;

    public PacketCompactPlusGuiInteract(CompactPlusGuiInteraction interaction, BlockEntity tile) {
        this(interaction, tile.getBlockPos());
    }

    public PacketCompactPlusGuiInteract(CompactPlusGuiInteraction interaction, BlockEntity tile, double extra) {
        this(interaction, tile.getBlockPos(), extra);
    }

    public PacketCompactPlusGuiInteract(CompactPlusGuiInteraction interaction, BlockPos tilePosition) {
        this(interaction, tilePosition, 0);
    }

    public PacketCompactPlusGuiInteract(CompactPlusGuiInteraction interaction, BlockPos tilePosition, double extra) {
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), tilePosition);
            if (tile != null) {
                interaction.consume(tile, player, extra);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(interaction);
        buffer.writeBlockPos(tilePosition);
        buffer.writeDouble(extra);
    }

    public static PacketCompactPlusGuiInteract decode(FriendlyByteBuf buffer) {
        return new PacketCompactPlusGuiInteract(buffer.readEnum(CompactPlusGuiInteraction.class), buffer.readBlockPos(), buffer.readDouble());
    }

    public enum CompactPlusGuiInteraction {
        INJECTION_RATE((tile, player, extra) -> {
            if (tile instanceof TileEntityCompactFusionReactor reactorBlock) {
                reactorBlock.setInjectionRateFromPacket((int) Math.round(extra));
            }
        }),
        SUPER_HEATING_ELEMENTS((tile, player, extra) -> {
            if (tile instanceof TileEntityCompactThermoelectricBoiler boiler) {
                boiler.setSuperHeatingElements((int) Math.round(extra));
            }
        }),
        DISPERSERS_Y((tile, player, extra) -> {
            if (tile instanceof TileEntityCompactThermoelectricBoiler boiler) {
                boiler.setDispersersY((int) Math.round(extra));
            }
        });

        private final TriConsumer<TileEntityMekanism, Player, Double> consumerForTile;

        CompactPlusGuiInteraction(TriConsumer<TileEntityMekanism, Player, Double> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, double extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}
