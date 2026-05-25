package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateAliasMixin {
    @Redirect(
            method = "test",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/core/HolderSet;)Z")
    )
    private boolean oreAndAlloy$matchCanonicalAliases(ItemStack stack, HolderSet<Item> holderSet) {
        return VanillaItemAliasResolver.matchesHolderSetThroughAliases(stack, holderSet);
    }
}
