package eu.zunix.ore_and_alloy.integration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RecipeInterceptorTest {
    @Test
    void ingredientSignaturesDistinguishDifferentInputsForSameResult() {
        List<String> dustPileCompacting = RecipeInterceptor.normalizedIngredientSignature(List.of(
                List.of("ore_and_alloy:iron_dust_pile"),
                List.of("ore_and_alloy:iron_dust_pile"),
                List.of("ore_and_alloy:iron_dust_pile"),
                List.of("ore_and_alloy:iron_dust_pile")
        ));
        List<String> mortarGrinding = RecipeInterceptor.normalizedIngredientSignature(List.of(
                List.of("ore_and_alloy:iron_ingot"),
                List.of("tge_core:mortar")
        ));

        assertNotEquals(dustPileCompacting, mortarGrinding);
    }
}
