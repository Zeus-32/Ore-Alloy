package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.registry.ModStandaloneItems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

final class MaterialManifestReader {
    private static final Pattern VALID_ITEM_ID = Pattern.compile("[a-z0-9_]+");

    private final String manifestPath;

    MaterialManifestReader(String manifestPath) {
        this.manifestPath = manifestPath;
    }

    List<String> read() {
        InputStream stream = MaterialItemDiscovery.class.getClassLoader().getResourceAsStream(manifestPath);
        if (stream == null) return List.of();

        Set<String> ids = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim().toLowerCase(Locale.ROOT);
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (!VALID_ITEM_ID.matcher(trimmed).matches()) {
                    OreAndAlloy.LOGGER.warn("[{}] Ignoring invalid material item id in manifest: {}",
                            OreAndAlloy.MODID, trimmed);
                    continue;
                }
                if (MaterialItemIdUtil.isStandaloneMaterialItemId(trimmed) || ModStandaloneItems.isStandaloneItemId(trimmed)) {
                    continue;
                }
                ids.add(trimmed);
            }
        } catch (IOException ex) {
            OreAndAlloy.LOGGER.warn("[{}] Failed to read {}: {}", OreAndAlloy.MODID, manifestPath, ex.getMessage());
        }
        return MaterialItemOrder.sorted(ids);
    }
}
