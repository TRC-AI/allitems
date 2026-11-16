package trc.allitems.network;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import trc.allitems.challenge.ChallengeData;
import trc.allitems.challenge.ChallengeManager;
import trc.allitems.challenge.ChallengeMode;
import trc.allitems.challenge.ItemPool;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public final class ChallengeSync {
    private ChallengeSync() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((listener, _, _) -> {
            ServerPlayer player = listener.player;

            if (!ServerPlayNetworking.canSend(player, StatePayload.TYPE)) {
                listener.disconnect(Component.literal("This server requires the All Items Challenge mod on the client."));
                return;
            }

            state(player);
        });
    }

    public static void state(@NotNull ServerPlayer player) {
        ChallengeData data = ChallengeManager.get(player);

        if (data == null) {
            ServerPlayNetworking.send(player, StatePayload.INACTIVE);
            return;
        }

        MinecraftServer server = player.level().getServer();

        ServerPlayNetworking.send(player, new StatePayload(true, data.mode == ChallengeMode.ORDER, data.paused, data.finished,
                ItemPool.count(server, data.collected), ItemPool.size(server), ItemPool.count(server, data.skipped),
                Optional.ofNullable(data.target), data.ticks));
    }

    public static void collected(@NotNull ServerPlayer player, @NotNull Identifier item) {
        ChallengeData data = ChallengeManager.get(player);
        if (data == null) return;

        MinecraftServer server = player.level().getServer();
        ServerPlayNetworking.send(player, new CollectedPayload(Optional.of(item), ItemPool.count(server, data.collected), ItemPool.size(server), false));
    }

    public static void finished(@NotNull ServerPlayer player) {
        int total = ItemPool.size(player.level().getServer());
        ServerPlayNetworking.send(player, new CollectedPayload(Optional.empty(), total, total, true));
    }

    public static void list(@NotNull ServerPlayer player) {
        state(player);

        ChallengeData data = ChallengeManager.get(player);
        ServerPlayNetworking.send(player, data == null
                ? new ListPayload(List.of(), List.of())
                : new ListPayload(List.copyOf(data.collected), List.copyOf(data.skipped)));
    }
}