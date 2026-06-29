package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.MaterialActivationRequests;
import eu.zunix.ore_and_alloy.item.OAMaterialItem;
import eu.zunix.ore_and_alloy.registry.discovery.MaterialItemDiscovery;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreAndAlloy.MODID);
    private static final Map<String, DeferredItem<Item>> REGISTERED_MATERIAL_ITEMS = new LinkedHashMap<>();

    static {
        MaterialActivationRequests.freeze();
        MaterialItemDiscovery.DiscoveryResult discovered = MaterialItemDiscovery.discoverMaterialItems();
        for (String id : discovered.itemIds()) {
            if (isBlockBackedItemId(id)) continue;
            registerMaterialItem(id);
        }
        ModFluids.registerBuckets(ITEMS);
        ModOreBlocks.registerRawVariantOres(discovered.itemIds(), ITEMS);
        ModStorageBlocks.registerStorageBlocks(discovered.itemIds(), ITEMS);
    }

    public static Map<String, DeferredItem<Item>> materialItems() {
        return Collections.unmodifiableMap(REGISTERED_MATERIAL_ITEMS);
    }

    private static void registerMaterialItem(String id) {
        if (REGISTERED_MATERIAL_ITEMS.containsKey(id)) return;
        DeferredItem<Item> item = ITEMS.register(id, () -> createMaterialItem(id));
        REGISTERED_MATERIAL_ITEMS.put(id, item);
    }

    private static Item createMaterialItem(String id) {
        return new OAMaterialItem(id, new Item.Properties());
    }

    private static boolean isBlockBackedItemId(String id) {
        return id.endsWith("_ore") || id.endsWith("_block");
    }
}
