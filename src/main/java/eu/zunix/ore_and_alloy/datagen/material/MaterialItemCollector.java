package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MaterialItemCollector {
    private final String namespace;
    private final Path mainResources;

    public MaterialItemCollector(String namespace, Path mainResources) {
        this.namespace = namespace;
        this.mainResources = mainResources;
    }

    public List<String> collectMaterialItems() throws IOException {
        LinkedHashSet<String> items = new LinkedHashSet<>();
        Path itemTextureRoot = mainResources.resolve(Path.of("assets", namespace, "textures", "item"));
        if (!Files.isDirectory(itemTextureRoot)) return List.of();

        try (var files = Files.list(itemTextureRoot)) {
            for (Path file : files.toList()) {
                if (!Files.isRegularFile(file)) continue;
                String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!fileName.endsWith(".png")) continue;

                String itemId = fileName.substring(0, fileName.length() - 4);
                if (MaterialItemOrder.bareItemForm(itemId).isPresent() && isKnownMetal(itemId)) {
                    items.add(itemId);
                }
            }
        }

        try (var dirs = Files.list(itemTextureRoot)) {
            for (Path dir : dirs.toList()) {
                if (!Files.isDirectory(dir)) continue;
                String form = dir.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!isSupportedFormFolder(form)) continue;

                try (var files = Files.list(dir)) {
                    for (Path file : files.toList()) {
                        if (!Files.isRegularFile(file)) continue;
                        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
                        if (!fileName.endsWith(".png")) continue;

                        String textureId = fileName.substring(0, fileName.length() - 4);
                        String itemId = MaterialIdParser.deriveItemIdFromTextureFile(form, textureId);
                        if (!itemId.isBlank() && isKnownMetal(itemId)) {
                            items.add(itemId);
                        }
                    }
                }
            }
        }

        List<String> sorted = new ArrayList<>(items);
        addGuaranteedMaterialForms(sorted);
        sorted.sort(MaterialItemOrder.comparator());
        return sorted;
    }

    public void writeMaterialManifest(Path mainResRoot, List<String> materialItems) throws IOException {
        Path manifest = mainResRoot.resolve(Path.of("assets", namespace, "material_items.json"));
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"items\": [\n");
        for (int i = 0; i < materialItems.size(); i++) {
            sb.append("    \"").append(materialItems.get(i)).append("\"");
            sb.append(i + 1 < materialItems.size() ? ",\n" : "\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        DatagenFiles.writeText(manifest, sb.toString());
    }

    private static void addGuaranteedMaterialForms(List<String> items) {
        Map<String, String> preferredTokens = preferredMaterialTokens(items);
        Map<String, Set<String>> discoveredFormsByMaterial = discoveredFormsByMaterial(items);
        LinkedHashSet<String> set = new LinkedHashSet<>();

        for (String id : items) {
            MaterialId parsed = MaterialIdParser.parseItemId(id);
            if ("raw".equals(parsed.form()) || "crushed".equals(parsed.form())) {
                set.add(id);
                continue;
            }
            String material = preferredMaterialToken(preferredTokens, parsed.material());
            set.add(MaterialIdParser.itemIdFor(material, parsed.form()));
        }

        for (Map.Entry<String, Set<String>> entry : discoveredFormsByMaterial.entrySet()) {
            String material = preferredMaterialToken(preferredTokens, entry.getKey());
            Set<String> forms = entry.getValue();

            if (forms.contains("ingot")) {
                MetalMaterial.fromToken(entry.getKey()).ifPresent(metal -> {
                    for (MaterialForm form : metal.getForms()) {
                        String formToken = form.name().toLowerCase(Locale.ROOT);
                        if ("ore".equals(formToken) || "raw".equals(formToken) || "crushed".equals(formToken)) continue;
                        set.add(MaterialIdParser.itemIdFor(material, formToken));
                    }
                });
            }
            if (forms.contains("diamond")) {
                MetalMaterial.fromToken(entry.getKey()).ifPresent(metal -> {
                    for (MaterialForm form : metal.getForms()) {
                        String formToken = form.name().toLowerCase(Locale.ROOT);
                        set.add(MaterialIdParser.itemIdFor(material, formToken));
                    }
                });
            }
        }

        for (MetalMaterial metal : MetalMaterial.values()) {
            boolean bareMaterial = false;
            for (MaterialForm form : metal.getForms()) {
                String formToken = form.name().toLowerCase(Locale.ROOT);
                MaterialItemOrder.bareItemForm(metal.materialName())
                        .filter(formToken::equals)
                        .ifPresent(ignored -> set.add(MaterialIdParser.itemIdFor(metal.materialName(), formToken)));
                if (MaterialItemOrder.bareItemForm(metal.materialName()).isPresent()) {
                    bareMaterial = true;
                }
            }
            if (bareMaterial) {
                for (MaterialForm form : metal.getForms()) {
                    String formToken = form.name().toLowerCase(Locale.ROOT);
                    set.add(MaterialIdParser.itemIdFor(metal.materialName(), formToken));
                }
            }
            String material = preferredMaterialToken(preferredTokens, metal.materialName());
            if (metal.getForms().contains(MaterialForm.RAW)) {
                set.addAll(RawMaterialMappings.rawItemIdsForMaterial(material));
            }
            if (metal.getForms().contains(MaterialForm.CRUSHED)) {
                set.addAll(RawMaterialMappings.crushedItemIdsForMaterial(material));
            }
        }
        items.clear();
        items.addAll(set);
    }

    private static Map<String, String> preferredMaterialTokens(List<String> itemIds) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String id : itemIds) {
            MaterialId parsed = MaterialIdParser.parseItemId(id);
            String token = parsed.material();
            String canonical = MaterialItemOrder.canonicalMaterialToken(token);
            String previous = out.get(canonical);
            if (previous == null || shouldPreferToken(token, previous, canonical)) {
                out.put(canonical, token);
            }
        }
        return out;
    }

    private static Map<String, Set<String>> discoveredFormsByMaterial(List<String> itemIds) {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String id : itemIds) {
            MaterialId parsed = MaterialIdParser.parseItemId(id);
            String material = MaterialItemOrder.canonicalMaterialToken(parsed.material());
            out.computeIfAbsent(material, ignored -> new LinkedHashSet<>()).add(parsed.form());
        }
        return out;
    }

    private static boolean shouldPreferToken(String candidate, String existing, String canonical) {
        return canonical.equals(candidate) && !canonical.equals(existing);
    }

    private static boolean isKnownMetal(String itemId) {
        try {
            MaterialId parsed = MaterialIdParser.parseItemId(itemId);
            if (("raw".equals(parsed.form()) || "crushed".equals(parsed.form()))
                    && !RawMaterialMappings.isConfiguredRawVariant(variantToken(itemId, parsed.form()))) {
                return false;
            }
            return MetalMaterial.fromToken(parsed.material()).isPresent();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static String variantToken(String itemId, String form) {
        String prefix = form + "_";
        return itemId.startsWith(prefix) ? itemId.substring(prefix.length()) : "";
    }

    private static String preferredMaterialToken(Map<String, String> preferredTokens, String material) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(material);
        return preferredTokens.getOrDefault(canonical, canonical);
    }

    private static boolean isSupportedFormFolder(String folderName) {
        String normalized = normalizeFormFolder(folderName);
        return MaterialFormCatalog.FORM_ORDER.contains(normalized);
    }

    private static String normalizeFormFolder(String form) {
        if (form == null) return "";
        String lowered = form.toLowerCase(Locale.ROOT);
        if ("raw_material".equals(lowered) || "raw_materials".equals(lowered)) {
            return "raw";
        }
        return lowered;
    }
}
