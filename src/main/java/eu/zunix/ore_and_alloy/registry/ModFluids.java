package eu.zunix.ore_and_alloy.registry;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;


public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, OreAndAlloy.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, OreAndAlloy.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreAndAlloy.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreAndAlloy.MODID);

    private static final Map<MetalMaterial, MoltenFluidSet> MOLTEN_FLUIDS = new EnumMap<>(MetalMaterial.class);

    static {
        for (MetalMaterial material : MetalMaterial.values()) {
            MOLTEN_FLUIDS.put(material, registerMolten(material));
        }
    }

    private ModFluids() {}

    public static MoltenFluidSet molten(MetalMaterial material) {
        return MOLTEN_FLUIDS.get(material);
    }

    public static Map<MetalMaterial, MoltenFluidSet> allMolten() {
        return Collections.unmodifiableMap(MOLTEN_FLUIDS);
    }

    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        for (Map.Entry<MetalMaterial, MoltenFluidSet> entry : MOLTEN_FLUIDS.entrySet()) {
            final int tint = moltenTint(entry.getKey());
            event.registerFluidType(new IClientFluidTypeExtensions() {
                private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/lava_still");
                private static final ResourceLocation FLOW = ResourceLocation.withDefaultNamespace("block/lava_flow");

                @Override
                public ResourceLocation getStillTexture() {
                    return STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return FLOW;
                }

                @Override
                public int getTintColor() {
                    return tint;
                }
            }, entry.getValue().type().value());
        }
    }

    private static MoltenFluidSet registerMolten(MetalMaterial material) {
        String fluidName = material.moltenFluidPath();

        DeferredHolder<FluidType, FluidType> type = FLUID_TYPES.register(fluidName, () -> new FluidType(
                FluidType.Properties.create()
                        .density(2600)
                        .viscosity(7000)
                        .temperature(1300)
                        .lightLevel(10)
                        .canExtinguish(false)
                        .canConvertToSource(false)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
        ));

        FluidRefs refs = new FluidRefs();
        Supplier<Fluid> sourceSupplier = () -> refs.source.value();
        Supplier<Fluid> flowingSupplier = () -> refs.flowing.value();
        Supplier<FlowingFluid> sourceFlowingSupplier = () -> refs.source.value();

        DeferredItem<Item> bucket = ITEMS.register(fluidName + "_bucket", key -> new BucketItem(
                sourceSupplier.get(),
                new Item.Properties()
                        .stacksTo(1)
                        .craftRemainder(Items.BUCKET)
        ));
        refs.bucket = bucket;

        DeferredBlock<LiquidBlock> block = BLOCKS.register(fluidName, key -> new LiquidBlock(
                sourceFlowingSupplier.get(),
                BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)
        ));
        refs.block = block;

        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(type::value, sourceSupplier, flowingSupplier)
                .bucket(bucket::value)
                .block(block::value)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(120.0F);

        DeferredHolder<Fluid, BaseFlowingFluid.Source> source = FLUIDS.register(fluidName, () -> new BaseFlowingFluid.Source(properties));
        DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing = FLUIDS.register("flowing_" + fluidName, () -> new BaseFlowingFluid.Flowing(properties));
        refs.source = source;
        refs.flowing = flowing;

        return new MoltenFluidSet(material, type, source, flowing, block, bucket);
    }

    private static int moltenTint(MetalMaterial material) {
        return switch (material) {
            case COPPER -> 0xFFFF874A;
            case GOLD, ELECTRUM -> 0xFFFFD25D;
            case IRON, STEEL, INVAR -> 0xFFE7C69A;
            case BRASS, BRONZE -> 0xFFE9A84F;
            case OSMIUM, TITANIUM, IRIDIUM -> 0xFFC4CCDE;
            case URANIUM -> 0xFF9CC86D;
            case CHROME, NICKEL, PLATINUM, SILVER, ALUMINUM, COBALT, TIN, LEAD, ZINC, CONSTANTAN -> 0xFFD8C29E;
        };
    }

    public record MoltenFluidSet(
            MetalMaterial material,
            DeferredHolder<FluidType, FluidType> type,
            DeferredHolder<Fluid, BaseFlowingFluid.Source> source,
            DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing,
            DeferredBlock<LiquidBlock> block,
            DeferredItem<Item> bucket
    ) {}

    private static final class FluidRefs {
        private DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
        private DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
        private DeferredBlock<LiquidBlock> block;
        private DeferredItem<Item> bucket;
    }
}
