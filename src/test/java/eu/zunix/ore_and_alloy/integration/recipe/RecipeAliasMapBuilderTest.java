package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
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

    @ParameterizedTest
    @MethodSource("rawMaterials")
    void rawStorageBlockCommonTagsUseRawVariantMaterialKeys(MetalMaterial material) {
        String materialName = material.materialName();

        assertEquals(
                new MaterialAliasKey("block", "raw_" + materialName),
                RecipeAliasMapBuilder.materialKeyFromCommonTag(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/raw_" + materialName)).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("rawMaterials")
    void directRawStorageBlockItemIdsUseRawVariantMaterialKeys(MetalMaterial material) {
        String materialName = material.materialName();

        assertEquals(
                new MaterialAliasKey("block", "raw_" + materialName),
                RecipeAliasMapBuilder.materialKeyFromItemId(ResourceLocation.fromNamespaceAndPath("create", "raw_" + materialName + "_block")).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("rawIronVariants")
    void rawStorageBlockVariantAliasesUseCanonicalMaterial(String rawVariant) {
        assertEquals(
                new MaterialAliasKey("block", "raw_iron"),
                RecipeAliasMapBuilder.materialKeyFromCommonTag(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/raw_" + rawVariant)).orElseThrow()
        );
        assertEquals(
                new MaterialAliasKey("block", "raw_iron"),
                RecipeAliasMapBuilder.materialKeyFromItemId(ResourceLocation.fromNamespaceAndPath("create", "raw_" + rawVariant + "_block")).orElseThrow()
        );
    }

    @Test
    void rawStorageBlockCanonicalPathUsesPrimaryRawVariantLikeRawItems() throws Exception {
        Method preferredCanonicalPath = RecipeAliasMapBuilder.class.getDeclaredMethod("preferredCanonicalPath", MaterialAliasKey.class);
        preferredCanonicalPath.setAccessible(true);

        assertEquals(
                "raw_chalcopyrite_block",
                preferredCanonicalPath.invoke(null, new MaterialAliasKey("block", "raw_copper"))
        );
        assertEquals(
                "raw_iron_block",
                preferredCanonicalPath.invoke(null, new MaterialAliasKey("block", "raw_iron"))
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

    private static Stream<String> rawIronVariants() {
        return Stream.of("hematite", "magnetite", "limonite");
    }
}
