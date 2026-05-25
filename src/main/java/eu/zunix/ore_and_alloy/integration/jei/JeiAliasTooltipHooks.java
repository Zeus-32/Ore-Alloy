package eu.zunix.ore_and_alloy.integration.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.viewer.ViewerAliases;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class JeiAliasTooltipHooks {
    private static final String JEI_ALIAS_HEADER_TRANSLATION_KEY = "jei.tooltip.item.search.aliases";
    private static final String JEI_ALIAS_BULLET_PREFIX = "\u2022 ";
    private static volatile IJeiRuntime runtime;
    private static volatile boolean registered;

    private JeiAliasTooltipHooks() {}

    static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        registerIfNeeded();
    }

    static void onRuntimeUnavailable() {
        runtime = null;
    }

    private static void registerIfNeeded() {
        if (registered) return;
        registered = true;
        NeoForge.EVENT_BUS.addListener(JeiAliasTooltipHooks::onGatherTooltipComponents);
        NeoForge.EVENT_BUS.addListener(JeiAliasTooltipHooks::onScreenRenderPost);
    }

    private static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!OreAndAlloy.MODID.equals(id.getNamespace())) return;

        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        int headerIndex = findJeiAliasHeaderIndex(elements);
        if (headerIndex < 0) return;

        int removeStart = headerIndex;
        if (removeStart > 0 && isEmptyText(elements.get(removeStart - 1))) {
            removeStart--;
        }

        int removeEnd = headerIndex + 1;
        while (removeEnd < elements.size() && isJeiAliasBulletLine(elements.get(removeEnd))) {
            removeEnd++;
        }

        elements.subList(removeStart, removeEnd).clear();
    }

    private static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!isPhysicalShiftDown()) return;
        if (runtime == null) return;
        if (!runtime.getIngredientListOverlay().isListDisplayed()) return;

        Optional<ITypedIngredient<?>> ingredientUnderMouse = runtime.getIngredientListOverlay().getIngredientUnderMouse();
        if (ingredientUnderMouse.isEmpty()) return;

        ItemStack hoveredStack = ingredientUnderMouse.get().getItemStack().orElse(ItemStack.EMPTY);
        if (hoveredStack.isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(hoveredStack.getItem());
        if (!OreAndAlloy.MODID.equals(id.getNamespace())) return;

        Set<String> aliases = ViewerAliases.aliasesForItemId(id);
        if (aliases.isEmpty()) return;

        List<Component> lines = new ArrayList<>(aliases.size() + 1);
        lines.add(Component.translatable("ore_and_alloy.jei.aliases.title").withStyle(ChatFormatting.YELLOW));
        for (String alias : aliases) {
            lines.add(Component.literal("- " + alias).withStyle(ChatFormatting.GRAY));
        }

        int mouseX = event.getMouseX() + 12;
        int mouseY = event.getMouseY() + 12;
        event.getGuiGraphics().renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY);
    }

    private static int findJeiAliasHeaderIndex(List<Either<FormattedText, TooltipComponent>> elements) {
        for (int i = 0; i < elements.size(); i++) {
            Optional<FormattedText> line = elements.get(i).left();
            if (line.isEmpty()) continue;
            if (isJeiAliasHeader(line.get())) return i;
        }
        return -1;
    }

    private static boolean isJeiAliasHeader(FormattedText line) {
        if (!(line instanceof Component component)) return false;
        if (!(component.getContents() instanceof TranslatableContents translatable)) return false;
        return JEI_ALIAS_HEADER_TRANSLATION_KEY.equals(translatable.getKey());
    }

    private static boolean isJeiAliasBulletLine(Either<FormattedText, TooltipComponent> element) {
        String text = textOf(element);
        return text != null && text.startsWith(JEI_ALIAS_BULLET_PREFIX);
    }

    private static boolean isEmptyText(Either<FormattedText, TooltipComponent> element) {
        String text = textOf(element);
        return text != null && text.isBlank();
    }

    private static String textOf(Either<FormattedText, TooltipComponent> element) {
        Optional<FormattedText> line = element.left();
        return line.map(FormattedText::getString).orElse(null);
    }

    private static boolean isPhysicalShiftDown() {
        Minecraft minecraft = Minecraft.getInstance();
        long window = minecraft.getWindow().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
