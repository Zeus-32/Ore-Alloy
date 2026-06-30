package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.RawBlockCatalog;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModRawBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreAndAlloy.MODID);

    private static final Map<String, DeferredBlock<Block>> RAW_BLOCKS_BY_ID = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> RAW_BLOCK_ITEMS = new LinkedHashMap<>();

    private ModRawBlocks() {}

    public static void registerRawBlocks(Collection<String> materialItemIds, DeferredRegister.Items itemRegister) {
        for (String rawVariant : RawBlockCatalog.collectRawBlockBaseItems(materialItemIds).keySet()) {
            String blockId = RawBlockCatalog.blockIdForRawVariant(rawVariant);
            if (RAW_BLOCKS_BY_ID.containsKey(blockId)) continue;

            DeferredBlock<Block> rawBlock = BLOCKS.register(blockId,
                    key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).requiresCorrectToolForDrops()));
            DeferredItem<BlockItem> rawBlockItem = itemRegister.registerSimpleBlockItem(blockId, rawBlock);

            RAW_BLOCKS_BY_ID.put(blockId, rawBlock);
            RAW_BLOCK_ITEMS.put(blockId, rawBlockItem);
        }
    }

    public static Map<String, DeferredBlock<Block>> allRawBlocksById() {
        return Collections.unmodifiableMap(RAW_BLOCKS_BY_ID);
    }

    public static Map<String, DeferredItem<BlockItem>> rawBlockItems() {
        return Collections.unmodifiableMap(RAW_BLOCK_ITEMS);
    }
}
