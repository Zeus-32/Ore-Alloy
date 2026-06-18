package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import eu.zunix.ore_and_alloy.integration.IntegrationMaterialRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class RecipeAliasMapBuilder {
    private static final String COMMON_TAG_NAMESPACE = "c";
    private static final Map<ResourceLocation, ResourceLocation> EXPLICIT_ITEM_ALIASES = Map.of(
            ResourceLocation.fromNamespaceAndPath("ae2", "silicon"),
            ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "silicon")
    );

    private static final Pattern INVALID_MATERIAL_CHARS = Pattern.compile("[^a-z0-9_]");
    private static final Pattern DUPLICATED_UNDERSCORES = Pattern.compile("_+");

    private static final Comparator<MaterialAliasKey> MATERIAL_KEY_ORDER = Comparator
            .comparing(MaterialAliasKey::material)
            .thenComparing(MaterialAliasKey::form);

    private RecipeAliasMapBuilder() {}

    public static Map<Item, Item> buildAliasMap() {
        return buildAliasAnalysis().aliasToCanonical();
    }

    public static RecipeAliasBuildResult buildAliasAnalysis() {
        Map<MaterialAliasKey, Set<Item>> groups = collectMaterialGroups();
        Map<Item, List<Item>> aliasCandidates = new IdentityHashMap<>();
        Map<MaterialAliasKey, List<Item>> duplicateGroups = new LinkedHashMap<>();
        Map<MaterialAliasKey, Item> canonicalByGroup = new LinkedHashMap<>();

        List<Map.Entry<MaterialAliasKey, Set<Item>>> groupEntries = new ArrayList<>(groups.entrySet());
        groupEntries.sort(Map.Entry.comparingByKey(MATERIAL_KEY_ORDER));

        for (Map.Entry<MaterialAliasKey, Set<Item>> entry : groupEntries) {
            List<Item> items = new ArrayList<>(entry.getValue());
            if (items.size() < 2) continue;
            items.sort(Comparator.comparing(item -> itemId(item).toString()));

            duplicateGroups.put(entry.getKey(), List.copyOf(items));

            Item canonical = selectCanonicalItem(entry.getKey(), items);
            canonicalByGroup.put(entry.getKey(), canonical);
            for (Item item : items) {
                if (item == canonical) continue;
                aliasCandidates.computeIfAbsent(item, ignored -> new ArrayList<>()).add(canonical);
            }
        }

        List<Map.Entry<Item, List<Item>>> candidateEntries = new ArrayList<>(aliasCandidates.entrySet());
        candidateEntries.sort(Comparator.comparing(entry -> itemId(entry.getKey()).toString()));

        Map<Item, Item> aliasToCanonical = new IdentityHashMap<>();
        for (Map.Entry<Item, List<Item>> entry : candidateEntries) {
            Item alias = entry.getKey();
            Item canonical = UnificationPriorityRules.pickBestCanonicalCandidate(entry.getValue());
            if (canonical == Items.AIR) {
                canonical = alias;
            }
            if (canonical == alias) continue;
            if (isInternalOreAndAlloyAlias(alias, canonical)) continue;
            aliasToCanonical.put(alias, canonical);
        }
        addExplicitItemAliases(aliasToCanonical);

        Map<Item, List<Item>> canonicalCandidatesSnapshot = new IdentityHashMap<>();
        for (Map.Entry<Item, List<Item>> entry : candidateEntries) {
            List<Item> candidates = new ArrayList<>(entry.getValue());
            candidates.sort(Comparator.comparing(item -> itemId(item).toString()));
            canonicalCandidatesSnapshot.put(entry.getKey(), List.copyOf(candidates));
        }

        return new RecipeAliasBuildResult(
                Map.copyOf(aliasToCanonical),
                Collections.unmodifiableMap(duplicateGroups),
                Collections.unmodifiableMap(canonicalByGroup),
                Collections.unmodifiableMap(canonicalCandidatesSnapshot)
        );
    }

    private static void addExplicitItemAliases(Map<Item, Item> aliasToCanonical) {
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : EXPLICIT_ITEM_ALIASES.entrySet()) {
            Item alias = BuiltInRegistries.ITEM.get(entry.getKey());
            Item canonical = BuiltInRegistries.ITEM.get(entry.getValue());
            if (alias == Items.AIR || canonical == Items.AIR || alias == canonical) continue;
            aliasToCanonical.put(alias, canonical);
        }
    }

    public static Map<Item, Item> buildAliasMapSnapshot() {
        Map<Item, Item> aliasMap = buildAliasMap();
        List<Map.Entry<Item, Item>> entries = new ArrayList<>(aliasMap.entrySet());
        entries.sort(Comparator.comparing(entry -> itemId(entry.getKey()).toString()));

        Map<Item, Item> out = new LinkedHashMap<>();
        for (Map.Entry<Item, Item> entry : entries) {
            out.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(out);
    }

    private static Map<MaterialAliasKey, Set<Item>> collectMaterialGroups() {
        Map<MaterialAliasKey, Set<Item>> groups = new LinkedHashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ItemStack probe = item.getDefaultInstance();
            if (probe.isEmpty()) continue;

            List<MaterialAliasKey> tagKeys = probe.getTags()
                    .map(TagKey::location)
                    .map(RecipeAliasMapBuilder::materialKeyFromCommonTag)
                    .flatMap(Optional::stream)
                    .sorted(MATERIAL_KEY_ORDER)
                    .toList();

            for (MaterialAliasKey key : tagKeys) {
                groups.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(item);
            }

            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                List<MaterialAliasKey> blockTagKeys = block.builtInRegistryHolder().tags()
                        .map(TagKey::location)
                        .map(RecipeAliasMapBuilder::materialKeyFromCommonTag)
                        .flatMap(Optional::stream)
                        .sorted(MATERIAL_KEY_ORDER)
                        .toList();
                for (MaterialAliasKey key : blockTagKeys) {
                    groups.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(item);
                }
            }

            ResourceLocation id = itemId(item);
            materialKeyFromItemId(id).ifPresent(key -> groups.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(item));
        }

        return groups;
    }

    public static Optional<MaterialAliasKey> materialKeyFromCommonTag(ResourceLocation tagId) {
        if (!COMMON_TAG_NAMESPACE.equals(tagId.getNamespace())) return Optional.empty();

        String path = tagId.getPath().toLowerCase(Locale.ROOT);
        int split = path.indexOf('/');
        if (split < 0) {
            Optional<String> bareForm = MaterialItemOrder.bareItemForm(path);
            if (bareForm.isEmpty() || !IntegrationMaterialRegistry.isMaterialEnabled(path)) {
                return Optional.empty();
            }
            return Optional.of(new MaterialAliasKey(bareForm.get(), MaterialItemOrder.canonicalMaterialToken(path)));
        }
        if (split == 0 || split >= path.length() - 1) return Optional.empty();

        String bucket = path.substring(0, split);
        String material = normalizeMaterial(path.substring(split + 1));
        String form = MaterialFormCatalog.FORM_BY_TAG_BUCKET.get(bucket);
        if (form == null || material.isBlank()) return Optional.empty();
        if (!IntegrationMaterialRegistry.isMaterialEnabled(material)) return Optional.empty();
        if ("raw".equals(form) || "crushed".equals(form)) {
            return Optional.empty();
        }
        return Optional.of(new MaterialAliasKey(form, normalizeMaterialForForm(material, form)));
    }

    public static Optional<MaterialAliasKey> materialKeyFromItemId(ResourceLocation id) {
        String path = normalizeMaterial(id.getPath());
        if (path.isBlank()) return Optional.empty();

        for (String form : MaterialFormCatalog.FORM_SUFFIX_PARSE_ORDER) {
            String canonicalForm = "sheet".equals(form) ? "plate" : form;

            String suffix = "_" + form;
            if (path.endsWith(suffix) && path.length() > suffix.length()) {
                String material = normalizeMaterial(path.substring(0, path.length() - suffix.length()));
                material = normalizeMaterialForForm(material, canonicalForm);
                if (!material.isBlank() && IntegrationMaterialRegistry.isMaterialEnabled(material)) {
                    return Optional.of(new MaterialAliasKey(canonicalForm, material));
                }
            }

            String prefix = form + "_";
            if (path.startsWith(prefix) && path.length() > prefix.length()) {
                String material = normalizeMaterial(path.substring(prefix.length()));
                material = normalizeMaterialForForm(material, canonicalForm);
                if (!material.isBlank() && IntegrationMaterialRegistry.isMaterialEnabled(material)) {
                    return Optional.of(new MaterialAliasKey(canonicalForm, material));
                }
            }
        }

        String storageSuffix = "_block";
        if (path.endsWith(storageSuffix) && path.length() > storageSuffix.length()) {
            String material = normalizeMaterial(path.substring(0, path.length() - storageSuffix.length()));
            if (!material.isBlank() && IntegrationMaterialRegistry.isMaterialEnabled(material)) {
                return Optional.of(new MaterialAliasKey("block", material));
            }
        }

        Optional<String> bareForm = MaterialItemOrder.bareItemForm(path);
        if (bareForm.isPresent()) {
            return Optional.of(new MaterialAliasKey(bareForm.get(), MaterialItemOrder.canonicalMaterialToken(path)));
        }

        return Optional.empty();
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static Item selectCanonicalItem(MaterialAliasKey key, List<Item> items) {
        ResourceLocation preferred = ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, preferredCanonicalPath(key));
        return UnificationPriorityRules.selectCanonicalItem(items, preferred.toString());
    }

    private static String preferredCanonicalPath(MaterialAliasKey key) {
        String form = "sheet".equals(key.form()) ? "plate" : key.form();
        String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(key.material());
        String preferredMaterial = MaterialItemOrder.preferredItemMaterialToken(canonicalMaterial);

        if (MaterialItemOrder.bareItemForm(canonicalMaterial).map(form::equals).orElse(false)) {
            return canonicalMaterial;
        }
        if ("raw".equals(form)) {
            return RawMaterialMappings.primaryRawVariantForMaterial(canonicalMaterial)
                    .map(variant -> "raw_" + variant)
                    .orElse("raw_" + preferredMaterial);
        }
        if ("crushed".equals(form)) {
            return RawMaterialMappings.primaryCrushedVariantForMaterial(canonicalMaterial)
                    .map(variant -> "crushed_" + variant)
                    .orElse("crushed_" + preferredMaterial);
        }
        return preferredMaterial + "_" + form;
    }

    private static String normalizeMaterial(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_')
                .replace('/', '_');
        normalized = INVALID_MATERIAL_CHARS.matcher(normalized).replaceAll("");
        normalized = DUPLICATED_UNDERSCORES.matcher(normalized).replaceAll("_");
        normalized = MaterialItemOrder.canonicalMaterialToken(trimUnderscores(normalized));
        return normalized;
    }

    private static String normalizeMaterialForForm(String material, String form) {
        String out = material;
        if ("crushed".equals(form)) {
            if (out.startsWith("raw_") && out.length() > "raw_".length()) {
                out = out.substring("raw_".length());
            }
            if (out.endsWith("_raw") && out.length() > "_raw".length()) {
                out = out.substring(0, out.length() - "_raw".length());
            }
        }
        if ("ore".equals(form)) {
            out = normalizeOreMaterialToken(out);
        }
        return out;
    }

    private static String normalizeOreMaterialToken(String materialToken) {
        String out = RawVariantCatalog.stripOreMaterialPrefix(materialToken);
        out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        return out;
    }

    private static String trimUnderscores(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '_') start++;
        while (end > start && value.charAt(end - 1) == '_') end--;
        return value.substring(start, end);
    }

    private static boolean isInternalOreAndAlloyAlias(Item alias, Item canonical) {
        ResourceLocation aliasId = itemId(alias);
        ResourceLocation canonicalId = itemId(canonical);
        return OreAndAlloy.MODID.equals(aliasId.getNamespace())
                && OreAndAlloy.MODID.equals(canonicalId.getNamespace());
    }
}
