package eu.zunix.ore_and_alloy.core;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DustOnlyMaterials {
    private static final List<String> MATERIALS = List.of("flint", "sand", "clay", "brick");
    private static final Set<String> FORMS = Set.of("dust", "dust_pile", "tiny_dust_pile");

    private DustOnlyMaterials() {}

    public static List<String> materials() {
        return MATERIALS;
    }

    public static List<String> itemIds() {
        return MATERIALS.stream()
                .flatMap(material -> FORMS.stream().map(form -> itemIdFor(material, form)))
                .toList();
    }

    public static boolean isSupported(String material, String form) {
        String normalizedMaterial = material == null ? "" : material.toLowerCase(Locale.ROOT);
        String normalizedForm = form == null ? "" : form.toLowerCase(Locale.ROOT);
        return MATERIALS.contains(normalizedMaterial) && FORMS.contains(normalizedForm);
    }

    private static String itemIdFor(String material, String form) {
        return material + "_" + form;
    }
}
