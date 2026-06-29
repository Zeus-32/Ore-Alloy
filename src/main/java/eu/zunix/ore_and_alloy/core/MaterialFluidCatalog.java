package eu.zunix.ore_and_alloy.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MaterialFluidCatalog {
    private static final List<Entry> STANDALONE_FLUIDS = List.of(
            new Entry("creosote", "creosote", false),
            new Entry("steam", "steam", false)
    );

    private MaterialFluidCatalog() {}

    public static List<Entry> entries() {
        List<Entry> out = new ArrayList<>(STANDALONE_FLUIDS);
        for (MetalMaterial material : MetalMaterial.values()) {
            if (!material.getForms().contains(MaterialForm.INGOT)) continue;
            String materialName = material.materialName();
            out.add(new Entry("liquid_" + materialName, materialName, true));
        }
        out.sort(Comparator.comparing(Entry::id));
        return List.copyOf(out);
    }

    public static List<String> ids() {
        return entries().stream().map(Entry::id).toList();
    }

    public record Entry(String id, String material, boolean metalLike) {
        public String bucketItemId() {
            return id + "_bucket";
        }

        public String displayName() {
            if (metalLike) {
                return "Liquid " + capitalizeWords(material.replace('_', ' '));
            }
            return capitalizeWords(id.replace('_', ' '));
        }

        private static String capitalizeWords(String value) {
            if (value.isBlank()) return value;
            String[] words = value.split(" ");
            for (int i = 0; i < words.length; i++) {
                if (words[i].isBlank()) continue;
                words[i] = words[i].substring(0, 1).toUpperCase(Locale.ROOT)
                        + words[i].substring(1).toLowerCase(Locale.ROOT);
            }
            return String.join(" ", words);
        }
    }
}
