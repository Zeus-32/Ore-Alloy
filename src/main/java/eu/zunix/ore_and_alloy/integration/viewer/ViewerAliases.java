package eu.zunix.ore_and_alloy.integration.viewer;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ViewerAliases {
    private static final List<String> FORM_PARSE_ORDER = MaterialFormCatalog.FORM_SUFFIX_PARSE_ORDER;

    private ViewerAliases() {}

    public static Map<Item, Set<String>> resolveVisibleItemAliases(Set<Item> hiddenItems) {
        List<Item> visible = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (hiddenItems.contains(item)) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!OreAndAlloy.MODID.equals(id.getNamespace())) continue;
            visible.add(item);
        }
        visible.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));

        Map<Item, Set<String>> out = new LinkedHashMap<>();
        for (Item item : visible) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            Set<String> aliases = aliasesForItemId(id);
            if (!aliases.isEmpty()) out.put(item, aliases);
        }
        return Collections.unmodifiableMap(out);
    }

    public static Set<String> aliasesForItemId(ResourceLocation itemId) {
        if (!OreAndAlloy.MODID.equals(itemId.getNamespace())) return Set.of();

        String normalizedPath = ViewerAliasRuleEngine.normalizeToken(itemId.getPath());
        if (normalizedPath.isBlank()) return Set.of();

        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        ViewerAliasRuleEngine.addItemAliases(normalizedPath, aliases);

        parseMaterialKey(normalizedPath).ifPresent(material ->
                ViewerAliasRuleEngine.addFormAliases(material.formToken(), material.material(), aliases));

        return Collections.unmodifiableSet(aliases);
    }

    private static Optional<MaterialKey> parseMaterialKey(String path) {
        String storageSuffix = "_block";
        if (path.endsWith(storageSuffix) && path.length() > storageSuffix.length()) {
            String material = path.substring(0, path.length() - storageSuffix.length());
            if (!material.isBlank()) {
                return Optional.of(new MaterialKey(normalizeMaterialForForm(material, blockFormToken(material)), blockFormToken(material)));
            }
        }

        for (String token : MaterialFormCatalog.PREFIX_FORMS) {
            String prefix = token + "_";
            if (!path.startsWith(prefix) || path.length() <= prefix.length()) continue;
            String material = path.substring(prefix.length());
            if (!material.isBlank()) return Optional.of(new MaterialKey(normalizeMaterialForForm(material, token), token));
        }

        for (String token : FORM_PARSE_ORDER) {
            String suffix = "_" + token;
            if (!path.endsWith(suffix) || path.length() <= suffix.length()) continue;

            String material = path.substring(0, path.length() - suffix.length());
            if (!material.isBlank()) return Optional.of(new MaterialKey(normalizeMaterialForForm(material, token), token));

        }
        Optional<String> bareForm = MaterialItemOrder.bareItemForm(path);
        if (bareForm.isPresent()) {
            return Optional.of(new MaterialKey(MaterialItemOrder.canonicalMaterialToken(path), bareForm.get()));
        }
        return Optional.empty();
    }

    private static String normalizeMaterialForForm(String material, String formToken) {
        String out = material;
        if ("raw".equals(formToken)) {
            out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        }
        if ("crushed".equals(formToken)) {
            if (out.startsWith("raw_") && out.length() > "raw_".length()) {
                out = out.substring("raw_".length());
            }
            if (out.endsWith("_raw") && out.length() > "_raw".length()) {
                out = out.substring(0, out.length() - "_raw".length());
            }
            out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        }
        if ("ore".equals(formToken)) {
            out = stripOreMaterialPrefix(out);
            out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        }
        if ("raw_block".equals(formToken) && out.startsWith("raw_") && out.length() > "raw_".length()) {
            String rawVariant = out.substring("raw_".length());
            out = "raw_" + RawMaterialMappings.materialForRawVariant(rawVariant).orElse(rawVariant);
        }
        return out;
    }

    private static String stripOreMaterialPrefix(String token) {
        String lowered = token.toLowerCase(Locale.ROOT);
        for (String prefix : RawVariantCatalog.oreMaterialPrefixesForParsing()) {
            if (lowered.startsWith(prefix) && lowered.length() > prefix.length()) {
                return lowered.substring(prefix.length());
            }
        }
        return lowered;
    }

    private static String blockFormToken(String material) {
        return material.startsWith("raw_") && material.length() > "raw_".length()
                ? "raw_block"
                : "block";
    }

    private record MaterialKey(String material, String formToken) {}
}
