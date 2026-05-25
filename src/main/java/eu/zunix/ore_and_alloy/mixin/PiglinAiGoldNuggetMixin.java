package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PiglinAi.class)
public abstract class PiglinAiGoldNuggetMixin {
    @Redirect(
            method = "pickUpItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private static boolean oreAndAlloy$acceptCanonicalGoldNuggetInPickup(ItemStack stack, Item item) {
        if (item == Items.GOLD_NUGGET) {
            return VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.GOLD_NUGGET);
        }
        return stack.is(item);
    }

    @Redirect(
            method = "wantsToPickup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private static boolean oreAndAlloy$acceptCanonicalGoldNuggetInWantsToPickup(ItemStack stack, Item item) {
        if (item == Items.GOLD_NUGGET) {
            return VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.GOLD_NUGGET);
        }
        return stack.is(item);
    }

    @Redirect(
            method = "isBarterCurrency",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private static boolean oreAndAlloy$acceptCanonicalGoldInBarterCheck(ItemStack stack, Item item) {
        if (item == Items.GOLD_INGOT) {
            return VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.GOLD_INGOT);
        }
        return stack.is(item);
    }
}
