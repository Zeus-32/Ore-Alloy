package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class UnificationPriorityRules {
    private static final List<String> DEFAULT_GLOBAL_NAMESPACE_PRIORITY = List.of(
            OreAndAlloy.MODID,
            "minecraft"
    );

    private static final Comparator<Item> BASE_CANONICAL_COMPARATOR = Comparator
            .comparingInt((Item item) -> globalNamespaceRank(itemId(item).getNamespace()))
            .thenComparing(item -> itemId(item).toString());

    private UnificationPriorityRules() {}

    public static Item selectCanonicalItem(
            List<Item> candidates,
            String defaultCanonicalItemId
    ) {
        if (candidates.isEmpty()) {
            return Items.AIR;
        }

        Optional<Item> implicit = resolveItemIdMatch(defaultCanonicalItemId, candidates);
        if (implicit.isPresent()) {
            return implicit.get();
        }

        return candidates.stream().min(BASE_CANONICAL_COMPARATOR).orElse(candidates.getFirst());
    }

    public static Item pickBestCanonicalCandidate(List<Item> candidates) {
        if (candidates.isEmpty()) return Items.AIR;
        return candidates.stream().min(BASE_CANONICAL_COMPARATOR).orElse(candidates.getFirst());
    }

    private static Optional<Item> resolveItemIdMatch(String itemId, List<Item> candidates) {
        ResourceLocation resolved = resolveItemId(itemId);
        if (resolved == null) return Optional.empty();

        for (Item item : candidates) {
            if (resolved.equals(itemId(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private static int globalNamespaceRank(String namespace) {
        for (int i = 0; i < DEFAULT_GLOBAL_NAMESPACE_PRIORITY.size(); i++) {
            if (DEFAULT_GLOBAL_NAMESPACE_PRIORITY.get(i).equals(namespace)) return i;
        }
        return 1_000 + Math.abs(namespace.hashCode() % 100);
    }

    private static ResourceLocation resolveItemId(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) return null;
        if (!normalized.contains(":")) {
            normalized = OreAndAlloy.MODID + ":" + normalized;
        }
        try {
            return ResourceLocation.parse(normalized);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

}
