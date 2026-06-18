package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeAliasMapBuilder;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeAliasBuildResult;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeMutationEngine;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecipeInterceptor {
    private static volatile Field recipeManagerRegistriesField;
    private static volatile Map<Item, Item> aliasMapSnapshot = Map.of();

    private RecipeInterceptor() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RecipeInterceptor::onServerStarted);
        NeoForge.EVENT_BUS.addListener(RecipeInterceptor::onDatapackSync);
    }

    public static void rewriteNow(RecipeManager recipeManager, String reason) {
        rewriteRecipes(recipeManager, reason);
    }

    public static Map<Item, Item> buildAliasMapSnapshot() {
        Map<Item, Item> snapshot = aliasMapSnapshot;
        if (!snapshot.isEmpty()) {
            return snapshot;
        }
        snapshot = RecipeAliasMapBuilder.buildAliasMapSnapshot();
        aliasMapSnapshot = snapshot;
        return snapshot;
    }

    private static void onServerStarted(ServerStartedEvent event) {
        rewriteRecipes(event.getServer().getRecipeManager(), "server_start");
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            rewriteRecipes(event.getPlayerList().getServer().getRecipeManager(), "datapack_reload");
        }
    }

    private static void rewriteRecipes(RecipeManager recipeManager, String reason) {
        RecipeAliasBuildResult aliasBuild = RecipeAliasMapBuilder.buildAliasAnalysis();
        Map<Item, Item> aliasToCanonical = aliasBuild.aliasToCanonical();
        aliasMapSnapshot = aliasToCanonical.isEmpty() ? Map.of() : Map.copyOf(aliasToCanonical);
        if (aliasToCanonical.isEmpty()) {
            return;
        }

        List<RecipeHolder<?>> recipes = new ArrayList<>(recipeManager.getRecipes());
        for (RecipeHolder<?> holder : recipes) {
            int rewrites = RecipeMutationEngine.rewriteRecipeStacks(holder.value(), aliasToCanonical);
            if (rewrites <= 0) continue;
        }
        removeForeignRecipesWhenCanonicalExists(recipeManager, recipes);
    }

    private static int removeForeignRecipesWhenCanonicalExists(RecipeManager recipeManager, List<RecipeHolder<?>> recipes) {
        HolderLookup.Provider registries = resolveRecipeRegistries(recipeManager);
        if (registries == null) {
            return 0;
        }

        Set<RecipeCollisionKey> canonicalKeys = new HashSet<>();
        for (RecipeHolder<?> holder : recipes) {
            if (!OreAndAlloy.MODID.equals(holder.id().getNamespace())) continue;
            RecipeCollisionKey key = collisionKey(holder, registries);
            if (key != null) {
                canonicalKeys.add(key);
            }
        }
        if (canonicalKeys.isEmpty()) {
            return 0;
        }

        List<RecipeHolder<?>> filtered = new ArrayList<>(recipes.size());
        int removed = 0;
        for (RecipeHolder<?> holder : recipes) {
            if (OreAndAlloy.MODID.equals(holder.id().getNamespace())) {
                filtered.add(holder);
                continue;
            }

            RecipeCollisionKey key = collisionKey(holder, registries);
            if (key != null && canonicalKeys.contains(key)) {
                removed++;
                continue;
            }
            filtered.add(holder);
        }

        if (removed > 0) {
            recipeManager.replaceRecipes(filtered);
        }
        return removed;
    }

    private static RecipeCollisionKey collisionKey(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        Recipe<?> recipe = holder.value();
        ResourceLocation typeId = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        if (typeId == null) return null;

        ItemStack result;
        try {
            result = recipe.getResultItem(registries);
        } catch (RuntimeException ignored) {
            return null;
        }
        if (result.isEmpty()) return null;

        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (resultId == null || result.getItem() == Items.AIR) return null;
        return new RecipeCollisionKey(typeId, resultId, Math.max(1, result.getCount()));
    }

    private static HolderLookup.Provider resolveRecipeRegistries(RecipeManager recipeManager) {
        Field field = recipeManagerRegistriesField;
        if (field == null) {
            try {
                Field resolved = RecipeManager.class.getDeclaredField("registries");
                if (!resolved.trySetAccessible()) return null;
                recipeManagerRegistriesField = resolved;
                field = resolved;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        try {
            Object value = field.get(recipeManager);
            if (value instanceof HolderLookup.Provider provider) {
                return provider;
            }
        } catch (IllegalAccessException ignored) {
            return null;
        }
        return null;
    }

    private record RecipeCollisionKey(ResourceLocation recipeTypeId, ResourceLocation resultItemId, int resultCount) {}
}
