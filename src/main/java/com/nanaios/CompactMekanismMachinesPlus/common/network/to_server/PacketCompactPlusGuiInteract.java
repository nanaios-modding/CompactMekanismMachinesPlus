package com.nanaios.CompactMekanismMachinesPlus.common.network.to_server;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import io.netty.buffer.ByteBuf;
import mekanism.api.functions.TriConsumer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

//TODO 1.21.1対応

public record PacketCompactPlusGuiInteract(CompactPlusGuiInteraction interaction, BlockPos tilePosition, double extra) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketCompactPlusGuiInteract> TYPE = new CustomPacketPayload.Type<>(CompactMekanismMachinesPlus.rl("gui_interact"));
    public static final StreamCodec<ByteBuf, PacketCompactPlusGuiInteract> STREAM_CODEC = StreamCodec.composite(
            CompactPlusGuiInteraction.STREAM_CODEC, PacketCompactPlusGuiInteract::interaction,
            BlockPos.STREAM_CODEC, PacketCompactPlusGuiInteract::tilePosition,
            ByteBufCodecs.DOUBLE, PacketCompactPlusGuiInteract::extra,
            PacketCompactPlusGuiInteract::new
    );

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

    public static PacketCompactPlusGuiInteract decode(FriendlyByteBuf buffer) {
        return new PacketCompactPlusGuiInteract(buffer.readEnum(CompactPlusGuiInteraction.class), buffer.readBlockPos(), buffer.readDouble());
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), tilePosition);
        if (tile != null) {
            interaction.consume(tile, player, extra);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum CompactPlusGuiInteraction {
        INJECTION_RATE((tile, player, extra) -> {
            if (tile instanceof TileEntityCompactFusionReactor reactorBlock) {
                reactorBlock.setInjectionRateFromPacket((int) Math.round(extra));
            }
        }),
        SUPER_HEATING_ELEMENTS((tile, player, extra) -> {
            if (tile instanceof TileEntityCompactThermoelectricBoiler boiler) {
                boiler.setSuperHeatingElementsFromPacket((int) Math.round(extra));
            }
        }),
        DISPERSERS_Y((tile, player, extra) -> {
            if (tile instanceof TileEntityCompactThermoelectricBoiler boiler) {
                boiler.setDispersersYFromPacket((int) Math.round(extra));
            }
        });

        private final TriConsumer<TileEntityMekanism, Player, Double> consumerForTile;

        public static final IntFunction<CompactPlusGuiInteraction> BY_ID = ByIdMap.continuous(CompactPlusGuiInteraction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, CompactPlusGuiInteraction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CompactPlusGuiInteraction::ordinal);


        CompactPlusGuiInteraction(TriConsumer<TileEntityMekanism, Player, Double> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, double extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}
