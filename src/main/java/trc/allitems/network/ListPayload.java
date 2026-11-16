package trc.allitems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import trc.allitems.TRCAddon;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public record ListPayload(List<Identifier> collected, List<Identifier> skipped) implements CustomPacketPayload {
    public static final Type<@NotNull ListPayload> TYPE = new Type<>(TRCAddon.id("list"));
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull ListPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ListPayload::collected,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ListPayload::skipped,
            ListPayload::new);

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return TYPE; }
}