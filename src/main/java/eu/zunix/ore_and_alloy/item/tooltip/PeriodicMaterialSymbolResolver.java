package eu.zunix.ore_and_alloy.item.tooltip;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import eu.zunix.ore_and_alloy.registry.ModStandaloneItems;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class PeriodicMaterialSymbolResolver {
    private static final Pattern SPLIT_TOKENS = Pattern.compile("[_\\-\\s]+");
    private static final List<String> FORM_TOKENS = MaterialFormCatalog.FORM_SUFFIX_PARSE_ORDER;

    private static final Map<String, String> MATERIAL_FORMULAS = Map.ofEntries(
            Map.entry("iron", "Fe"),
            Map.entry("gold", "Au"),
            Map.entry("copper", "Cu"),
            Map.entry("tin", "Sn"),
            Map.entry("lead", "Pb"),
            Map.entry("silver", "Ag"),
            Map.entry("nickel", "Ni"),
            Map.entry("zinc", "Zn"),
            Map.entry("aluminum", "Al"),
            Map.entry("osmium", "Os"),
            Map.entry("uranium", "U"),
            Map.entry("cobalt", "Co"),
            Map.entry("titanium", "Ti"),
            Map.entry("chrome", "Cr"),
            Map.entry("chromium", "Cr"),
            Map.entry("platinum", "Pt"),
            Map.entry("iridium", "Ir"),
            Map.entry("diamond", "C"),
            Map.entry("coal", "C"),
            Map.entry("silicon", "Si"),
            Map.entry("lithium", "Li"),
            Map.entry("antimony", "Sb"),
            Map.entry("lapis", "La"),
            Map.entry("brass", "Cu3Zn2"),
            Map.entry("bronze", "Cu3Sn"),
            Map.entry("electrum", "AuAg"),
            Map.entry("invar", "Fe2Ni"),
            Map.entry("constantan", "Cu55Ni45"),
            Map.entry("steel", "FeC"),
            Map.entry("stainless_steel", "FeCrNi"),
            Map.entry("wrought_iron", "FeC")
    );

    private PeriodicMaterialSymbolResolver() {}

    public static Optional<String> resolve(String itemPath) {
        String material = extractMaterialToken(itemPath);
        if (material.isBlank()) return Optional.empty();
        String canonical = MaterialItemOrder.canonicalMaterialToken(material);
        String explicit = MATERIAL_FORMULAS.get(canonical);
        if (explicit != null && !explicit.isBlank()) return Optional.of(explicit);
        return Optional.of(generalSymbol(canonical));
    }

    private static String extractMaterialToken(String itemPath) {
        if (itemPath == null || itemPath.isBlank()) return "";
        String id = itemPath.toLowerCase(Locale.ROOT);

        if (id.startsWith("molten_") && id.endsWith("_bucket") && id.length() > "molten__bucket".length()) {
            return id.substring("molten_".length(), id.length() - "_bucket".length());
        }

        if (id.startsWith("crushed_raw_") && id.length() > "crushed_raw_".length()) {
            String token = id.substring("crushed_raw_".length());
            return RawMaterialMappings.materialForRawVariant(token).orElse(token);
        }
        if (id.startsWith("crushed_") && id.length() > "crushed_".length()) {
            String token = id.substring("crushed_".length());
            return RawMaterialMappings.materialForRawVariant(token).orElse(token);
        }
        if (id.startsWith("raw_") && id.length() > "raw_".length()) {
            String token = id.substring("raw_".length());
            return RawMaterialMappings.materialForRawVariant(token).orElse(token);
        }

        for (String formToken : FORM_TOKENS) {
            String suffix = "_" + formToken;
            if (id.endsWith(suffix) && id.length() > suffix.length()) {
                String token = id.substring(0, id.length() - suffix.length());
                if ("ore".equals(formToken)) {
                    token = RawVariantCatalog.stripOreMaterialPrefix(token);
                    token = RawMaterialMappings.materialForRawVariant(token).orElse(token);
                }
                return token;
            }
        }

        if (id.endsWith("_block") && id.length() > "_block".length()) {
            return id.substring(0, id.length() - "_block".length());
        }

        if (MaterialItemOrder.bareItemForm(id).isPresent()) {
            return id;
        }

        return ModStandaloneItems.materialTokenForItemId(id).orElse("");
    }

    private static String generalSymbol(String materialToken) {
        String[] tokens = SPLIT_TOKENS.split(materialToken);
        if (tokens.length == 0) return materialToken;

        String first = tokens[0];
        if (tokens.length == 1) {
            if (first.length() == 1) {
                return first.toUpperCase(Locale.ROOT);
            }
            if (first.length() >= 2) {
                return first.substring(0, 1).toUpperCase(Locale.ROOT) + first.substring(1, 2).toLowerCase(Locale.ROOT);
            }
        }

        StringBuilder out = new StringBuilder(2);
        for (String token : tokens) {
            if (token.isBlank()) continue;
            out.append(Character.toUpperCase(token.charAt(0)));
            if (out.length() >= 2) break;
        }

        if (out.length() == 0) {
            return materialToken.length() >= 2
                    ? materialToken.substring(0, 2).toUpperCase(Locale.ROOT)
                    : materialToken.toUpperCase(Locale.ROOT);
        }
        return out.toString();
    }
}
