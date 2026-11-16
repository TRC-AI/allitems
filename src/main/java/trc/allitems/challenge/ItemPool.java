package trc.allitems.challenge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class ItemPool {
    private static MinecraftServer cachedFor;
    private static Set<Identifier> cached = Set.of();

    private ItemPool() {}

    public static @NotNull Set<Identifier> of(@NotNull MinecraftServer server) {
        if (cachedFor != server) {
            cached = build(server.getWorldData().enabledFeatures());
            cachedFor = server;
        }

        return cached;
    }

    public static void clear() {
        cachedFor = null;
        cached = Set.of();
    }

    public static int size(@NotNull MinecraftServer server) { return of(server).size(); }

    public static int count(@NotNull MinecraftServer server, @NotNull Collection<Identifier> ids) {
        Set<Identifier> pool = of(server);
        int count = 0;

        for (Identifier id : ids) if (pool.contains(id)) count++;

        return count;
    }

    public static boolean complete(@NotNull MinecraftServer server, @NotNull ChallengeData data) {
        return count(server, data.collected) >= size(server);
    }

    public static @NotNull List<Identifier> remaining(@NotNull MinecraftServer server, @NotNull ChallengeData data) {
        List<Identifier> remaining = new ArrayList<>();

        for (Identifier id : of(server)) if (!data.collected.contains(id)) remaining.add(id);

        return remaining;
    }

    private static @NotNull @UnmodifiableView Set<Identifier> build(@NotNull FeatureFlagSet features) {
        Set<Identifier> ids = new LinkedHashSet<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || !item.isEnabled(features)) continue;

            ids.add(BuiltInRegistries.ITEM.getKey(item));
        }

        return Collections.unmodifiableSet(ids);
    }
}