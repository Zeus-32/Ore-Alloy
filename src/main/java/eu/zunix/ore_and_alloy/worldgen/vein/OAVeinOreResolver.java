package eu.zunix.ore_and_alloy.worldgen.vein;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OAVeinOreResolver {
    private static final Map<ResourceLocation, List<Block>> TAG_BLOCK_CACHE = new ConcurrentHashMap<>();

    private OAVeinOreResolver() {}

    public static void clearCaches() {
        TAG_BLOCK_CACHE.clear();
    }

    public static List<Block> resolveBlocks(OAVeinDefinition definition) {
        LinkedHashSet<Block> resolved = new LinkedHashSet<>();

        for (ResourceLocation blockId : definition.fallbackBlocks()) {
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            if (block != Blocks.AIR) {
                resolved.add(block);
            }
        }
        if (!resolved.isEmpty()) {
            return List.copyOf(resolved);
        }

        for (ResourceLocation tagId : definition.oreTags()) {
            resolved.addAll(blocksForTag(tagId));
        }

        return List.copyOf(resolved);
    }

    public static List<ItemStack> resolveDisplayStacks(OAVeinDefinition definition, int maxStacks) {
        List<ItemStack> out = new ArrayList<>();
        for (Block block : resolveBlocks(definition)) {
            ItemStack stack = block.asItem().getDefaultInstance();
            if (stack.isEmpty()) continue;
            out.add(stack);
            if (out.size() >= maxStacks) break;
        }
        return List.copyOf(out);
    }

    private static List<Block> blocksForTag(ResourceLocation tagId) {
        return TAG_BLOCK_CACHE.computeIfAbsent(tagId, id -> {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, id);
            List<Block> out = new ArrayList<>();
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block == Blocks.AIR) continue;
                if (block.builtInRegistryHolder().is(tag)) {
                    out.add(block);
                }
            }
            out.sort(Comparator.comparing(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
            return List.copyOf(out);
        });
    }
}
