package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.StorageBlockCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModStorageBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreAndAlloy.MODID);

    private static final Map<String, DeferredBlock<Block>> STORAGE_BLOCKS_BY_ID = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> STORAGE_BLOCK_ITEMS = new LinkedHashMap<>();
    private static final Map<String, String> STORAGE_BLOCK_ID_BY_MATERIAL = new LinkedHashMap<>();

    private ModStorageBlocks() {}

    public static void registerStorageBlocks(Collection<String> materialItemIds, DeferredRegister.Items itemRegister) {
        Map<String, String> baseFormsByMaterial = StorageBlockCatalog.collectStorageBlockBaseForms(materialItemIds);

        for (Map.Entry<String, String> entry : baseFormsByMaterial.entrySet()) {
            String material = entry.getKey();
            String baseForm = entry.getValue();
            String blockId = StorageBlockCatalog.blockIdForMaterial(material);

            if (STORAGE_BLOCKS_BY_ID.containsKey(blockId)) continue;

            Block baseBlock = resolveBaseStorageBlock(blockId, baseForm);
            DeferredBlock<Block> storageBlock = BLOCKS.register(blockId,
                    key -> createStorageBlock(blockId, baseBlock));
            DeferredItem<BlockItem> storageItem = itemRegister.registerSimpleBlockItem(blockId, storageBlock);

            STORAGE_BLOCKS_BY_ID.put(blockId, storageBlock);
            STORAGE_BLOCK_ITEMS.put(blockId, storageItem);
            STORAGE_BLOCK_ID_BY_MATERIAL.put(material, blockId);
        }
    }

    public static Map<String, DeferredBlock<Block>> allStorageBlocksById() {
        return Collections.unmodifiableMap(STORAGE_BLOCKS_BY_ID);
    }

    public static Map<String, DeferredItem<BlockItem>> storageBlockItems() {
        return Collections.unmodifiableMap(STORAGE_BLOCK_ITEMS);
    }

    public static Map<String, String> blockIdByMaterial() {
        return Collections.unmodifiableMap(STORAGE_BLOCK_ID_BY_MATERIAL);
    }

    private static Block resolveBaseStorageBlock(String blockId, String baseForm) {
        ResourceLocation vanillaId = ResourceLocation.withDefaultNamespace(blockId);
        Block vanillaBlock = BuiltInRegistries.BLOCK.get(vanillaId);
        if (vanillaBlock != Blocks.AIR) {
            return vanillaBlock;
        }

        if ("gem".equals(baseForm)) {
            return Blocks.DIAMOND_BLOCK;
        }
        if ("dust".equals(baseForm)) {
            return Blocks.REDSTONE_BLOCK;
        }
        return Blocks.IRON_BLOCK;
    }

    private static Block createStorageBlock(String blockId, Block baseBlock) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(baseBlock).requiresCorrectToolForDrops();
        if ("redstone_block".equals(blockId)) {
            return new PoweredBlock(properties);
        }
        return new Block(properties);
    }
}
