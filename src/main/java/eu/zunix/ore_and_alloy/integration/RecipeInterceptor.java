package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.integration.recipe.RecipeAliasMapBuilder;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeAliasBuildResult;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeMutationEngine;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecipeInterceptor {
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
    }
}
