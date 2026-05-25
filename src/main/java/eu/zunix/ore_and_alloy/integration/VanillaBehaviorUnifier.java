package eu.zunix.ore_and_alloy.integration;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;

public final class VanillaBehaviorUnifier {
    private static final int IRON_GOLEM_HEAL = 25;

    private VanillaBehaviorUnifier() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VanillaBehaviorUnifier::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(VanillaBehaviorUnifier::onAnvilUpdate);
        NeoForge.EVENT_BUS.addListener(VanillaBehaviorUnifier::onRegisterBrewingRecipes);
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof IronGolem golem)) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (held.isEmpty()) {
            return;
        }

        if (!isCanonicalForVanilla(held, Items.IRON_INGOT)) {
            return;
        }
        if (held.is(Items.IRON_INGOT)) {
            return;
        }

        float before = golem.getHealth();
        if (before >= golem.getMaxHealth()) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        golem.heal(IRON_GOLEM_HEAL);
        if (golem.getHealth() == before) {
            return;
        }

        float pitch = 1.0F + (golem.getRandom().nextFloat() - golem.getRandom().nextFloat()) * 0.2F;
        golem.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, pitch);
        held.consume(1, event.getEntity());
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!event.getOutput().isEmpty()) {
            return;
        }

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty()) {
            return;
        }
        if (!left.isDamageableItem() || !left.isDamaged()) {
            return;
        }
        if (left.getItem().isValidRepairItem(left, right)) {
            return;
        }

        ItemStack repairAlias = findRepairAlias(left, right);
        if (repairAlias.isEmpty()) {
            return;
        }

        ItemStack output = left.copy();
        int materialCount = applyMaterialRepair(output, right.getCount());
        if (materialCount <= 0) {
            return;
        }

        int renameCost = applyRename(output, left, event.getName());
        int oldRepairCost = output.getOrDefault(DataComponents.REPAIR_COST, 0);
        output.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(oldRepairCost));

        event.setOutput(output);
        event.setMaterialCost(materialCount);
        event.setCost(Math.max(1L, materialCount + renameCost));
    }

    private static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        Item canonicalRedstone = VanillaItemAliasResolver.canonicalFor(Items.REDSTONE);
        if (canonicalRedstone == Items.REDSTONE) {
            return;
        }

        PotionBrewing.Builder builder = event.getBuilder();
        addRedstoneMix(builder, Potions.WATER, Potions.MUNDANE, canonicalRedstone);
        addRedstoneMix(builder, Potions.NIGHT_VISION, Potions.LONG_NIGHT_VISION, canonicalRedstone);
        addRedstoneMix(builder, Potions.INVISIBILITY, Potions.LONG_INVISIBILITY, canonicalRedstone);
        addRedstoneMix(builder, Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE, canonicalRedstone);
        addRedstoneMix(builder, Potions.LEAPING, Potions.LONG_LEAPING, canonicalRedstone);
        addRedstoneMix(builder, Potions.SLOWNESS, Potions.LONG_SLOWNESS, canonicalRedstone);
        addRedstoneMix(builder, Potions.TURTLE_MASTER, Potions.LONG_TURTLE_MASTER, canonicalRedstone);
        addRedstoneMix(builder, Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, canonicalRedstone);
        addRedstoneMix(builder, Potions.WATER_BREATHING, Potions.LONG_WATER_BREATHING, canonicalRedstone);
        addRedstoneMix(builder, Potions.POISON, Potions.LONG_POISON, canonicalRedstone);
        addRedstoneMix(builder, Potions.REGENERATION, Potions.LONG_REGENERATION, canonicalRedstone);
        addRedstoneMix(builder, Potions.STRENGTH, Potions.LONG_STRENGTH, canonicalRedstone);
        addRedstoneMix(builder, Potions.WEAKNESS, Potions.LONG_WEAKNESS, canonicalRedstone);
        addRedstoneMix(builder, Potions.SLOW_FALLING, Potions.LONG_SLOW_FALLING, canonicalRedstone);
    }

    private static void addRedstoneMix(PotionBrewing.Builder builder, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> from, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> to, Item redstoneItem) {
        builder.addMix(from, redstoneItem, to);
    }

    private static int applyMaterialRepair(ItemStack stack, int maxMaterials) {
        int perUnit = Math.min(stack.getDamageValue(), stack.getMaxDamage() / 4);
        if (perUnit <= 0) {
            return 0;
        }

        int consumed = 0;
        while (perUnit > 0 && consumed < maxMaterials) {
            int repairedDamage = stack.getDamageValue() - perUnit;
            stack.setDamageValue(repairedDamage);
            consumed++;
            perUnit = Math.min(stack.getDamageValue(), stack.getMaxDamage() / 4);
        }
        return consumed;
    }

    private static int applyRename(ItemStack output, ItemStack originalLeft, String requestedName) {
        if (requestedName != null && !StringUtil.isBlank(requestedName)) {
            if (!requestedName.equals(originalLeft.getHoverName().getString())) {
                output.set(DataComponents.CUSTOM_NAME, Component.literal(requestedName));
                return 1;
            }
            return 0;
        }

        if (requestedName != null && StringUtil.isBlank(requestedName) && originalLeft.has(DataComponents.CUSTOM_NAME)) {
            output.remove(DataComponents.CUSTOM_NAME);
            return 1;
        }

        return 0;
    }

    private static ItemStack findRepairAlias(ItemStack left, ItemStack right) {
        Item rightItem = right.getItem();
        Map<Item, Item> aliasMap = RecipeInterceptor.buildAliasMapSnapshot();

        for (Map.Entry<Item, Item> entry : aliasMap.entrySet()) {
            if (entry.getValue() != rightItem) {
                continue;
            }

            Item aliasItem = entry.getKey();
            if (aliasItem == rightItem) {
                continue;
            }

            ItemStack candidate = right.transmuteCopy(aliasItem, right.getCount());
            if (left.getItem().isValidRepairItem(left, candidate)) {
                return candidate;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isCanonicalForVanilla(ItemStack stack, Item vanillaItem) {
        return VanillaItemAliasResolver.isCanonicalForVanilla(stack, vanillaItem);
    }
}
