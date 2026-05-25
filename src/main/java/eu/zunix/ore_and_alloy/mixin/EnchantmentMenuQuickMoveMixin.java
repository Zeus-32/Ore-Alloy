package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.world.inventory.EnchantmentMenu.class)
public abstract class EnchantmentMenuQuickMoveMixin {
    @Redirect(
            method = "quickMoveStack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean oreAndAlloy$acceptCanonicalLapisInQuickMove(ItemStack stack, Item item) {
        if (item == Items.LAPIS_LAZULI) {
            return VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.LAPIS_LAZULI);
        }
        return stack.is(item);
    }
}
