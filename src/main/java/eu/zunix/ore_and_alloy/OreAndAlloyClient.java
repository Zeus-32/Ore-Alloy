package eu.zunix.ore_and_alloy;

import eu.zunix.ore_and_alloy.core.MaterialFluidCatalog;
import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import eu.zunix.ore_and_alloy.registry.ModFluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = OreAndAlloy.MODID, dist = Dist.CLIENT)
public final class OreAndAlloyClient {
    private static final ResourceLocation FLUID_STILL =
            ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "block/fluid_still");
    private static final ResourceLocation FLUID_FLOW =
            ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "block/fluid_flow");

    public OreAndAlloyClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerClientExtensions);
        NeoForge.EVENT_BUS.addListener(this::onRecipesUpdated);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (MaterialFluidCatalog.Entry fluid : MaterialFluidCatalog.entries()) {
            var type = ModFluids.fluidTypes().get(fluid.id());
            if (type == null) continue;
            event.registerFluidType(clientTexturesFor(fluid), type.value());
        }
    }

    private static IClientFluidTypeExtensions clientTexturesFor(MaterialFluidCatalog.Entry fluid) {
        ResourceLocation still = fluid.metalLike()
                ? ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "block/fluid/" + fluid.id() + "_still")
                : FLUID_STILL;
        ResourceLocation flowing = fluid.metalLike()
                ? ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "block/fluid/" + fluid.id() + "_flow")
                : FLUID_FLOW;
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowing;
            }
        };
    }

    private void onRecipesUpdated(RecipesUpdatedEvent event) {
        RecipeInterceptor.rewriteNow(event.getRecipeManager(), "client_recipe_sync");
    }
}
