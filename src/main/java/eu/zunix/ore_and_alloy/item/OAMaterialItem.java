package eu.zunix.ore_and_alloy.item;

import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class OAMaterialItem extends Item {
    private final String itemId;

    public OAMaterialItem(String itemId, Properties properties) {
        super(properties);
        this.itemId = itemId;
    }

    @Override
    public boolean isPiglinCurrency(ItemStack stack) {
        if (isCanonicalForVanilla(stack, Items.GOLD_INGOT)) {
            return true;
        }
        return super.isPiglinCurrency(stack);
    }

    @Override
    public String toString() {
        return "OAMaterialItem[" + itemId + "]";
    }

    private static boolean isCanonicalForVanilla(ItemStack stack, Item vanillaItem) {
        Map<Item, Item> aliasMap = RecipeInterceptor.buildAliasMapSnapshot();
        Item canonical = aliasMap.getOrDefault(vanillaItem, vanillaItem);
        return stack.is(canonical);
    }
}
