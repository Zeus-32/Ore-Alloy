package eu.zunix.ore_and_alloy.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class StorageBlockCatalog {
    private static final List<String> BASE_FORM_PRIORITY = List.of("ingot", "dust");
    private StorageBlockCatalog() {}

    public static Map<String, String> collectStorageBlockBaseForms(Collection<String> materialItemIds) {
        Map<String, Set<String>> formsByMaterial = new LinkedHashMap<>();

        for (String id : materialItemIds) {
            Optional<String> form = MaterialItemOrder.formToken(id);
            if (form.isEmpty()) continue;

            String material = MaterialItemOrder.materialPart(id);
            if (!isStorageBaseForm(material, form.get())) continue;
            formsByMaterial.computeIfAbsent(material, ignored -> new LinkedHashSet<>()).add(form.get());
        }

        List<String> materials = new ArrayList<>(formsByMaterial.keySet());
        materials.sort(Comparator
                .comparing((String material) -> MaterialItemOrder.preferredItemMaterialToken(material))
                .thenComparing(Comparator.naturalOrder()));

        Map<String, String> out = new LinkedHashMap<>();
        for (String material : materials) {
            Set<String> forms = formsByMaterial.getOrDefault(material, Set.of());
            for (String candidate : baseFormPriorityForMaterial(material)) {
                if (forms.contains(candidate)) {
                    out.put(material, candidate);
                    break;
                }
            }
        }

        return Map.copyOf(out);
    }

    public static String blockIdForMaterial(String materialToken) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(materialToken);
        String preferred = MaterialItemOrder.preferredItemMaterialToken(canonical);
        return preferred + "_block";
    }

    public static String baseItemIdForMaterial(String materialToken, String baseForm) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(materialToken);
        String normalizedForm = baseForm == null ? "" : baseForm.toLowerCase(Locale.ROOT);

        if (MaterialItemOrder.bareItemForm(canonical).map(normalizedForm::equals).orElse(false)) {
            return canonical;
        }

        String preferred = MaterialItemOrder.preferredItemMaterialToken(canonical);
        return preferred + "_" + normalizedForm;
    }

    public static int storageBlockCraftCountForMaterial(String materialToken) {
        return 9;
    }

    private static boolean isStorageBaseForm(String material, String form) {
        if (DustOnlyMaterials.isSupported(material, form)) {
            return false;
        }
        return BASE_FORM_PRIORITY.contains(form)
                || isBareGemForm(material, form);
    }

    private static List<String> baseFormPriorityForMaterial(String material) {
        return MaterialItemOrder.bareItemForm(material)
                .filter(bareForm -> isBareGemForm(material, bareForm))
                .map(bareForm -> List.of("ingot", bareForm, "dust"))
                .orElse(BASE_FORM_PRIORITY);
    }

    private static boolean isBareGemForm(String material, String form) {
        return MaterialItemOrder.bareItemForm(material).map(form::equals).orElse(false)
                && "gems".equals(MaterialFormCatalog.TAG_BUCKET_BY_FORM.get(form));
    }
}
