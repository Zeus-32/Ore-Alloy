package eu.zunix.ore_and_alloy.integration;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class VanillaTradeUnifier {
    private VanillaTradeUnifier() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VanillaTradeUnifier::onVillagerTrades);
        NeoForge.EVENT_BUS.addListener(VanillaTradeUnifier::onWandererTrades);
    }

    private static void onVillagerTrades(VillagerTradesEvent event) {
        Map<Item, Item> aliasMap = RecipeInterceptor.buildAliasMapSnapshot();
        if (aliasMap.isEmpty()) {
            return;
        }

        int wrapped = 0;
        for (List<VillagerTrades.ItemListing> listings : event.getTrades().values()) {
            wrapped += wrapListings(listings, aliasMap);
        }

    }

    private static void onWandererTrades(WandererTradesEvent event) {
        Map<Item, Item> aliasMap = RecipeInterceptor.buildAliasMapSnapshot();
        if (aliasMap.isEmpty()) {
            return;
        }

        int wrapped = 0;
        wrapped += wrapListings(event.getGenericTrades(), aliasMap);
        wrapped += wrapListings(event.getRareTrades(), aliasMap);
    }

    private static int wrapListings(List<VillagerTrades.ItemListing> listings, Map<Item, Item> aliasMap) {
        int wrapped = 0;
        for (int i = 0; i < listings.size(); i++) {
            VillagerTrades.ItemListing listing = listings.get(i);
            VillagerTrades.ItemListing delegate = listing instanceof UnifiedItemListing unified ? unified.delegate() : listing;
            listings.set(i, new UnifiedItemListing(delegate, aliasMap));
            wrapped++;
        }
        return wrapped;
    }

    private static MerchantOffer unifyOffer(MerchantOffer offer, Map<Item, Item> aliasMap) {
        if (offer == null) {
            return null;
        }

        ItemCost baseCost = offer.getItemCostA();
        Optional<ItemCost> costB = offer.getItemCostB();
        ItemStack result = offer.getResult();

        ItemCost unifiedBaseCost = unifyCost(baseCost, aliasMap);
        Optional<ItemCost> unifiedCostB = costB.map(cost -> unifyCost(cost, aliasMap));
        ItemStack unifiedResult = unifyStack(result, aliasMap);

        if (unifiedBaseCost == baseCost && unifiedCostB.equals(costB) && unifiedResult == result) {
            return offer;
        }

        MerchantOffer unified = new MerchantOffer(
                unifiedBaseCost,
                unifiedCostB,
                unifiedResult.copy(),
                offer.getUses(),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier(),
                offer.getDemand()
        );
        unified.setSpecialPriceDiff(offer.getSpecialPriceDiff());
        if (offer.isOutOfStock()) {
            unified.setToOutOfStock();
        }
        return unified;
    }

    private static ItemCost unifyCost(ItemCost cost, Map<Item, Item> aliasMap) {
        Item original = cost.item().value();
        Item canonical = aliasMap.get(original);
        if (canonical == null || canonical == original) {
            return cost;
        }
        return new ItemCost(canonical.builtInRegistryHolder(), cost.count(), cost.components());
    }

    private static ItemStack unifyStack(ItemStack stack, Map<Item, Item> aliasMap) {
        if (stack.isEmpty()) {
            return stack;
        }
        Item canonical = aliasMap.get(stack.getItem());
        if (canonical == null || canonical == stack.getItem()) {
            return stack;
        }
        return stack.transmuteCopy(canonical, stack.getCount());
    }

    private record UnifiedItemListing(VillagerTrades.ItemListing delegate, Map<Item, Item> aliasMap) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            MerchantOffer original = delegate.getOffer(trader, random);
            return unifyOffer(original, aliasMap);
        }
    }
}
