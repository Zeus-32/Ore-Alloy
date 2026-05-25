package eu.zunix.ore_and_alloy.integration.jei;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.jei.vein.JeiVeinInfoCategory;
import eu.zunix.ore_and_alloy.integration.jei.vein.JeiVeinInfoRecipe;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerAliases;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerHiddenItems;
import eu.zunix.ore_and_alloy.registry.ModItems;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinCatalog;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModInfoRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JeiPlugin
public final class OreAndAlloyJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerModInfo(IModInfoRegistration registration) {
        registration.addModAliases(OreAndAlloy.MODID, "Ore and Alloy", "Ore & Alloy", "Unification");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ItemStack icon = ModItems.prospector()
                .map(item -> item.value().getDefaultInstance())
                .orElse(new ItemStack(Items.IRON_ORE));
        registration.addRecipeCategories(new JeiVeinInfoCategory(registration.getJeiHelpers().getGuiHelper(), icon));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ModItems.prospector()
                .map(item -> item.value().getDefaultInstance())
                .ifPresent(stack -> registration.addRecipeCatalyst(stack, JeiVeinInfoCategory.TYPE));
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        Set<Item> hiddenItems = ViewerHiddenItems.resolveHiddenItems();
        Map<Item, Set<String>> aliasesByItem = ViewerAliases.resolveVisibleItemAliases(hiddenItems);
        int aliasCount = 0;

        for (Map.Entry<Item, Set<String>> entry : aliasesByItem.entrySet()) {
            ItemStack stack = entry.getKey().getDefaultInstance();
            if (stack.isEmpty()) continue;

            for (String alias : entry.getValue()) {
                registration.addAlias(stack, alias);
                aliasCount++;
            }
        }

        OreAndAlloy.LOGGER.info("[{}][JEI] Registered {} aliases for {} items.",
                OreAndAlloy.MODID, aliasCount, aliasesByItem.size());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Set<Item> hiddenItems = ViewerHiddenItems.resolveHiddenItems();
        List<ItemStack> moltenBuckets = new ArrayList<>();
        ItemStack prospector = ItemStack.EMPTY;

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (hiddenItems.contains(item)) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!OreAndAlloy.MODID.equals(id.getNamespace())) continue;

            String path = id.getPath();
            if ("prospector".equals(path)) {
                prospector = item.getDefaultInstance();
            } else if (path.startsWith("molten_") && path.endsWith("_bucket")) {
                moltenBuckets.add(item.getDefaultInstance());
            }
        }

        if (!prospector.isEmpty()) {
            registration.addItemStackInfo(
                    prospector,
                    Component.translatable("ore_and_alloy.jei.info.prospector")
            );
        }

        if (!moltenBuckets.isEmpty()) {
            registration.addItemStackInfo(
                    moltenBuckets,
                    Component.translatable("ore_and_alloy.jei.info.molten.line1"),
                    Component.translatable("ore_and_alloy.jei.info.molten.line2")
            );
        }

        List<JeiVeinInfoRecipe> veinRecipes = OAVeinCatalog.definitions().stream()
                .map(JeiVeinInfoRecipe::fromDefinition)
                .filter(recipe -> !recipe.oreStacks().isEmpty() || !recipe.rawStacks().isEmpty())
                .toList();
        if (!veinRecipes.isEmpty()) {
            registration.addRecipes(JeiVeinInfoCategory.TYPE, veinRecipes);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JeiAliasTooltipHooks.onRuntimeAvailable(jeiRuntime);

        Set<Item> hiddenItems = ViewerHiddenItems.resolveHiddenItems();
        if (hiddenItems.isEmpty()) return;

        hideIngredients(jeiRuntime.getIngredientManager(), hiddenItems);
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiAliasTooltipHooks.onRuntimeUnavailable();
    }

    private static void hideIngredients(IIngredientManager ingredientManager, Set<Item> hiddenItems) {
        List<ItemStack> toHide = new ArrayList<>(hiddenItems.size());
        for (Item item : hiddenItems) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isEmpty()) toHide.add(stack);
        }

        if (toHide.isEmpty()) return;
        ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, toHide);
        OreAndAlloy.LOGGER.info("[{}][JEI] Hidden {} item stacks.", OreAndAlloy.MODID, toHide.size());
    }
}
