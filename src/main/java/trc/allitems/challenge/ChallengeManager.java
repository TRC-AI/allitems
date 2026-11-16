package trc.allitems.challenge;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import trc.allitems.TRCAddon;
import trc.allitems.network.ChallengeSync;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChallengeManager {
    public static final AttachmentType<@NotNull ChallengeData> CHALLENGE = AttachmentRegistry.create(
            TRCAddon.id("challenge"),
            builder -> builder.persistent(ChallengeData.CODEC).copyOnDeath());

    private ChallengeManager() {}

    public static @Nullable ChallengeData get(@NotNull ServerPlayer player) { return player.getAttached(CHALLENGE); }

    public static boolean hasChallenge(@NotNull ServerPlayer player) { return get(player) != null; }

    public static @NotNull Result start(@NotNull ServerPlayer player, @NotNull ChallengeMode mode) {
        if (hasChallenge(player)) return Result.fail("A challenge is already started. Use /allitems reset first.");

        ChallengeData data = new ChallengeData(mode);
        player.setAttached(CHALLENGE, data);

        MinecraftServer server = player.level().getServer();
        if (mode == ChallengeMode.ORDER) ChallengeTracker.advanceTarget(server, player, data);

        ChallengeSync.state(player);
        return Result.ok("All Items Challenge started (" + mode.getSerializedName() + " mode, " + ItemPool.size(server) + " items).");
    }

    public static @NotNull Result pause(@NotNull ServerPlayer player) {
        ChallengeData data = get(player);
        if (data == null) return Result.fail("No challenge started.");
        if (data.finished) return Result.fail("The challenge is already complete.");
        if (data.paused) return Result.fail("The challenge is already paused.");

        data.paused = true;
        ChallengeSync.state(player);
        return Result.ok("Challenge paused.");
    }

    public static @NotNull Result resume(@NotNull ServerPlayer player) {
        ChallengeData data = get(player);
        if (data == null) return Result.fail("No challenge started.");
        if (data.finished) return Result.fail("The challenge is already complete.");
        if (!data.paused) return Result.fail("The challenge is not paused.");

        data.paused = false;
        ChallengeSync.state(player);
        return Result.ok("Challenge resumed.");
    }

    public static @NotNull Result reset(@NotNull ServerPlayer player) {
        if (!hasChallenge(player)) return Result.fail("No challenge started.");

        player.removeAttached(CHALLENGE);
        ChallengeSync.state(player);
        return Result.ok("Challenge reset.");
    }

    public static @NotNull Result skip(@NotNull ServerPlayer player) {
        ChallengeData data = get(player);
        if (data == null) return Result.fail("No challenge started.");
        if (data.mode != ChallengeMode.ORDER) return Result.fail("In free mode, name the item to skip: /allitems skip <item>");
        if (data.finished) return Result.fail("The challenge is already complete.");

        MinecraftServer server = player.level().getServer();
        if (data.target == null) ChallengeTracker.advanceTarget(server, player, data);
        if (data.target == null) return Result.fail("There is no item to skip.");

        ChallengeTracker.skip(server, player, data, data.target);
        ChallengeTracker.advanceTarget(server, player, data);

        ChallengeSync.state(player);
        return Result.ok(null);
    }

    public static @NotNull Result skipItem(@NotNull ServerPlayer player, @NotNull Identifier id) {
        ChallengeData data = get(player);
        if (data == null) return Result.fail("No challenge started.");
        if (data.mode != ChallengeMode.FREE) return Result.fail("In order mode, only the current item can be skipped: /allitems skip");
        if (data.finished) return Result.fail("The challenge is already complete.");

        MinecraftServer server = player.level().getServer();
        if (!ItemPool.of(server).contains(id)) return Result.fail("Unknown item: " + id);
        if (data.collected.contains(id)) return Result.fail("This item is already done.");

        ChallengeTracker.skip(server, player, data, id);
        if (ItemPool.complete(server, data)) ChallengeTracker.finish(server, player, data);

        ChallengeSync.state(player);
        return Result.ok(null);
    }

    public static @NotNull List<Identifier> skippable(@NotNull ServerPlayer player) {
        ChallengeData data = get(player);
        if (data == null || data.mode != ChallengeMode.FREE || data.finished) return List.of();

        return ItemPool.remaining(player.level().getServer(), data);
    }

    public record Result(boolean success, @Nullable String message) {
        static @NotNull Result ok(@Nullable String message) { return new Result(true, message); }

        static @NotNull Result fail(@NotNull String message) { return new Result(false, message); }
    }
}