package eu.zunix.ore_and_alloy.integration.jei.vein;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinDefinition;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinOreResolver;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinText;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public record JeiVeinInfoRecipe(
        OAVeinDefinition definition,
        List<List<ItemStack>> oreVariantGroups,
        List<ItemStack> oreStacks,
        List<ItemStack> rawStacks,
        String rawVariantsText
) {
    public static JeiVeinInfoRecipe fromDefinition(OAVeinDefinition definition) {
        List<List<ItemStack>> oreVariantGroups = resolveOreVariantGroups(definition, 14);
        List<ItemStack> oreStacks = flattenGroups(oreVariantGroups);
        if (oreStacks.isEmpty()) {
            oreStacks = OAVeinOreResolver.resolveDisplayStacks(definition, 14);
        }
        List<ItemStack> rawStacks = resolveRawStacks(definition, 10);
        String rawText = definition.rawVariants().stream()
                .map(OAVeinText::toDisplayName)
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
        return new JeiVeinInfoRecipe(definition, oreVariantGroups, oreStacks, rawStacks, rawText);
    }

    private static List<List<ItemStack>> resolveOreVariantGroups(OAVeinDefinition definition, int maxGroups) {
        List<List<ItemStack>> groups = new ArrayList<>();
        for (String rawVariant : definition.rawVariants()) {
            List<ItemStack> group = new ArrayList<>();
            for (String oreBlockId : RawVariantCatalog.oreBlockIds(rawVariant)) {
                ResourceLocation blockRl = ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, oreBlockId);
                Block block = BuiltInRegistries.BLOCK.get(blockRl);
                if (block == Blocks.AIR) continue;

                ItemStack stack = block.asItem().getDefaultInstance();
                if (stack.isEmpty()) continue;
                group.add(stack);
            }

            if (!group.isEmpty()) {
                groups.add(List.copyOf(group));
                if (groups.size() >= maxGroups) {
                    break;
                }
            }
        }
        return List.copyOf(groups);
    }

    private static List<ItemStack> flattenGroups(List<List<ItemStack>> groups) {
        List<ItemStack> out = new ArrayList<>();
        for (List<ItemStack> group : groups) {
            out.addAll(group);
        }
        return List.copyOf(out);
    }

    private static List<ItemStack> resolveRawStacks(OAVeinDefinition definition, int maxStacks) {
        List<ItemStack> out = new ArrayList<>();
        for (String variant : definition.rawVariants()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "raw_" + variant);
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == Items.AIR) continue;
            ItemStack stack = item.getDefaultInstance();
            if (stack.isEmpty()) continue;

            out.add(stack);
            if (out.size() >= maxStacks) break;
        }
        return List.copyOf(out);
    }
}
