package eu.zunix.ore_and_alloy.item;

import eu.zunix.ore_and_alloy.item.tooltip.PeriodicMaterialSymbolResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Optional;

public class PeriodicTooltip {
    private PeriodicTooltip() {}

    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        Optional<String> formula = PeriodicMaterialSymbolResolver.resolve(id.getPath());
        if (formula.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();
        int insertIndex = Math.min(1, tooltip.size());
        tooltip.add(insertIndex, Component.literal(formula.get()).withStyle(ChatFormatting.YELLOW));
    }
}

