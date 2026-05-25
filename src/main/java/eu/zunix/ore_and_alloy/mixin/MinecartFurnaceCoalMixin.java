package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartFurnace.class)
public abstract class MinecartFurnaceCoalMixin {
    @Redirect(
            method = "interact",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean oreAndAlloy$acceptCanonicalCoalFuel(Ingredient ingredient, ItemStack stack) {
        return ingredient.test(stack) || VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.COAL);
    }
}
