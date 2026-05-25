package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ModStandaloneItems {
    private static final Map<String, StandaloneItemDefinition> DEFINITIONS = buildDefinitions();
    private static final Set<String> MATERIAL_TOKENS = buildMaterialTokens();
    private static final Map<String, DeferredItem<Item>> REGISTERED_ITEMS = new LinkedHashMap<>();

    private ModStandaloneItems() {}

    public static void registerAll(DeferredRegister.Items register) {
        if (!REGISTERED_ITEMS.isEmpty()) return;

        List<String> ids = new ArrayList<>(DEFINITIONS.keySet());
        ids.sort(String::compareTo);
        for (String id : ids) {
            DeferredItem<Item> item = register.register(id, () -> new Item(new Item.Properties()));
            REGISTERED_ITEMS.put(id, item);
        }
    }

    public static Map<String, DeferredItem<Item>> items() {
        return Collections.unmodifiableMap(REGISTERED_ITEMS);
    }

    public static List<StandaloneItemDefinition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static boolean isStandaloneItemId(String itemId) {
        return DEFINITIONS.containsKey(normalize(itemId));
    }

    public static Optional<String> materialTokenForItemId(String itemId) {
        StandaloneItemDefinition definition = DEFINITIONS.get(normalize(itemId));
        if (definition == null) return Optional.empty();
        return Optional.of(definition.materialToken());
    }

    public static boolean isStandaloneMaterialToken(String materialToken) {
        return MATERIAL_TOKENS.contains(MaterialItemOrder.canonicalMaterialToken(normalize(materialToken)));
    }

    private static Map<String, StandaloneItemDefinition> buildDefinitions() {
        Map<String, StandaloneItemDefinition> out = new LinkedHashMap<>();
        register(out, "silicon", "item/ingot/silicon_ingot", "Silicon", "silicon");
        return Map.copyOf(out);
    }

    private static Set<String> buildMaterialTokens() {
        Set<String> out = new LinkedHashSet<>();
        for (StandaloneItemDefinition definition : DEFINITIONS.values()) {
            out.add(MaterialItemOrder.canonicalMaterialToken(definition.materialToken()));
        }
        return Set.copyOf(out);
    }

    private static void register(Map<String, StandaloneItemDefinition> out, String id, String texturePath, String displayName, String materialToken) {
        String normalizedId = normalize(id);
        String normalizedToken = MaterialItemOrder.canonicalMaterialToken(normalize(materialToken));
        out.put(normalizedId, new StandaloneItemDefinition(normalizedId, texturePath, displayName, normalizedToken));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public record StandaloneItemDefinition(String id, String texturePath, String displayName, String materialToken) {}
}
