package eu.zunix.ore_and_alloy.integration;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class VanillaItemAliasResolver {
    private VanillaItemAliasResolver() {}

    public static Item canonicalFor(Item vanillaItem) {
        return RecipeInterceptor.buildAliasMapSnapshot().getOrDefault(vanillaItem, vanillaItem);
    }

    public static boolean isCanonicalItemForVanilla(Item item, Item vanillaItem) {
        return item == canonicalFor(vanillaItem);
    }

    public static boolean isCanonicalForVanilla(ItemStack stack, Item vanillaItem) {
        return isCanonicalItemForVanilla(stack.getItem(), vanillaItem);
    }

    public static boolean matchesHolderSetThroughAliases(ItemStack stack, HolderSet<Item> holderSet) {
        if (stack.is(holderSet)) {
            return true;
        }

        Item stackItem = stack.getItem();
        Map<Item, Item> aliases = RecipeInterceptor.buildAliasMapSnapshot();
        for (Map.Entry<Item, Item> entry : aliases.entrySet()) {
            if (entry.getValue() != stackItem) {
                continue;
            }
            if (holderSet.contains(entry.getKey().builtInRegistryHolder())) {
                return true;
            }
        }
        return false;
    }
}
