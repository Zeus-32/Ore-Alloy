package eu.zunix.ore_and_alloy;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import eu.zunix.ore_and_alloy.config.OAConfig;
import eu.zunix.ore_and_alloy.registry.ModItems;
import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import eu.zunix.ore_and_alloy.integration.VanillaBehaviorUnifier;
import eu.zunix.ore_and_alloy.integration.VanillaTradeUnifier;
import eu.zunix.ore_and_alloy.item.PeriodicTooltip;
import eu.zunix.ore_and_alloy.registry.ModAttachments;
import eu.zunix.ore_and_alloy.registry.ModFluids;
import eu.zunix.ore_and_alloy.registry.ModCreativeTabs;
import eu.zunix.ore_and_alloy.registry.ModLootModifiers;
import eu.zunix.ore_and_alloy.registry.ModOreBlocks;
import eu.zunix.ore_and_alloy.registry.ModStorageBlocks;
import eu.zunix.ore_and_alloy.worldgen.OAVeinWorldgenController;


@Mod(OreAndAlloy.MODID)
public class OreAndAlloy {
    public static final String MODID = "ore_and_alloy";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String PURPLE = "\u001B[35m";

    public OreAndAlloy(IEventBus modEventBus, ModContainer modContainer) {
        printSignature();

        OAConfig.register(modContainer);

        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModOreBlocks.BLOCKS.register(modEventBus);
        ModStorageBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.BLOCKS.register(modEventBus);
        ModFluids.ITEMS.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(PeriodicTooltip::onTooltip);

        modEventBus.addListener(this::commonSetup);

        OAVeinWorldgenController.register();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Ore & Alloy common setup");
        LOGGER.info("[{}] Startup config: custom_vein_worldgen_enabled={}, periodic_tooltips_enabled={}, unification_audit_enabled={}, strict_mode_enabled={}, strict_mode_fail_fast_enabled={}, snapshot_export_enabled={}",
                MODID,
                OAConfig.customVeinWorldgenEnabled(),
                OAConfig.periodicTooltipsEnabled(),
                OAConfig.unificationAuditEnabled(),
                OAConfig.unificationStrictModeEnabled(),
                OAConfig.unificationStrictModeFailFastEnabled(),
                OAConfig.unificationSnapshotExportEnabled());

        RecipeInterceptor.register();
        VanillaTradeUnifier.register();
        VanillaBehaviorUnifier.register();
    }

    private void printSignature() {
        LOGGER.info(PURPLE + "========================================" + RESET);
        LOGGER.info(BOLD + CYAN + "Ore & Alloy initialized!" + RESET);
        LOGGER.info(CYAN + "Mod made by Zeus_32." + RESET);
        LOGGER.info(CYAN + "Checkout my website: " + GREEN + "https://zunix.eu/tge" + RESET);
        LOGGER.info(PURPLE + "========================================" + RESET);
    }
}
