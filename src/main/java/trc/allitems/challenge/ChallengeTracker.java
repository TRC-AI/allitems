package trc.allitems.challenge;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import trc.allitems.TRCAddon;
import trc.allitems.network.ChallengeSync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public final class ChallengeTracker {
    private static final Identifier ADVANCEMENT = TRCAddon.id("all_items");
    private static final EquipmentSlot[] WORN_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND
    };
    private static final int SYNC_INTERVAL = 20;

    private ChallengeTracker() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ChallengeTracker::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> ItemPool.clear());
    }

    private static void tick(@NotNull MinecraftServer server) {
        boolean syncTick = server.getTickCount() % SYNC_INTERVAL == 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ChallengeData data = ChallengeManager.get(player);
            if (data == null || !data.running()) continue;

            data.ticks++;

            boolean changed = data.mode == ChallengeMode.ORDER ? tickOrder(server, player, data) : tickFree(server, player, data);
            if (changed || syncTick) ChallengeSync.state(player);
        }
    }

    private static boolean tickOrder(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull ChallengeData data) {
        if (data.target == null || !ItemPool.of(server).contains(data.target) || data.collected.contains(data.target)) {
            return advanceTarget(server, player, data);
        }

        for (ItemStack stack : heldStacks(player)) {
            if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(data.target)) continue;

            data.collected.add(data.target);
            ChallengeSync.collected(player, data.target);
            advanceTarget(server, player, data);
            return true;
        }

        return false;
    }

    private static boolean tickFree(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull ChallengeData data) {
        Set<Identifier> pool = ItemPool.of(server);
        boolean changed = false;

        for (ItemStack stack : heldStacks(player)) {
            if (stack.isEmpty()) continue;

            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!pool.contains(id) || !data.collected.add(id)) continue;

            ChallengeSync.collected(player, id);
            changed = true;
        }

        if (changed && ItemPool.complete(server, data)) finish(server, player, data);

        return changed;
    }

    private static @NotNull List<ItemStack> heldStacks(@NotNull ServerPlayer player) {
        List<ItemStack> stacks = new ArrayList<>(player.getInventory().getNonEquipmentItems());

        for (EquipmentSlot slot : WORN_SLOTS) stacks.add(player.getItemBySlot(slot));
        stacks.add(player.containerMenu.getCarried());

        return stacks;
    }

    static boolean advanceTarget(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull ChallengeData data) {
        List<Identifier> remaining = ItemPool.remaining(server, data);

        if (remaining.isEmpty()) {
            data.target = null;
            finish(server, player, data);
            return true;
        }

        Identifier previous = data.target;
        data.target = remaining.get(player.getRandom().nextInt(remaining.size()));

        return !data.target.equals(previous);
    }

    static void skip(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull ChallengeData data, @NotNull Identifier id) {
        data.collected.add(id);
        data.skipped.add(id);

        player.sendSystemMessage(Component.literal("⏭ Skipped ").withStyle(ChatFormatting.GOLD)
                .append(itemName(id).copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" (" + ItemPool.count(server, data.collected) + "/" + ItemPool.size(server) + ")")
                        .withStyle(ChatFormatting.GRAY)));
    }

    static void finish(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull ChallengeData data) {
        if (data.finished) return;

        data.finished = true;
        data.target = null;

        AdvancementHolder advancement = server.getAdvancements().get(ADVANCEMENT);
        if (advancement != null) {
            for (String criterion : advancement.value().criteria().keySet()) player.getAdvancements().award(advancement, criterion);
        }

        player.sendSystemMessage(Component.literal("All Items Challenge complete!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Time: " + ChallengeTime.format(data.ticks)
                + " | Items: " + ItemPool.size(server)
                + " | Skipped: " + data.skipped.size()).withStyle(ChatFormatting.YELLOW));

        ChallengeSync.finished(player);
        ChallengeSync.state(player);
    }

    private static @NotNull Component itemName(@NotNull Identifier id) {
        return BuiltInRegistries.ITEM.getOptional(id)
                .map(item -> item.getDefaultInstance().getHoverName())
                .orElse(Component.literal(id.toString()));
    }
}