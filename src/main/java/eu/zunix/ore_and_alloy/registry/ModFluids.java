package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialFluidCatalog;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, OreAndAlloy.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, OreAndAlloy.MODID);
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OreAndAlloy.MODID);

    private static final Map<String, DeferredHolder<FluidType, FluidType>> TYPES = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Fluid, FlowingFluid>> STILL = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Fluid, FlowingFluid>> FLOWING = new LinkedHashMap<>();
    private static final Map<String, DeferredBlock<LiquidBlock>> BLOCK_ITEMS = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<Item>> BUCKETS = new LinkedHashMap<>();

    static {
        for (MaterialFluidCatalog.Entry fluid : MaterialFluidCatalog.entries()) {
            registerFluid(fluid.id());
        }
    }

    private ModFluids() {}

    public static void registerBuckets(DeferredRegister.Items itemRegister) {
        for (MaterialFluidCatalog.Entry fluid : MaterialFluidCatalog.entries()) {
            String bucketId = fluid.bucketItemId();
            BUCKETS.computeIfAbsent(bucketId, ignored -> itemRegister.register(
                    bucketId,
                    () -> new BucketItem(STILL.get(fluid.id()).value(), new Item.Properties()
                            .craftRemainder(Items.BUCKET)
                            .stacksTo(1))
            ));
        }
    }

    public static Map<String, DeferredItem<Item>> buckets() {
        return Collections.unmodifiableMap(BUCKETS);
    }

    public static Map<String, DeferredHolder<FluidType, FluidType>> fluidTypes() {
        return Collections.unmodifiableMap(TYPES);
    }

    private static void registerFluid(String id) {
        DeferredHolder<FluidType, FluidType> type = FLUID_TYPES.register(id, () -> new FluidType(FluidType.Properties.create()));
        TYPES.put(id, type);

        DeferredHolder<Fluid, FlowingFluid> still = FLUIDS.register(id, () -> new BaseFlowingFluid.Source(properties(id)));
        STILL.put(id, still);

        DeferredHolder<Fluid, FlowingFluid> flowing = FLUIDS.register("flowing_" + id, () -> new BaseFlowingFluid.Flowing(properties(id)));
        FLOWING.put(id, flowing);

        DeferredBlock<LiquidBlock> block = BLOCKS.register(id, () -> new LiquidBlock(still.value(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
        BLOCK_ITEMS.put(id, block);
    }

    private static BaseFlowingFluid.Properties properties(String id) {
        return new BaseFlowingFluid.Properties(
                () -> TYPES.get(id).value(),
                () -> STILL.get(id).value(),
                () -> FLOWING.get(id).value()
        ).bucket(() -> bucketItem(id))
                .block(() -> BLOCK_ITEMS.get(id).value());
    }

    private static Item bucketItem(String fluidId) {
        DeferredItem<Item> bucket = BUCKETS.get(fluidId + "_bucket");
        return bucket == null ? Items.AIR : bucket.value();
    }
}
