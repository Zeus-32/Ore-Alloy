package eu.zunix.ore_and_alloy.integration.jei.vein;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class JeiVeinInfoCategory implements IRecipeCategory<JeiVeinInfoRecipe> {
    public static final RecipeType<JeiVeinInfoRecipe> TYPE = RecipeType.create(OreAndAlloy.MODID, "vein_info", JeiVeinInfoRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 132;
    private static final int SLOT_SPACING = 18;
    private static final int ORE_COLUMNS = 7;
    private static final int MAX_ORE_SLOTS = 14;
    private static final int LEFT = 8;
    private static final int RAW_ROW_Y = 112;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFE0E0E0;
    private static final int LINE_COLOR = 0xFFB7B7B7;

    private final IDrawable icon;

    public JeiVeinInfoCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<JeiVeinInfoRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ore_and_alloy.jei.vein.title");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiVeinInfoRecipe recipe, IFocusGroup focuses) {
        int oreLimit = Math.min(MAX_ORE_SLOTS, recipe.oreVariantGroups().size());
        for (int i = 0; i < oreLimit; i++) {
            int row = i / ORE_COLUMNS;
            int col = i % ORE_COLUMNS;
            int x = LEFT + col * SLOT_SPACING;
            int y = 22 + row * SLOT_SPACING;

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStacks(recipe.oreVariantGroups().get(i))
                    .setStandardSlotBackground();
        }

        int rawLimit = Math.min(9, recipe.rawStacks().size());
        for (int i = 0; i < rawLimit; i++) {
            int x = LEFT + i * SLOT_SPACING;
            int y = RAW_ROW_Y;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStack(recipe.rawStacks().get(i))
                    .setStandardSlotBackground();
        }

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemStacks(recipe.oreStacks());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addItemStacks(recipe.rawStacks());
    }

    @Override
    public void draw(JeiVeinInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.fill(0, 18, WIDTH, 19, LINE_COLOR);
        guiGraphics.fill(0, 58, WIDTH, 59, LINE_COLOR);
        guiGraphics.fill(0, 106, WIDTH, 107, LINE_COLOR);

        drawBoundedLine(
                guiGraphics,
                recipe.definition().displayName(),
                LEFT,
                6,
                WIDTH - 16,
                TEXT_PRIMARY
        );
        drawBoundedLine(
                guiGraphics,
                Component.translatable("ore_and_alloy.jei.vein.height", recipe.definition().minY(), recipe.definition().maxY()).getString(),
                LEFT,
                62,
                WIDTH - 16,
                TEXT_SECONDARY
        );
        drawBoundedLine(
                guiGraphics,
                Component.translatable("ore_and_alloy.jei.vein.size", recipe.definition().size()).getString(),
                LEFT,
                72,
                WIDTH - 16,
                TEXT_SECONDARY
        );
        drawBoundedLine(
                guiGraphics,
                Component.translatable("ore_and_alloy.jei.vein.chance", recipe.definition().chanceDenominator()).getString(),
                LEFT,
                82,
                WIDTH - 16,
                TEXT_SECONDARY
        );
        drawBoundedLine(
                guiGraphics,
                Component.translatable("ore_and_alloy.jei.vein.dimension", recipe.definition().dimensionDisplayName()).getString(),
                LEFT,
                92,
                WIDTH - 16,
                TEXT_SECONDARY
        );
    }

    @Override
    public void getTooltip(mezz.jei.api.gui.builder.ITooltipBuilder tooltip, JeiVeinInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        
    }

    @Override
    public ResourceLocation getRegistryName(JeiVeinInfoRecipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, "vein_info/" + recipe.definition().id());
    }

    private static void drawBoundedLine(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        var font = Minecraft.getInstance().font;
        String safe = text == null ? "" : text;
        if (font.width(safe) <= maxWidth) {
            guiGraphics.drawString(font, safe, x, y, color, true);
            return;
        }

        int ellipsisWidth = font.width("...");
        int widthForText = Math.max(0, maxWidth - ellipsisWidth);
        String trimmed = font.plainSubstrByWidth(safe, widthForText);
        guiGraphics.drawString(font, trimmed + "...", x, y, color, true);
    }
}
