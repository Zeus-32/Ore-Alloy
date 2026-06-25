package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeAliasMapBuilderTest {
    @BeforeEach
    void enableAllMaterials() {
        System.setProperty("ore_and_alloy.register_all", "true");
    }

    @AfterEach
    void clearProperties() {
        System.clearProperty("ore_and_alloy.register_all");
    }

    @ParameterizedTest
    @MethodSource("rawMaterials")
    void rawCommonTagsUseCanonicalMaterialVariants(MetalMaterial material) {
        String materialName = material.materialName();

        assertEquals(
                new MaterialAliasKey("raw", materialName),
                RecipeAliasMapBuilder.materialKeyFromCommonTag(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + materialName)).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("crushedMaterials")
    void crushedCommonTagsUseCanonicalMaterialVariants(MetalMaterial material) {
        String materialName = material.materialName();

        assertEquals(
                new MaterialAliasKey("crushed", materialName),
                RecipeAliasMapBuilder.materialKeyFromCommonTag(ResourceLocation.fromNamespaceAndPath("c", "crushed_raw_materials/" + materialName)).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("rawMaterials")
    void directRawItemIdsUseCanonicalMaterialVariants(MetalMaterial material) {
        String materialName = material.materialName();

        assertEquals(
                new MaterialAliasKey("raw", materialName),
                RecipeAliasMapBuilder.materialKeyFromItemId(ResourceLocation.fromNamespaceAndPath("create", "raw_" + materialName)).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("crushedMaterials")
    void directCrushedItemIdsUseCanonicalMaterialVariants(MetalMaterial material) {
        String materialName = material.materialName();

        assertEquals(
                new MaterialAliasKey("crushed", materialName),
                RecipeAliasMapBuilder.materialKeyFromItemId(ResourceLocation.fromNamespaceAndPath("create", "crushed_raw_" + materialName)).orElseThrow()
        );
    }

    private static Stream<MetalMaterial> rawMaterials() {
        return Stream.of(MetalMaterial.values())
                .filter(material -> material.getForms().contains(MaterialForm.RAW));
    }

    private static Stream<MetalMaterial> crushedMaterials() {
        return Stream.of(MetalMaterial.values())
                .filter(material -> material.getForms().contains(MaterialForm.CRUSHED));
    }
}
