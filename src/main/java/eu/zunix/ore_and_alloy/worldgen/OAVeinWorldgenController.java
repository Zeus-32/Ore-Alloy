package eu.zunix.ore_and_alloy.worldgen;

import eu.zunix.ore_and_alloy.config.OAConfig;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import eu.zunix.ore_and_alloy.registry.ModAttachments;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinCatalog;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinDefinition;
import eu.zunix.ore_and_alloy.worldgen.vein.OAVeinOreResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OAVeinWorldgenController {
    private static final int MAX_CHUNKS_PER_TICK = 4;
    private static final long SEED_SALT = 0x6A09E667F3BCC909L;

    private static final Map<ResourceLocation, LinkedHashSet<Long>> QUEUED_CHUNKS = new ConcurrentHashMap<>();
    private static final Object QUEUE_LOCK = new Object();

    private OAVeinWorldgenController() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(OAVeinWorldgenController::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(OAVeinWorldgenController::onLevelTickPost);
        NeoForge.EVENT_BUS.addListener(OAVeinWorldgenController::onLevelUnload);
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!OAConfig.customVeinWorldgenEnabled()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        ChunkAccess chunk = event.getChunk();
        if (isWorldgenProcessed(chunk)) return;

        ResourceLocation dimension = serverLevel.dimension().location();
        long packedChunk = chunk.getPos().toLong();

        synchronized (QUEUE_LOCK) {
            QUEUED_CHUNKS.computeIfAbsent(dimension, ignored -> new LinkedHashSet<>()).add(packedChunk);
        }
    }

    private static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!OAConfig.customVeinWorldgenEnabled()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        List<Long> queued = pollQueuedChunks(level.dimension().location(), MAX_CHUNKS_PER_TICK);
        if (queued.isEmpty()) return;

        for (long packed : queued) {
            ChunkPos chunkPos = new ChunkPos(packed);
            generateInChunk(level, chunkPos);
            markChunkProcessed(level, chunkPos);
        }
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        synchronized (QUEUE_LOCK) {
            QUEUED_CHUNKS.remove(level.dimension().location());
        }
    }

    private static List<Long> pollQueuedChunks(ResourceLocation dimension, int maxCount) {
        synchronized (QUEUE_LOCK) {
            LinkedHashSet<Long> queue = QUEUED_CHUNKS.get(dimension);
            if (queue == null || queue.isEmpty()) return List.of();

            List<Long> out = new ArrayList<>(Math.min(maxCount, queue.size()));
            Iterator<Long> it = queue.iterator();
            while (it.hasNext() && out.size() < maxCount) {
                out.add(it.next());
                it.remove();
            }
            if (queue.isEmpty()) {
                QUEUED_CHUNKS.remove(dimension);
            }
            return out;
        }
    }

    private static boolean isWorldgenProcessed(ChunkAccess chunk) {
        return Boolean.TRUE.equals(chunk.getExistingDataOrNull(ModAttachments.VEIN_WORLDGEN_PROCESSED));
    }

    private static void markChunkProcessed(ServerLevel level, ChunkPos chunkPos) {
        var loadedChunk = level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
        if (loadedChunk != null) {
            loadedChunk.setData(ModAttachments.VEIN_WORLDGEN_PROCESSED, true);
        }
    }

    private static void generateInChunk(ServerLevel level, ChunkPos chunkPos) {
        RandomSource random = RandomSource.create(mixSeed(level.getSeed(), chunkPos.x, chunkPos.z));
        ResourceLocation dimension = level.dimension().location();

        for (OAVeinDefinition vein : OAVeinCatalog.definitions()) {
            if (!dimension.equals(vein.dimensionId())) continue;

            List<Block> ores = OAVeinOreResolver.resolveBlocks(vein);
            if (ores.isEmpty()) continue;

            int minY = Math.max(level.getMinBuildHeight(), vein.minY());
            int maxY = Math.min(level.getMaxBuildHeight() - 1, vein.maxY());
            if (minY > maxY) continue;

            for (int attempt = 0; attempt < vein.attemptsPerChunk(); attempt++) {
                if (vein.chanceDenominator() > 1 && random.nextInt(vein.chanceDenominator()) != 0) {
                    continue;
                }
                placeSingleVein(level, chunkPos, vein, ores, minY, maxY, random);
            }
        }
    }

    private static void placeSingleVein(
            ServerLevel level,
            ChunkPos chunkPos,
            OAVeinDefinition vein,
            List<Block> ores,
            int minY,
            int maxY,
            RandomSource random
    ) {
        int baseX = chunkPos.getMinBlockX() + random.nextInt(16);
        int baseY = Mth.nextInt(random, minY, maxY);
        int baseZ = chunkPos.getMinBlockZ() + random.nextInt(16);
        int minX = chunkPos.getMinBlockX();
        int maxX = chunkPos.getMaxBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxZ = chunkPos.getMaxBlockZ();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int horizontalSpread = Math.max(2, vein.size() / 4);
        int verticalSpread = Math.max(1, vein.size() / 10);

        for (int i = 0; i < vein.size(); i++) {
            int x = baseX + random.nextInt(horizontalSpread + 1) - random.nextInt(horizontalSpread + 1);
            int y = baseY + random.nextInt(verticalSpread + 1) - random.nextInt(verticalSpread + 1);
            int z = baseZ + random.nextInt(horizontalSpread + 1) - random.nextInt(horizontalSpread + 1);
            x = Mth.clamp(x, minX, maxX);
            z = Mth.clamp(z, minZ, maxZ);

            cursor.set(x, y, z);
            if (!level.isInWorldBounds(cursor)) continue;

            BlockState existing = level.getBlockState(cursor);
            if (!isReplaceableStone(existing)) continue;

            Block ore = selectOreForHost(existing, ores, random);
            level.setBlock(cursor, ore.defaultBlockState(), 2);
        }
    }

    private static boolean isReplaceableStone(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES)
                || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.END_STONE);
    }

    private static Block selectOreForHost(BlockState hostState, List<Block> ores, RandomSource random) {
        if (ores.size() == 1) return ores.getFirst();

        String wantedHostToken = hostTokenForState(hostState);
        List<Block> preferred = new ArrayList<>();
        for (Block ore : ores) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(ore);
            if (id == null) continue;
            if (wantedHostToken.equals(RawVariantCatalog.hostTokenForOreBlockId(id.getPath()))) {
                preferred.add(ore);
            }
        }

        if (!preferred.isEmpty()) {
            return preferred.get(random.nextInt(preferred.size()));
        }
        return ores.get(random.nextInt(ores.size()));
    }

    private static String hostTokenForState(BlockState state) {
        if (state.is(Blocks.DEEPSLATE)) return "deepslate";
        if (state.is(Blocks.TUFF)) return "tuff";
        if (state.is(Blocks.ANDESITE)) return "andesite";
        if (state.is(Blocks.DIORITE)) return "diorite";
        if (state.is(Blocks.GRANITE)) return "granite";
        if (state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) return "deepslate";
        if (state.is(BlockTags.BASE_STONE_NETHER) || state.is(Blocks.NETHERRACK)) return "nether";
        if (state.is(Blocks.END_STONE)) return "end";
        return "stone";
    }

    private static long mixSeed(long worldSeed, int chunkX, int chunkZ) {
        long x = 341873128712L * chunkX;
        long z = 132897987541L * chunkZ;
        long seed = worldSeed ^ x ^ z ^ SEED_SALT;
        seed ^= (seed >>> 33);
        seed *= 0xff51afd7ed558ccdL;
        seed ^= (seed >>> 33);
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= (seed >>> 33);
        return seed;
    }
}
