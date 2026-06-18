package eu.zunix.ore_and_alloy.integration.jei;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerAliases;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerHiddenItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModInfoRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JeiPlugin
public final class OreAndAlloyJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerModInfo(IModInfoRegistration registration) {
        registration.addModAliases(OreAndAlloy.MODID, "Ore and Alloy", "Ore & Alloy", "Unification");
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        Set<Item> hiddenItems = ViewerHiddenItems.resolveHiddenItems();
        for (Map.Entry<Item, Set<String>> entry : ViewerAliases.resolveVisibleItemAliases(hiddenItems).entrySet()) {
            ItemStack stack = entry.getKey().getDefaultInstance();
            if (stack.isEmpty()) continue;
            for (String alias : entry.getValue()) {
                registration.addAlias(stack, alias);
            }
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiAliasTooltipHooks.onRuntimeAvailable(runtime);
        Set<Item> hiddenItems = ViewerHiddenItems.resolveHiddenItems();
        if (!hiddenItems.isEmpty()) {
            hideIngredients(runtime.getIngredientManager(), hiddenItems);
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiAliasTooltipHooks.onRuntimeUnavailable();
    }

    private static void hideIngredients(IIngredientManager ingredientManager, Set<Item> hiddenItems) {
        List<ItemStack> stacks = new ArrayList<>(hiddenItems.size());
        for (Item item : hiddenItems) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isEmpty()) stacks.add(stack);
        }
        if (!stacks.isEmpty()) {
            ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        }
    }
}
