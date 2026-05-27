package eu.zunix.ore_and_alloy.integration;

import com.mojang.logging.LogUtils;
import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.config.OAConfig;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeAliasMapBuilder;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeAliasBuildResult;
import eu.zunix.ore_and_alloy.integration.recipe.RecipeMutationEngine;
import eu.zunix.ore_and_alloy.integration.recipe.UnificationAuditReporter;
import eu.zunix.ore_and_alloy.integration.recipe.UnificationSnapshotWriter;
import eu.zunix.ore_and_alloy.integration.recipe.UnificationStabilityGuard;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

import java.util.Map;

public final class RecipeInterceptor {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation CREATE_IRON_SHEET_ID = ResourceLocation.fromNamespaceAndPath("create", "iron_sheet");
    private static final ResourceLocation ORE_AND_ALLOY_IRON_PLATE_ID = ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "iron_plate");
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
        logCreatePressingAlias(aliasToCanonical, reason);
        UnificationStabilityGuard.enforce(aliasBuild, reason);
        if (OAConfig.unificationAuditEnabled()) {
            UnificationAuditReporter.report(aliasBuild, reason);
        }
        if (OAConfig.unificationSnapshotExportEnabled()) {
            UnificationSnapshotWriter.write(aliasBuild, reason);
        }
        if (aliasToCanonical.isEmpty()) {
            LOGGER.info("[{}] No alias materials found during recipe unification ({}).", OreAndAlloy.MODID, reason);
            return;
        }

        int touchedRecipes = 0;
        int rewrittenStacks = 0;
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            int rewrites = RecipeMutationEngine.rewriteRecipeStacks(holder.value(), aliasToCanonical);
            if (rewrites > 0) {
                touchedRecipes++;
                rewrittenStacks += rewrites;
            }
        }

        LOGGER.info("[{}] Recipe unification ({}) complete: aliases={}, recipes={}, stacks={}",
                OreAndAlloy.MODID, reason, aliasToCanonical.size(), touchedRecipes, rewrittenStacks);
    }

    private static void logCreatePressingAlias(Map<Item, Item> aliasToCanonical, String reason) {
        Item createSheet = BuiltInRegistries.ITEM.get(CREATE_IRON_SHEET_ID);
        Item oaPlate = BuiltInRegistries.ITEM.get(ORE_AND_ALLOY_IRON_PLATE_ID);
        if (createSheet == Items.AIR || oaPlate == Items.AIR) return;

        Item mapped = aliasToCanonical.get(createSheet);
        if (mapped == null) {
            LOGGER.info("[{}] Alias sample ({}): {} has no canonical override.", OreAndAlloy.MODID, reason, CREATE_IRON_SHEET_ID);
            return;
        }

        LOGGER.info("[{}] Alias sample ({}): {} -> {}{}",
                OreAndAlloy.MODID,
                reason,
                CREATE_IRON_SHEET_ID,
                BuiltInRegistries.ITEM.getKey(mapped),
                mapped == oaPlate ? " (expected ore_and_alloy plate)" : "");
    }
}
