package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(FireworkStarRecipe.class)
public abstract class FireworkStarRecipeMixin {
    @Shadow
    @Final
    private static Ingredient SHAPE_INGREDIENT;

    @Shadow
    @Final
    private static Ingredient TRAIL_INGREDIENT;

    @Shadow
    @Final
    private static Map<Item, FireworkExplosion.Shape> SHAPE_BY_ITEM;

    @Redirect(
            method = "matches",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean oreAndAlloy$acceptCanonicalInputsInMatches(Ingredient ingredient, ItemStack stack) {
        return acceptsAliasCompatibleInput(ingredient, stack);
    }

    @Redirect(
            method = "assemble",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean oreAndAlloy$acceptCanonicalInputsInAssemble(Ingredient ingredient, ItemStack stack) {
        return acceptsAliasCompatibleInput(ingredient, stack);
    }

    @Redirect(
            method = "assemble",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object oreAndAlloy$mapCanonicalGoldNuggetShape(Map<?, ?> map, Object key) {
        if (map == SHAPE_BY_ITEM && key instanceof Item item && VanillaItemAliasResolver.isCanonicalItemForVanilla(item, Items.GOLD_NUGGET)) {
            return SHAPE_BY_ITEM.get(Items.GOLD_NUGGET);
        }
        return map.get(key);
    }

    private static boolean acceptsAliasCompatibleInput(Ingredient ingredient, ItemStack stack) {
        if (ingredient.test(stack)) {
            return true;
        }
        if (ingredient == TRAIL_INGREDIENT && VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.DIAMOND)) {
            return true;
        }
        return ingredient == SHAPE_INGREDIENT && VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.GOLD_NUGGET);
    }
}
