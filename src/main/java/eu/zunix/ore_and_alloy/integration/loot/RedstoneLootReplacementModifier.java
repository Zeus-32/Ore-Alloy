package eu.zunix.ore_and_alloy.integration.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.zunix.ore_and_alloy.integration.RecipeInterceptor;
import eu.zunix.ore_and_alloy.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class RedstoneLootReplacementModifier extends LootModifier {
    public static final MapCodec<RedstoneLootReplacementModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, RedstoneLootReplacementModifier::new)
    );

    private static final String OA_REDSTONE_ID = "redstone";

    public RedstoneLootReplacementModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var redstoneHolder = ModItems.materialItems().get(OA_REDSTONE_ID);
        Item fallbackRedstone = redstoneHolder == null ? Items.REDSTONE : redstoneHolder.value();
        var aliasMap = RecipeInterceptor.buildAliasMapSnapshot();

        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack stack = generatedLoot.get(i);
            Item replacementItem = aliasMap.get(stack.getItem());
            if (replacementItem == null && stack.is(Items.REDSTONE)) {
                replacementItem = fallbackRedstone;
            }
            if (replacementItem == null || replacementItem == stack.getItem()) continue;
            generatedLoot.set(i, stack.transmuteCopy(replacementItem, stack.getCount()));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
