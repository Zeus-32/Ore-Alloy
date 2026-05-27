package eu.zunix.ore_and_alloy.integration.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class UnificationVanillaExpectations {
    private static final List<Expectation> EXPECTATIONS = List.of(
            expectation("minecraft:iron_ingot", "ore_and_alloy:iron_ingot"),
            expectation("minecraft:gold_ingot", "ore_and_alloy:gold_ingot"),
            expectation("minecraft:copper_ingot", "ore_and_alloy:copper_ingot"),

            expectation("minecraft:iron_nugget", "ore_and_alloy:iron_nugget"),
            expectation("minecraft:gold_nugget", "ore_and_alloy:gold_nugget"),

            expectation("minecraft:diamond", "ore_and_alloy:diamond"),
            expectation("minecraft:emerald", "ore_and_alloy:emerald"),
            expectation("minecraft:coal", "ore_and_alloy:coal"),
            expectation("minecraft:redstone", "ore_and_alloy:redstone"),
            expectation("minecraft:lapis_lazuli", "ore_and_alloy:lapis"),

            expectation("minecraft:iron_block", "ore_and_alloy:iron_block"),
            expectation("minecraft:gold_block", "ore_and_alloy:gold_block"),
            expectation("minecraft:copper_block", "ore_and_alloy:copper_block"),
            expectation("minecraft:diamond_block", "ore_and_alloy:diamond_block"),
            expectation("minecraft:emerald_block", "ore_and_alloy:emerald_block"),
            expectation("minecraft:coal_block", "ore_and_alloy:coal_block"),
            expectation("minecraft:redstone_block", "ore_and_alloy:redstone_block"),
            expectation("minecraft:lapis_block", "ore_and_alloy:lapis_block")
    );

    private UnificationVanillaExpectations() {}

    public static List<String> evaluate(Map<Item, Item> aliasToCanonical) {
        List<String> issues = new ArrayList<>();
        for (Expectation expectation : EXPECTATIONS) {
            Item vanilla = BuiltInRegistries.ITEM.get(expectation.vanillaId());
            if (vanilla == Items.AIR) {
                continue;
            }

            Item expected = BuiltInRegistries.ITEM.get(expectation.expectedCanonicalId());
            if (expected == Items.AIR) {
                continue;
            }

            Item actual = aliasToCanonical.getOrDefault(vanilla, vanilla);
            if (actual != expected) {
                issues.add("expected " + expectation.vanillaId() + " -> " + expectation.expectedCanonicalId()
                        + ", but got " + BuiltInRegistries.ITEM.getKey(actual));
            }
        }
        return issues;
    }

    private static Expectation expectation(String vanillaId, String expectedCanonicalId) {
        return new Expectation(ResourceLocation.parse(vanillaId), ResourceLocation.parse(expectedCanonicalId));
    }

    private record Expectation(ResourceLocation vanillaId, ResourceLocation expectedCanonicalId) {}
}
