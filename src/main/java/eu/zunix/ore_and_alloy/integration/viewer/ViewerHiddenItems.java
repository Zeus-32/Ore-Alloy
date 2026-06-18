package eu.zunix.ore_and_alloy.integration.viewer;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class ViewerHiddenItems {
    private ViewerHiddenItems() {}

    public static Set<ResourceLocation> resolveHiddenItemIds() {
        LinkedHashSet<ResourceLocation> hiddenIds = new LinkedHashSet<>();
        List<Map.Entry<Item, Item>> aliasEntries = RecipeInterceptor.buildAliasMapSnapshot().entrySet().stream()
                .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey()).toString()))
                .toList();

        for (Map.Entry<Item, Item> entry : aliasEntries) {
            Item alias = entry.getKey();
            Item canonical = entry.getValue();
            if (alias == canonical || alias == Items.AIR || canonical == Items.AIR) continue;

            ResourceLocation canonicalId = BuiltInRegistries.ITEM.getKey(canonical);
            if (!OreAndAlloy.MODID.equals(canonicalId.getNamespace())) continue;

            ResourceLocation aliasId = BuiltInRegistries.ITEM.getKey(alias);
            hiddenIds.add(aliasId);
        }

        return Collections.unmodifiableSet(hiddenIds);
    }

    public static Set<Item> resolveHiddenItems() {
        LinkedHashSet<Item> hiddenItems = new LinkedHashSet<>();
        for (ResourceLocation id : resolveHiddenItemIds()) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == Items.AIR) continue;
            hiddenItems.add(item);
        }
        return Collections.unmodifiableSet(hiddenItems);
    }

    public static boolean isHiddenStack(ItemStack stack, Set<Item> hiddenItems) {
        return stack != null && !stack.isEmpty() && hiddenItems.contains(stack.getItem());
    }
}
