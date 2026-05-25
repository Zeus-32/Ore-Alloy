package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.OreHostVariantCatalog;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModOreBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreAndAlloy.MODID);

    private static final Set<String> REGISTERED_RAW_VARIANTS = new LinkedHashSet<>();
    private static final Map<String, DeferredBlock<Block>> ORE_BLOCKS_BY_ID = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> ORE_BLOCK_ITEMS = new LinkedHashMap<>();

    private ModOreBlocks() {}

    public static void registerRawVariantOres(Collection<String> materialItemIds, DeferredRegister.Items itemRegister) {
        List<String> rawVariants = RawVariantCatalog.collectRawVariants(materialItemIds);
        for (String variant : rawVariants) {
            registerOreBlocksIfMissing(variant, itemRegister);
        }
    }

    public static Map<String, DeferredBlock<Block>> allOreBlocksById() {
        return Collections.unmodifiableMap(ORE_BLOCKS_BY_ID);
    }

    public static Map<String, DeferredItem<BlockItem>> oreBlockItems() {
        return Collections.unmodifiableMap(ORE_BLOCK_ITEMS);
    }

    private static void registerOreBlocksIfMissing(String rawVariant, DeferredRegister.Items itemRegister) {
        if (!REGISTERED_RAW_VARIANTS.add(rawVariant)) return;

        for (OreHostVariantCatalog.HostVariant host : OreHostVariantCatalog.hostVariants()) {
            String blockId = host.blockId(rawVariant);
            if (ORE_BLOCKS_BY_ID.containsKey(blockId)) continue;

            Block baseBlock = resolveBaseBlock(host.baseBlockId());
            DeferredBlock<Block> oreBlock = BLOCKS.register(blockId,
                    key -> new Block(BlockBehaviour.Properties.ofFullCopy(baseBlock).requiresCorrectToolForDrops()));
            DeferredItem<BlockItem> oreItem = itemRegister.registerSimpleBlockItem(blockId, oreBlock);

            ORE_BLOCKS_BY_ID.put(blockId, oreBlock);
            ORE_BLOCK_ITEMS.put(blockId, oreItem);
        }
    }

    private static Block resolveBaseBlock(String baseBlockId) {
        ResourceLocation id = ResourceLocation.withDefaultNamespace(baseBlockId);
        Block base = BuiltInRegistries.BLOCK.get(id);
        return base == Blocks.AIR ? Blocks.STONE : base;
    }
}
