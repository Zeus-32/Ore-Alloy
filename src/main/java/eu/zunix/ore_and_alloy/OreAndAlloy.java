package eu.zunix.ore_and_alloy;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import eu.zunix.ore_and_alloy.registry.ModItems;
import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import eu.zunix.ore_and_alloy.integration.VanillaBehaviorUnifier;
import eu.zunix.ore_and_alloy.integration.VanillaTradeUnifier;
import eu.zunix.ore_and_alloy.item.PeriodicTooltip;
import eu.zunix.ore_and_alloy.registry.ModCreativeTabs;
import eu.zunix.ore_and_alloy.registry.ModFluids;
import eu.zunix.ore_and_alloy.registry.ModOreBlocks;
import eu.zunix.ore_and_alloy.registry.ModRawBlocks;
import eu.zunix.ore_and_alloy.registry.ModStorageBlocks;


@Mod(OreAndAlloy.MODID)
public class OreAndAlloy {
    public static final String MODID = "ore_and_alloy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OreAndAlloy(IEventBus modEventBus) {
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.BLOCKS.register(modEventBus);
        ModOreBlocks.BLOCKS.register(modEventBus);
        ModRawBlocks.BLOCKS.register(modEventBus);
        ModStorageBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(PeriodicTooltip::onTooltip);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        RecipeInterceptor.register();
        VanillaTradeUnifier.register();
        VanillaBehaviorUnifier.register();
    }

}
