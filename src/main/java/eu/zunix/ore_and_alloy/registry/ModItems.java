package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.config.OAConfig;
import eu.zunix.ore_and_alloy.item.OAMaterialItem;
import eu.zunix.ore_and_alloy.item.ProspectorItem;
import eu.zunix.ore_and_alloy.registry.discovery.MaterialItemDiscovery;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreAndAlloy.MODID);
    private static final Map<String, DeferredItem<Item>> REGISTERED_MATERIAL_ITEMS = new LinkedHashMap<>();

    private static final DeferredItem<Item> PROSPECTOR = registerProspectorIfEnabled();

    static {
        MaterialItemDiscovery.DiscoveryResult discovered = MaterialItemDiscovery.discoverMaterialItems();
        for (String id : discovered.itemIds()) {
            registerMaterialItem(id);
        }
        ModOreBlocks.registerRawVariantOres(discovered.itemIds(), ITEMS);
        ModStandaloneItems.registerAll(ITEMS);

        OreAndAlloy.LOGGER.info("[{}] Item registration source={} material_items={} ore_blocks={} standalone_items={}",
                OreAndAlloy.MODID,
                discovered.source(),
                REGISTERED_MATERIAL_ITEMS.size(),
                ModOreBlocks.allOreBlocksById().size(),
                ModStandaloneItems.items().size());
    }

    public static Map<String, DeferredItem<Item>> materialItems() {
        return Collections.unmodifiableMap(REGISTERED_MATERIAL_ITEMS);
    }

    public static Map<String, DeferredItem<Item>> standaloneItems() {
        return ModStandaloneItems.items();
    }

    public static boolean hasProspector() {
        return PROSPECTOR != null;
    }

    public static Optional<DeferredItem<Item>> prospector() {
        return Optional.ofNullable(PROSPECTOR);
    }

    private static void registerMaterialItem(String id) {
        if (REGISTERED_MATERIAL_ITEMS.containsKey(id)) return;
        DeferredItem<Item> item = ITEMS.register(id, () -> createMaterialItem(id));
        REGISTERED_MATERIAL_ITEMS.put(id, item);
    }

    private static Item createMaterialItem(String id) {
        if ("redstone".equals(id)) {
            return new ItemNameBlockItem(Blocks.REDSTONE_WIRE, new Item.Properties());
        }
        return new OAMaterialItem(id, new Item.Properties());
    }

    private static DeferredItem<Item> registerProspectorIfEnabled() {
        if (!OAConfig.prospectorEnabled()) {
            OreAndAlloy.LOGGER.info("[{}] Prospector not registered because worldgen.custom_vein_worldgen_enabled=false.",
                    OreAndAlloy.MODID);
            return null;
        }
        return ITEMS.register("prospector", () -> new ProspectorItem(new Item.Properties().stacksTo(1)));
    }
}
