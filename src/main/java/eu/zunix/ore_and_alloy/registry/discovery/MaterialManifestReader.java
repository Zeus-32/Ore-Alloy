package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.DustOnlyMaterials;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.StandaloneMaterialItems;
import eu.zunix.ore_and_alloy.integration.IntegrationMaterialRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MaterialManifestReader {
    private static final Pattern VALID_ITEM_ID = Pattern.compile("[a-z0-9_]+");
    private static final Pattern JSON_STRING = Pattern.compile("\"([a-z0-9_]+)\"");

    private final String manifestPath;

    MaterialManifestReader(String manifestPath) {
        this.manifestPath = manifestPath;
    }

    List<String> read() {
        InputStream stream = MaterialItemDiscovery.class.getClassLoader().getResourceAsStream(manifestPath);
        if (stream == null) return List.of();

        Set<String> ids = new TreeSet<>();
        try (stream) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = JSON_STRING.matcher(json);
            while (matcher.find()) {
                String trimmed = matcher.group(1).toLowerCase(Locale.ROOT);
                if (!VALID_ITEM_ID.matcher(trimmed).matches()) {
                    continue;
                }
                if (StandaloneMaterialItems.byId(trimmed).isPresent()) {
                    ids.add(trimmed);
                    continue;
                }
                MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(trimmed);
                if (parsed != null && DustOnlyMaterials.isSupported(parsed.material(), parsed.form())) {
                    ids.add(trimmed);
                    continue;
                }
                if (parsed == null || !IntegrationMaterialRegistry.isMaterialEnabled(parsed.material())) {
                    continue;
                }
                if (isRawVariantForm(parsed.form())
                        && !RawMaterialMappings.isConfiguredRawVariant(variantToken(trimmed, parsed.form()))) {
                    continue;
                }
                ids.add(trimmed);
            }
        } catch (IOException ex) {
        }
        return MaterialItemOrder.sorted(ids);
    }

    private static boolean isRawVariantForm(String form) {
        return "raw".equals(form) || "crushed".equals(form);
    }

    private static String variantToken(String itemId, String form) {
        String prefix = form + "_";
        return itemId.startsWith(prefix) ? itemId.substring(prefix.length()) : "";
    }
}
