package eu.zunix.ore_and_alloy;

import eu.zunix.ore_and_alloy.client.screen.OAConfigScreen;
import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import eu.zunix.ore_and_alloy.registry.ModFluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = OreAndAlloy.MODID, dist = Dist.CLIENT)
public final class OreAndAlloyClient {
    public OreAndAlloyClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ModFluids::onRegisterClientExtensions);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new OAConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(this::onRecipesUpdated);
    }

    private void onRecipesUpdated(RecipesUpdatedEvent event) {
        RecipeInterceptor.rewriteNow(event.getRecipeManager(), "client_recipe_sync");
    }
}
