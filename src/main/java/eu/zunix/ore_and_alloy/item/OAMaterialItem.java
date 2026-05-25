package eu.zunix.ore_and_alloy.item;

import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class OAMaterialItem extends Item {
    private static final int COAL_BURN_TIME = 1600;
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
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        if (isCanonicalForVanilla(stack, Items.COAL)) {
            return COAL_BURN_TIME;
        }
        return super.getBurnTime(stack, recipeType);
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
