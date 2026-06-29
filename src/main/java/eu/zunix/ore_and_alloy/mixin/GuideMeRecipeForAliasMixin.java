package eu.zunix.ore_and_alloy.mixin;

import eu.zunix.ore_and_alloy.integration.VanillaItemAliasResolver;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "guideme.compiler.tags.RecipeCompiler$RecipeTypeMapping", remap = false)
public abstract class GuideMeRecipeForAliasMixin {
    @ModifyVariable(
            method = "tryCreate(Lnet/minecraft/world/item/crafting/RecipeManager;Lnet/minecraft/world/item/Item;)Lguideme/document/block/LytBlock;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Item oreAndAlloy$resolveRecipeForAlias(Item item) {
        return VanillaItemAliasResolver.canonicalFor(item);
    }
}
