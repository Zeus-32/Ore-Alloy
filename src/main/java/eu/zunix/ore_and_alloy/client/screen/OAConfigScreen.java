package eu.zunix.ore_and_alloy.client.screen;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.config.OAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class OAConfigScreen extends Screen {
    private static final String LANG = OreAndAlloy.MODID + ".config.screen.";

    private static final int PANEL_OUTER = 0x55221612;
    private static final int PANEL_BORDER = 0xAA7A5B3A;
    private static final int PANEL_INNER = 0xC41B1713;
    private static final int HEADER_BG = 0xCC2B221B;
    private static final int HEADER_BORDER = 0xAA8C6A44;
    private static final int OPTION_BG = 0xAA2A2119;
    private static final int OPTION_BORDER = 0xAA5F4A32;
    private static final int OPTION_ACCENT = 0xFFB98239;

    private final Screen parent;

    private boolean worldgenEnabled;
    private boolean periodicTooltipsEnabled;
    private boolean loadedValues;

    private Button worldgenButton;
    private Button periodicButton;

    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFA0A0A0;

    public OAConfigScreen(Screen parent) {
        super(Component.translatable(LANG + "title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (!loadedValues) {
            loadFromConfig();
            loadedValues = true;
        }

        clearWidgets();

        int left = panelLeft();
        int width = panelWidth();
        int top = panelTop();
        int inset = 10;
        int rowLeft = left + inset;
        int rowWidth = width - (inset * 2);
        int rowRight = rowLeft + rowWidth;
        int toggleWidth = 92;

        worldgenButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            worldgenEnabled = !worldgenEnabled;
            refreshButtons();
        }).bounds(rowRight - toggleWidth - 12, top + 60, toggleWidth, 20)
                .tooltip(Tooltip.create(Component.translatable(LANG + "worldgen.tooltip")))
                .build());

        periodicButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            periodicTooltipsEnabled = !periodicTooltipsEnabled;
            refreshButtons();
        }).bounds(rowRight - toggleWidth - 12, top + 108, toggleWidth, 20)
                .tooltip(Tooltip.create(Component.translatable(LANG + "tooltips.tooltip")))
                .build());

        int contentLeft = left + inset;
        int contentWidth = width - (inset * 2);
        int buttonGap = 10;
        int leftButtonWidth = (contentWidth - buttonGap) / 2;
        int rightButtonWidth = contentWidth - leftButtonWidth - buttonGap;

        addRenderableWidget(Button.builder(Component.translatable(LANG + "buttons.save"), button -> save())
                .bounds(contentLeft, top + 158, leftButtonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable(LANG + "buttons.reset"), button -> resetToDefaults())
                .bounds(contentLeft + leftButtonWidth + buttonGap, top + 158, rightButtonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable(LANG + "buttons.back"), button -> onClose())
                .bounds(contentLeft, top + 182, contentWidth, 20)
                .build());

        refreshButtons();
        showKubeOverrideHint();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = panelLeft();
        int top = panelTop();
        int width = panelWidth();

        int inset = 10;
        int headerLeft = left + inset;
        int headerRight = left + width - inset;
        int headerTop = top + 8;
        int headerBottom = top + 34;

        guiGraphics.fill(headerLeft, headerTop, headerRight, headerBottom, HEADER_BG);
        guiGraphics.fill(headerLeft, headerTop, headerRight, headerTop + 1, HEADER_BORDER);
        guiGraphics.fill(headerLeft, headerBottom - 1, headerRight, headerBottom, HEADER_BORDER);
        guiGraphics.fill(headerLeft, headerTop, headerLeft + 1, headerBottom, HEADER_BORDER);
        guiGraphics.fill(headerRight - 1, headerTop, headerRight, headerBottom, HEADER_BORDER);

        guiGraphics.drawString(font, Component.translatable(LANG + "subtitle"), left + 12, top + 16, 0xD8CAB6);
        guiGraphics.drawString(font, Component.literal("O&A"), left + width - 30, top + 16, 0xF4C27A);
        guiGraphics.drawCenteredString(font, title, this.width / 2, top - 12, 0xFFFFFF);

        drawOptionCard(
                guiGraphics,
                left + inset,
                top + 48,
                width - (inset * 2),
                Component.translatable(LANG + "option.worldgen"),
                Component.translatable(LANG + "option.worldgen.desc")
        );
        drawOptionCard(
                guiGraphics,
                left + inset,
                top + 96,
                width - (inset * 2),
                Component.translatable(LANG + "option.tooltips"),
                Component.translatable(LANG + "option.tooltips.desc")
        );

        if (!statusMessage.getString().isBlank()) {
            guiGraphics.drawString(font, statusMessage, left + 10, top + 208, statusColor);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft != null && minecraft.level != null) {
            renderTransparentBackground(guiGraphics);
        } else {
            renderPanorama(guiGraphics, partialTick);
            renderMenuBackground(guiGraphics);
        }

        int left = panelLeft();
        int top = panelTop();
        int right = left + panelWidth();
        int bottom = top + panelHeight();

        guiGraphics.fill(left - 12, top - 12, right + 12, bottom + 12, PANEL_OUTER);
        guiGraphics.fill(left - 1, top - 1, right + 1, bottom + 1, PANEL_BORDER);
        guiGraphics.fill(left, top, right, bottom, PANEL_INNER);

        drawRivet(guiGraphics, left + 4, top + 4);
        drawRivet(guiGraphics, right - 7, top + 4);
        drawRivet(guiGraphics, left + 4, bottom - 7);
        drawRivet(guiGraphics, right - 7, bottom - 7);
    }

    private void drawOptionCard(GuiGraphics guiGraphics, int left, int top, int width, Component title, Component description) {
        int right = left + width;
        int bottom = top + 40;

        guiGraphics.fill(left, top, right, bottom, OPTION_BG);
        guiGraphics.fill(left, top, right, top + 1, OPTION_BORDER);
        guiGraphics.fill(left, bottom - 1, right, bottom, OPTION_BORDER);
        guiGraphics.fill(left, top, left + 1, bottom, OPTION_BORDER);
        guiGraphics.fill(right - 1, top, right, bottom, OPTION_BORDER);

        guiGraphics.fill(left + 2, top + 2, left + 6, bottom - 2, OPTION_ACCENT);
        guiGraphics.drawString(font, title, left + 12, top + 7, 0xF3E8DA);
        guiGraphics.drawString(font, description, left + 12, top + 20, 0xC4B39E);
    }

    private void drawRivet(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 3, y + 3, 0xFF8A6C47);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 2, 0xFFD7B07D);
    }

    private void save() {
        try {
            OAConfig.setCustomVeinWorldgenEnabledFromScreen(worldgenEnabled);
            OAConfig.setPeriodicTooltipsEnabledFromScreen(periodicTooltipsEnabled);

            statusMessage = Component.translatable(LANG + "status.saved").withStyle(ChatFormatting.GREEN);
            statusColor = 0xFF75E08C;
        } catch (RuntimeException ex) {
            OreAndAlloy.LOGGER.error("[{}] Failed to save config from clickable screen.", OreAndAlloy.MODID, ex);
            statusMessage = Component.translatable(LANG + "status.save_failed").withStyle(ChatFormatting.RED);
            statusColor = 0xFFFF7272;
        }
    }

    private void resetToDefaults() {
        worldgenEnabled = true;
        periodicTooltipsEnabled = true;

        refreshButtons();
        statusMessage = Component.translatable(LANG + "status.reset").withStyle(ChatFormatting.YELLOW);
        statusColor = 0xFFFAD97A;
    }

    private void loadFromConfig() {
        worldgenEnabled = OAConfig.customVeinWorldgenConfiguredValue();
        periodicTooltipsEnabled = OAConfig.periodicTooltipsConfiguredValue();
    }

    private void refreshButtons() {
        if (worldgenButton != null) {
            worldgenButton.setMessage(toggleValue(worldgenEnabled));
        }
        if (periodicButton != null) {
            periodicButton.setMessage(toggleValue(periodicTooltipsEnabled));
        }
    }

    private void showKubeOverrideHint() {
        if (OAConfig.hasKubeCustomVeinWorldgenOverride() || OAConfig.hasKubePeriodicTooltipsOverride()) {
            statusMessage = Component.translatable(LANG + "status.kube_override").withStyle(ChatFormatting.GOLD);
            statusColor = 0xFFF1C35E;
        }
    }

    private Component toggleValue(boolean enabled) {
        return Component.translatable(enabled ? LANG + "toggle.on" : LANG + "toggle.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return Math.max(16, (height - panelHeight()) / 2);
    }

    private int panelWidth() {
        return Math.min(428, width - 36);
    }

    private int panelHeight() {
        return 232;
    }
}
