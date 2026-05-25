package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.EnchantmentMenu$3")
public abstract class EnchantmentMenuLapisSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void oreAndAlloy$allowCanonicalLapis(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (VanillaItemAliasResolver.isCanonicalForVanilla(stack, Items.LAPIS_LAZULI)) {
            cir.setReturnValue(true);
        }
    }
}
