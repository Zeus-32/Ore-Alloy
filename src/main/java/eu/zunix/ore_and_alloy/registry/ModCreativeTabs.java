package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.OreHostVariantCatalog;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreAndAlloy.MODID);
    private static final Comparator<String> CREATIVE_MATERIAL_ITEM_COMPARATOR = Comparator
            .comparingInt(ModCreativeTabs::creativeFormRank)
            .thenComparing(ModCreativeTabs::creativeMaterialSortKey)
            .thenComparing(Comparator.naturalOrder());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + OreAndAlloy.MODID + ".main"))
                    .icon(ModCreativeTabs::tabIcon)
                    .displayItems((parameters, output) -> {
                        ModItems.prospector().ifPresent(item -> output.accept(item.value()));

                        appendOreBlockSets(output);
                        appendMaterialItems(output);
                        appendStandaloneItems(output);

                        ModFluids.allMolten().values().stream()
                                .map(set -> set.bucket().value())
                                .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()))
                                .forEach(output::accept);
                    })
                    .build()
    );

    private static ItemStack tabIcon() {
        return ModItems.prospector()
                .map(item -> new ItemStack(item.value()))
                .orElseGet(() -> ModItems.standaloneItems().values().stream()
                        .findFirst()
                        .map(item -> new ItemStack(item.value()))
                        .orElseGet(() -> ModItems.materialItems().values().stream()
                        .findFirst()
                        .map(item -> new ItemStack(item.value()))
                        .orElseGet(() -> new ItemStack(Items.IRON_INGOT))));
    }

    private static int creativeFormRank(String itemId) {
        return MaterialItemOrder.formToken(itemId)
                .map(MaterialItemOrder::formTokenRank)
                .orElse(Integer.MAX_VALUE);
    }

    private static String creativeMaterialSortKey(String itemId) {
        String canonical = MaterialItemOrder.materialPart(itemId);
        return MaterialItemOrder.preferredItemMaterialToken(canonical);
    }

    private static void appendMaterialItems(CreativeModeTab.Output output) {
        List<String> itemIds = new ArrayList<>(ModItems.materialItems().keySet());
        itemIds.sort(CREATIVE_MATERIAL_ITEM_COMPARATOR);
        for (String id : itemIds) {
            output.accept(ModItems.materialItems().get(id).value());
        }
    }

    private static void appendOreBlockSets(CreativeModeTab.Output output) {
        Map<String, DeferredItem<BlockItem>> oreItems = ModOreBlocks.oreBlockItems();
        if (oreItems.isEmpty()) return;

        Set<String> emitted = new LinkedHashSet<>();
        for (String rawVariant : orderedRawVariantsForCreativeTab()) {
            for (OreHostVariantCatalog.HostVariant host : OreHostVariantCatalog.hostVariants()) {
                String blockId = host.blockId(rawVariant);
                DeferredItem<BlockItem> item = oreItems.get(blockId);
                if (item == null) continue;

                output.accept(item.value());
                emitted.add(blockId);
            }
        }

        List<String> remaining = oreItems.keySet().stream()
                .filter(id -> !emitted.contains(id))
                .sorted()
                .toList();
        for (String blockId : remaining) {
            output.accept(oreItems.get(blockId).value());
        }
    }

    private static List<String> orderedRawVariantsForCreativeTab() {
        List<String> discoveredRawVariants = RawVariantCatalog.collectRawVariants(ModItems.materialItems().keySet());
        Map<String, List<String>> variantsByMaterial = new LinkedHashMap<>();

        for (String rawVariant : discoveredRawVariants) {
            String material = RawMaterialMappings.materialForRawVariant(rawVariant).orElse(rawVariant);
            String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(material);
            variantsByMaterial.computeIfAbsent(canonicalMaterial, ignored -> new ArrayList<>()).add(rawVariant);
        }

        List<String> materials = new ArrayList<>(variantsByMaterial.keySet());
        materials.sort(Comparator.comparing(material -> MaterialItemOrder.preferredItemMaterialToken(material)));

        List<String> out = new ArrayList<>(discoveredRawVariants.size());
        for (String material : materials) {
            List<String> discoveredForMaterial = variantsByMaterial.getOrDefault(material, List.of());
            Set<String> remaining = new LinkedHashSet<>(discoveredForMaterial);

            for (String preferredVariant : RawMaterialMappings.rawVariantsForMaterial(material)) {
                if (remaining.remove(preferredVariant)) {
                    out.add(preferredVariant);
                }
            }

            List<String> extras = new ArrayList<>(remaining);
            extras.sort(String::compareTo);
            out.addAll(extras);
        }

        return out;
    }

    private static void appendStandaloneItems(CreativeModeTab.Output output) {
        List<String> standaloneIds = new ArrayList<>(ModItems.standaloneItems().keySet());
        standaloneIds.sort(String::compareTo);
        for (String id : standaloneIds) {
            output.accept(ModItems.standaloneItems().get(id).value());
        }
    }

    private ModCreativeTabs() {}
}
