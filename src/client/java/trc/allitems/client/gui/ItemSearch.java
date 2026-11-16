package trc.allitems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public final class ItemSearch {
    private static ClientLanguage english;

    private final Map<Item, Entry> entries = new HashMap<>();
    private final Map<String, String> modNames = new HashMap<>();

    public ItemSearch(@NotNull List<Item> items) {
        ClientLanguage language = english();
        for (Item item : items) entries.put(item, entry(item, language));
    }

    public @NotNull List<Item> filter(@NotNull List<Item> items, @NotNull String rawQuery) {
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return items;

        String[] tokens = query.split("\\s+");
        List<Scored> hits = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            Entry entry = entries.get(item);
            if (entry == null) continue;

            double score = score(entry, query, tokens);
            if (score > 0) hits.add(new Scored(item, score, i));
        }

        hits.sort(Comparator.comparingDouble(Scored::score).reversed().thenComparingInt(Scored::order));
        return hits.stream().map(Scored::item).toList();
    }

    private static @NotNull ClientLanguage english() {
        if (english == null) english = ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(), List.of("en_us"), false);

        return english;
    }

    private @NotNull Entry entry(@NotNull Item item, @NotNull ClientLanguage language) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        Component display = item.getDefaultInstance().getHoverName();

        String name = display.getContents() instanceof TranslatableContents translatable
                ? language.getOrDefault(translatable.getKey(), display.getString())
                : display.getString();
        name = name.toLowerCase(Locale.ROOT);

        String modName = modNames.computeIfAbsent(id.getNamespace(), namespace -> FabricLoader.getInstance().getModContainer(namespace)
                .map(container -> container.getMetadata().getName()).orElse(namespace).toLowerCase(Locale.ROOT));
        List<String> tags = item.builtInRegistryHolder().tags().map(tag -> tag.location().toString()).toList();

        return new Entry(id.toString(), id.getNamespace(), modName, name, name.split("[^\\p{L}0-9]+"),
                name.replaceAll("[^a-z0-9]", ""), id.getPath().split("[_/]"), tags);
    }

    private static double score(@NotNull Entry entry, @NotNull String query, @NotNull String[] tokens) {
        double total = 0;

        for (String token : tokens) {
            double score;
            if (token.startsWith("@")) score = matchMod(entry, token.substring(1));
            else if (token.startsWith("#")) score = matchTag(entry, token.substring(1));
            else score = matchText(entry, token);

            if (score <= 0) return 0;
            total += score;
        }

        if (entry.name.equals(query)) total += 10;
        else if (entry.name.startsWith(query)) total += 3;

        return total;
    }

    private static double matchMod(@NotNull Entry entry, @NotNull String token) {
        if (token.isEmpty()) return 0.1;

        return entry.namespace.startsWith(token) || entry.modName.contains(token) ? 1 : 0;
    }

    private static double matchTag(@NotNull Entry entry, @NotNull String token) {
        if (token.isEmpty()) return entry.tags.isEmpty() ? 0 : 0.1;

        double best = 0;

        for (String tag : entry.tags) {
            String path = tag.substring(tag.indexOf(':') + 1);
            if (tag.equals(token) || path.equals(token)) return 2;
            if (tag.contains(token)) best = 1;
        }

        return best;
    }

    private static double matchText(@NotNull Entry entry, @NotNull String token) {
        double best = 0;

        for (String word : entry.nameWords) {
            if (word.equals(token)) return 4;
            if (word.startsWith(token)) best = Math.max(best, 3);
        }

        for (String word : entry.idWords) {
            if (word.equals(token)) best = Math.max(best, 3);
            else if (word.startsWith(token)) best = Math.max(best, 2.5);
        }

        if (best > 0) return best;
        if (entry.compactName.contains(token) || entry.id.contains(token) || entry.name.contains(token)) return 2;
        if (token.length() < 4) return 0;

        int allowed = token.length() >= 7 ? 2 : 1;

        for (String[] words : new String[][]{entry.nameWords, entry.idWords}) {
            for (String word : words) {
                if (word.isEmpty()) continue;

                if (distance(token, word) <= allowed) best = Math.max(best, 1);
                else if (word.length() > token.length() && distance(token, word.substring(0, token.length())) <= allowed) best = Math.max(best, 0.8);
            }
        }

        return best;
    }

    private static int distance(@NotNull String a, @NotNull String b) {
        if (Math.abs(a.length() - b.length()) > 2) return 3;

        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) d[i][0] = i;
        for (int j = 0; j <= b.length(); j++) d[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i][j - 1] + 1, d[i - 1][j] + 1), d[i - 1][j - 1] + cost);

                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }

        return d[a.length()][b.length()];
    }

    private record Entry(String id, String namespace, String modName, String name, String[] nameWords, String compactName,
                         String[] idWords, List<String> tags) {}

    private record Scored(Item item, double score, int order) {}
}
