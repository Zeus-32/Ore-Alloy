package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.OreHostVariantCatalog;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreAndAlloy.MODID);

    private static final Comparator<String> MATERIAL_COMPARATOR = Comparator
            .comparingInt(ModCreativeTabs::formRank)
            .thenComparing(ModCreativeTabs::materialSortKey)
            .thenComparing(Comparator.naturalOrder());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB =
            CREATIVE_TABS.register("materials", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + OreAndAlloy.MODID + ".material_items"))
                    .icon(ModCreativeTabs::itemTabIcon)
                    .displayItems((parameters, output) -> {
                        appendMaterialItems(output);
                        appendFluidBuckets(output);
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIAL_BLOCKS_TAB =
            CREATIVE_TABS.register("material_blocks", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + OreAndAlloy.MODID + ".material_blocks"))
                    .icon(ModCreativeTabs::blockTabIcon)
                    .displayItems((parameters, output) -> {
                        appendOreBlocks(output);
                        appendRawBlocks(output);
                        appendStorageBlocks(output);
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = MATERIALS_TAB;

    private ModCreativeTabs() {}

    private static ItemStack itemTabIcon() {
        DeferredItem<Item> ironIngot = ModItems.materialItems().get("iron_ingot");
        return ironIngot == null ? new ItemStack(Items.IRON_INGOT) : new ItemStack(ironIngot.value());
    }

    private static ItemStack blockTabIcon() {
        DeferredItem<BlockItem> ironBlock = ModStorageBlocks.storageBlockItems().get("iron_block");
        return ironBlock == null ? new ItemStack(Items.IRON_BLOCK) : new ItemStack(ironBlock.value());
    }

    private static void appendMaterialItems(CreativeModeTab.Output output) {
        List<String> ids = new ArrayList<>(ModItems.materialItems().keySet());
        ids.sort(MATERIAL_COMPARATOR);
        for (String id : ids) {
            output.accept(ModItems.materialItems().get(id).value());
        }
    }

    private static void appendFluidBuckets(CreativeModeTab.Output output) {
        ModFluids.buckets().keySet().stream()
                .sorted()
                .map(ModFluids.buckets()::get)
                .forEach(item -> output.accept(item.value()));
    }

    private static void appendOreBlocks(CreativeModeTab.Output output) {
        Map<String, DeferredItem<BlockItem>> items = ModOreBlocks.oreBlockItems();
        Set<String> emitted = new LinkedHashSet<>();

        for (String rawVariant : orderedRawVariants()) {
            for (OreHostVariantCatalog.HostVariant host : OreHostVariantCatalog.hostVariants()) {
                String id = host.blockId(rawVariant);
                DeferredItem<BlockItem> item = items.get(id);
                if (item == null) continue;
                output.accept(item.value());
                emitted.add(id);
            }
        }

        items.keySet().stream()
                .filter(id -> !emitted.contains(id))
                .sorted()
                .map(items::get)
                .forEach(item -> output.accept(item.value()));
    }

    private static void appendStorageBlocks(CreativeModeTab.Output output) {
        Map<String, DeferredItem<BlockItem>> items = ModStorageBlocks.storageBlockItems();
        ModStorageBlocks.blockIdByMaterial().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(items::get)
                .filter(java.util.Objects::nonNull)
                .forEach(item -> output.accept(item.value()));
    }

    private static void appendRawBlocks(CreativeModeTab.Output output) {
        Map<String, DeferredItem<BlockItem>> items = ModRawBlocks.rawBlockItems();
        Set<String> emitted = new LinkedHashSet<>();

        for (String rawVariant : orderedRawVariants()) {
            String id = "raw_" + rawVariant + "_block";
            DeferredItem<BlockItem> item = items.get(id);
            if (item == null) continue;
            output.accept(item.value());
            emitted.add(id);
        }

        items.keySet().stream()
                .filter(id -> !emitted.contains(id))
                .sorted(MATERIAL_COMPARATOR)
                .map(ModRawBlocks.rawBlockItems()::get)
                .forEach(item -> output.accept(item.value()));
    }

    private static List<String> orderedRawVariants() {
        List<String> discovered = RawVariantCatalog.collectRawVariants(ModItems.materialItems().keySet());
        Map<String, List<String>> byMaterial = new LinkedHashMap<>();
        for (String rawVariant : discovered) {
            String material = RawMaterialMappings.materialForRawVariant(rawVariant).orElse(rawVariant);
            byMaterial.computeIfAbsent(MaterialItemOrder.canonicalMaterialToken(material), ignored -> new ArrayList<>())
                    .add(rawVariant);
        }

        List<String> out = new ArrayList<>(discovered.size());
        byMaterial.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            Set<String> remaining = new LinkedHashSet<>(entry.getValue());
            for (String preferred : RawMaterialMappings.rawVariantsForMaterial(entry.getKey())) {
                if (remaining.remove(preferred)) out.add(preferred);
            }
            remaining.stream().sorted().forEach(out::add);
        });
        return out;
    }

    private static int formRank(String id) {
        return MaterialItemOrder.formToken(id)
                .map(MaterialItemOrder::formTokenRank)
                .orElse(Integer.MAX_VALUE);
    }

    private static String materialSortKey(String id) {
        return MaterialItemOrder.preferredItemMaterialToken(MaterialItemOrder.materialPart(id));
    }
}
