package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class UnificationPriorityRules {
    private static final List<String> DEFAULT_GLOBAL_NAMESPACE_PRIORITY = List.of(
            OreAndAlloy.MODID,
            "minecraft"
    );

    private static volatile List<String> globalNamespacePriority = DEFAULT_GLOBAL_NAMESPACE_PRIORITY;

    private static final Map<String, Integer> MOD_PRIORITY_OVERRIDES = new ConcurrentHashMap<>();
    private static final Map<String, String> MATERIAL_NAMESPACE_OVERRIDES = new ConcurrentHashMap<>();
    private static final Map<MaterialAliasKey, String> ITEM_CANONICAL_OVERRIDES = new ConcurrentHashMap<>();

    private static final Comparator<Item> BASE_CANONICAL_COMPARATOR = Comparator
            .comparingInt((Item item) -> modPriorityValue(itemId(item).getNamespace()))
            .thenComparingInt(item -> globalNamespaceRank(itemId(item).getNamespace()))
            .thenComparing(item -> itemId(item).toString());

    private UnificationPriorityRules() {}

    public static Item selectCanonicalItem(
            MaterialAliasKey key,
            List<Item> candidates,
            String defaultCanonicalItemId
    ) {
        if (candidates.isEmpty()) {
            return Items.AIR;
        }

        Optional<Item> explicit = resolveExplicitItemOverride(key, candidates);
        if (explicit.isPresent()) {
            return explicit.get();
        }

        Optional<Item> implicit = resolveItemIdMatch(defaultCanonicalItemId, candidates);
        if (implicit.isPresent()) {
            return implicit.get();
        }

        Optional<Item> materialScoped = resolveMaterialPreferredNamespace(key.material(), candidates);
        if (materialScoped.isPresent()) {
            return materialScoped.get();
        }

        return candidates.stream().min(BASE_CANONICAL_COMPARATOR).orElse(candidates.getFirst());
    }

    public static Item pickBestCanonicalCandidate(List<Item> candidates) {
        if (candidates.isEmpty()) return Items.AIR;
        return candidates.stream().min(BASE_CANONICAL_COMPARATOR).orElse(candidates.getFirst());
    }

    public static void resetRuntimeOverrides() {
        globalNamespacePriority = DEFAULT_GLOBAL_NAMESPACE_PRIORITY;
        MOD_PRIORITY_OVERRIDES.clear();
        MATERIAL_NAMESPACE_OVERRIDES.clear();
        ITEM_CANONICAL_OVERRIDES.clear();
    }

    public static void setGlobalNamespacePriority(List<String> namespaces) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String namespace : namespaces) {
            String token = normalizeNamespace(namespace);
            if (token.isBlank()) continue;
            normalized.add(token);
        }
        if (normalized.isEmpty()) {
            globalNamespacePriority = DEFAULT_GLOBAL_NAMESPACE_PRIORITY;
            return;
        }
        globalNamespacePriority = List.copyOf(normalized);
    }

    public static void setModPriority(String namespace, int priority) {
        String normalized = normalizeNamespace(namespace);
        if (normalized.isBlank()) return;
        MOD_PRIORITY_OVERRIDES.put(normalized, priority);
    }

    public static void clearModPriority(String namespace) {
        String normalized = normalizeNamespace(namespace);
        if (normalized.isBlank()) return;
        MOD_PRIORITY_OVERRIDES.remove(normalized);
    }

    public static void setMaterialPreferredNamespace(String material, String namespace) {
        String normalizedMaterial = normalizeMaterial(material);
        String normalizedNamespace = normalizeNamespace(namespace);
        if (normalizedMaterial.isBlank() || normalizedNamespace.isBlank()) return;
        MATERIAL_NAMESPACE_OVERRIDES.put(normalizedMaterial, normalizedNamespace);
    }

    public static void clearMaterialPreferredNamespace(String material) {
        String normalizedMaterial = normalizeMaterial(material);
        if (normalizedMaterial.isBlank()) return;
        MATERIAL_NAMESPACE_OVERRIDES.remove(normalizedMaterial);
    }

    public static void setCanonicalItemOverride(String form, String material, String itemId) {
        String normalizedForm = normalizeForm(form);
        String normalizedMaterial = normalizeMaterial(material);
        if (normalizedForm.isBlank() || normalizedMaterial.isBlank()) return;

        ResourceLocation resolved = resolveItemId(itemId);
        if (resolved == null) return;
        ITEM_CANONICAL_OVERRIDES.put(new MaterialAliasKey(normalizedForm, normalizedMaterial), resolved.toString());
    }

    public static void clearCanonicalItemOverride(String form, String material) {
        String normalizedForm = normalizeForm(form);
        String normalizedMaterial = normalizeMaterial(material);
        if (normalizedForm.isBlank() || normalizedMaterial.isBlank()) return;
        ITEM_CANONICAL_OVERRIDES.remove(new MaterialAliasKey(normalizedForm, normalizedMaterial));
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("globalNamespaces", globalNamespacePriority);
        out.put("modPriorities", Map.copyOf(MOD_PRIORITY_OVERRIDES));
        out.put("materialNamespaces", Map.copyOf(MATERIAL_NAMESPACE_OVERRIDES));
        out.put("itemOverrides", Map.copyOf(ITEM_CANONICAL_OVERRIDES));
        return out;
    }

    private static Optional<Item> resolveExplicitItemOverride(MaterialAliasKey key, List<Item> candidates) {
        String overrideId = ITEM_CANONICAL_OVERRIDES.get(key);
        return resolveItemIdMatch(overrideId, candidates);
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

    private static Optional<Item> resolveMaterialPreferredNamespace(String material, List<Item> candidates) {
        String normalized = normalizeMaterial(material);
        String preferredNamespace = MATERIAL_NAMESPACE_OVERRIDES.get(normalized);
        if (preferredNamespace == null || preferredNamespace.isBlank()) return Optional.empty();

        List<Item> matching = new ArrayList<>();
        for (Item item : candidates) {
            if (preferredNamespace.equals(itemId(item).getNamespace())) {
                matching.add(item);
            }
        }
        if (matching.isEmpty()) return Optional.empty();
        return Optional.of(matching.stream().min(BASE_CANONICAL_COMPARATOR).orElse(matching.getFirst()));
    }

    private static int modPriorityValue(String namespace) {
        Integer explicit = MOD_PRIORITY_OVERRIDES.get(namespace);
        return explicit != null ? explicit : Integer.MAX_VALUE;
    }

    private static int globalNamespaceRank(String namespace) {
        List<String> order = globalNamespacePriority;
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).equals(namespace)) return i;
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

    private static String normalizeNamespace(String token) {
        return normalizeToken(token);
    }

    private static String normalizeForm(String token) {
        String normalized = normalizeToken(token);
        if ("sheet".equals(normalized)) return "plate";
        return normalized;
    }

    private static String normalizeMaterial(String token) {
        return MaterialItemOrder.canonicalMaterialToken(normalizeToken(token));
    }

    private static String normalizeToken(String token) {
        if (token == null) return "";
        return token.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}
