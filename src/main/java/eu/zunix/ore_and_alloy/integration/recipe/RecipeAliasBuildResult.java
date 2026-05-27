package eu.zunix.ore_and_alloy.integration.recipe;

import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

public record RecipeAliasBuildResult(
        Map<Item, Item> aliasToCanonical,
        Map<MaterialAliasKey, List<Item>> duplicateGroups,
        Map<MaterialAliasKey, Item> canonicalByGroup,
        Map<Item, List<Item>> aliasCanonicalCandidates
) {}
