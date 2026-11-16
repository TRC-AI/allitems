package trc.allitems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import trc.allitems.TRCAddon;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public record CollectedPayload(Optional<Identifier> item, int collected, int total, boolean finished) implements CustomPacketPayload {
    public static final Type<@NotNull CollectedPayload> TYPE = new Type<>(TRCAddon.id("collected"));
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull CollectedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), CollectedPayload::item,
            ByteBufCodecs.VAR_INT, CollectedPayload::collected,
            ByteBufCodecs.VAR_INT, CollectedPayload::total,
            ByteBufCodecs.BOOL, CollectedPayload::finished,
            CollectedPayload::new);

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return TYPE; }
}