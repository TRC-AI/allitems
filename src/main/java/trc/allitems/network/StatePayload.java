package trc.allitems.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import trc.allitems.TRCAddon;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public record StatePayload(boolean active, boolean order, boolean paused, boolean finished, int collected, int total,
                           int skipped, Optional<Identifier> target, long ticks) implements CustomPacketPayload {
    public static final Type<@NotNull StatePayload> TYPE = new Type<>(TRCAddon.id("state"));
    public static final StatePayload INACTIVE = new StatePayload(false, false, false, false, 0, 0, 0, Optional.empty(), 0L);
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull StatePayload> CODEC = StreamCodec.of(StatePayload::write, StatePayload::read);

    private static void write(@NotNull RegistryFriendlyByteBuf buf, @NotNull StatePayload payload) {
        buf.writeBoolean(payload.active);
        buf.writeBoolean(payload.order);
        buf.writeBoolean(payload.paused);
        buf.writeBoolean(payload.finished);
        buf.writeVarInt(payload.collected);
        buf.writeVarInt(payload.total);
        buf.writeVarInt(payload.skipped);
        buf.writeBoolean(payload.target.isPresent());
        payload.target.ifPresent(buf::writeIdentifier);
        buf.writeVarLong(payload.ticks);
    }

    private static @NotNull StatePayload read(@NotNull RegistryFriendlyByteBuf buf) {
        boolean active = buf.readBoolean();
        boolean order = buf.readBoolean();
        boolean paused = buf.readBoolean();
        boolean finished = buf.readBoolean();
        int collected = buf.readVarInt();
        int total = buf.readVarInt();
        int skipped = buf.readVarInt();
        Optional<Identifier> target = buf.readBoolean() ? Optional.of(buf.readIdentifier()) : Optional.empty();
        long ticks = buf.readVarLong();

        return new StatePayload(active, order, paused, finished, collected, total, skipped, target, ticks);
    }

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return TYPE; }
}