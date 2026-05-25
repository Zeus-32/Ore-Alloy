package eu.zunix.ore_and_alloy.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerAliases;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerHiddenItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

@EmiEntrypoint
public final class OreAndAlloyEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        Set<Item> hiddenItems = ViewerHiddenItems.resolveHiddenItems();
        if (!hiddenItems.isEmpty()) {
            registry.removeEmiStacks(stack -> isHiddenStack(stack, hiddenItems));
            OreAndAlloy.LOGGER.info("[{}][EMI] Hidden {} item ids from sidebar/search index.",
                    OreAndAlloy.MODID, hiddenItems.size());
        }

        Map<Item, Set<String>> aliasesByItem = ViewerAliases.resolveVisibleItemAliases(hiddenItems);
        int aliasCount = 0;
        for (Map.Entry<Item, Set<String>> entry : aliasesByItem.entrySet()) {
            EmiStack stack = EmiStack.of(entry.getKey());
            if (stack.isEmpty()) continue;

            for (String alias : entry.getValue()) {
                registry.addAlias(stack, Component.literal(alias));
                aliasCount++;
            }
        }
        OreAndAlloy.LOGGER.info("[{}][EMI] Registered {} aliases for {} items.",
                OreAndAlloy.MODID, aliasCount, aliasesByItem.size());
    }

    private static boolean isHiddenStack(EmiStack stack, Set<Item> hiddenItems) {
        ItemStack itemStack = stack.getItemStack();
        return ViewerHiddenItems.isHiddenStack(itemStack, hiddenItems);
    }
}
