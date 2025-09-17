package com.nanaios.CompactMekanismMachinesPlus.common.network.to_server;

import com.nanaios.CompactMekanismMachinesPlus.common.CompactMekanismMachinesPlus;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactFusionReactor;
import com.nanaios.CompactMekanismMachinesPlus.common.tile.TileEntityCompactThermoelectricBoiler;
import io.netty.buffer.ByteBuf;
import mekanism.common.MekanismLang;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

//TODO 1.21.1対応

public record PacketCompactPlusTileButtonPress(ClickedCompactPlusTileButton buttonClicked, BlockPos pos) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketCompactPlusTileButtonPress> TYPE = new CustomPacketPayload.Type<>(CompactMekanismMachinesPlus.rl("tile_button"));
    public static final StreamCodec<ByteBuf, PacketCompactPlusTileButtonPress> STREAM_CODEC = StreamCodec.composite(
            ClickedCompactPlusTileButton.STREAM_CODEC, PacketCompactPlusTileButtonPress::buttonClicked,
            BlockPos.STREAM_CODEC, PacketCompactPlusTileButtonPress::pos,
            PacketCompactPlusTileButtonPress::new
    );

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        //If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), pos);
        MenuProvider provider = buttonClicked.getProvider(tile);
        if (provider != null) {
            player.openMenu(provider, buf -> {
                buf.writeBlockPos(pos);
                buttonClicked.encodeExtraData(buf, tile);
            });
        }
    }

    @Override
    public @NotNull Type<PacketCompactPlusTileButtonPress> type() {
        return TYPE;
    }

    public enum ClickedCompactPlusTileButton {
        TAB_HEAT(tile -> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_HEAT.getProvider(GeneratorsLang.FUSION_REACTOR, tile)),
        TAB_FUEL(tile-> CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_FUEL.getProvider(GeneratorsLang.FUSION_REACTOR, tile)),
        TAB_STATS(tile -> {
            if(tile instanceof TileEntityCompactFusionReactor) {
                return CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_STATS.getProvider(GeneratorsLang.FUSION_REACTOR, tile);
            } else if(tile instanceof TileEntityCompactThermoelectricBoiler) {
                return CompactPlusContainerTypes.BOILER_STATS.getProvider(MekanismLang.BOILER, tile);
            }
            return null;
        }),
        TAB_CONFIG(tile -> CompactPlusContainerTypes.BOILER_CONFIG.getProvider(MekanismLang.BOILER, tile)),
        TAB_MAIN(tile -> CompactPlusContainerTypes.COMPACT_THERMOELECTRIC_BOILER.getProvider(MekanismLang.BOILER, tile));

        public static final IntFunction<ClickedCompactPlusTileButton> BY_ID = ByIdMap.continuous(ClickedCompactPlusTileButton::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ClickedCompactPlusTileButton> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClickedCompactPlusTileButton::ordinal);


        private final Function<TileEntityMekanism, MenuProvider> providerFromTile;
        @Nullable
        private final BiConsumer<RegistryFriendlyByteBuf, TileEntityMekanism> extraEncodingData;

        ClickedCompactPlusTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile) {
            this(providerFromTile, null);
        }

        ClickedCompactPlusTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile,
                                     @Nullable BiConsumer<RegistryFriendlyByteBuf, TileEntityMekanism> extraEncodingData) {
            this.providerFromTile = providerFromTile;
            this.extraEncodingData = extraEncodingData;
        }

        public MenuProvider getProvider(TileEntityMekanism tile) {
            return providerFromTile.apply(tile);
        }

        private void encodeExtraData(RegistryFriendlyByteBuf buffer, TileEntityMekanism tile) {
            if (extraEncodingData != null) {
                extraEncodingData.accept(buffer, tile);
            }
        }
    }
}
