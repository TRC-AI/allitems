package trc.allitems.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import trc.allitems.challenge.ChallengeTime;
import trc.allitems.client.ClientChallenge;
import trc.allitems.client.TRCAddonClient;
import trc.allitems.network.StatePayload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class ChallengeListScreen extends Screen {
    private static final Identifier CONTAINER = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier SLOT_HIGHLIGHT_BACK = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT = Identifier.withDefaultNamespace("container/slot_highlight_front");

    private static final int IMAGE_WIDTH = 176;
    private static final int ROWS = 6;
    private static final int GRID_HEIGHT = ROWS * 18 + 17;
    private static final int IMAGE_HEIGHT = GRID_HEIGHT + 7;
    private static final int SEARCH_HEIGHT = 16;
    private static final int SEARCH_GAP = 4;
    private static final int PER_PAGE = (ROWS - 1) * 9;
    private static final int LABEL_COLOR = -12566464;

    private static final int SLOT_COLLECTED = 0;
    private static final int SLOT_PREV = 1;
    private static final int SLOT_INFO = 3;
    private static final int SLOT_TARGET = 5;
    private static final int SLOT_NEXT = 7;
    private static final int SLOT_MISSING = 8;

    private static final ItemStack GREEN = new ItemStack(Items.LIME_CONCRETE);
    private static final ItemStack RED = new ItemStack(Items.RED_CONCRETE);
    private static final ItemStack ARROW = new ItemStack(Items.ARROW);
    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    private static final ItemStack FILLER = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);

    private final Set<Identifier> collected;
    private final Set<Identifier> skipped;
    private final List<Item> collectedItems = new ArrayList<>();
    private final List<Item> missingItems = new ArrayList<>();

    private ItemSearch search;
    private List<Item> shown = List.of();
    private boolean showCollected;
    private int page;
    private String query = "";
    private EditBox searchBox;
    private int left;
    private int top;

    public ChallengeListScreen(@NotNull List<Identifier> collected, @NotNull List<Identifier> skipped) {
        super(Component.translatable("allitems.screen.info"));
        this.collected = new HashSet<>(collected);
        this.skipped = new HashSet<>(skipped);
    }

    @Override
    protected void init() {
        if (search == null) buildLists();

        left = (width - IMAGE_WIDTH) / 2;
        top = (height - IMAGE_HEIGHT + SEARCH_HEIGHT + SEARCH_GAP) / 2;

        searchBox = new EditBox(font, left + 1, top - SEARCH_HEIGHT - SEARCH_GAP, IMAGE_WIDTH - 2, SEARCH_HEIGHT, searchBox,
                Component.translatable("allitems.screen.search"));
        searchBox.setHint(Component.translatable("allitems.screen.search").withStyle(EditBox.SEARCH_HINT_STYLE));
        searchBox.setMaxLength(64);
        searchBox.setValue(query);
        searchBox.setResponder(value -> {
            if (value.equals(query)) return;

            query = value;
            page = 0;
            refresh();
        });

        addRenderableWidget(searchBox);
        setInitialFocus(searchBox);
        refresh();
    }

    private void buildLists() {
        FeatureFlagSet features = minecraft.level != null ? minecraft.level.enabledFeatures() : null;
        List<Item> allItems = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || (features != null && !item.isEnabled(features))) continue;

            allItems.add(item);
            (collected.contains(BuiltInRegistries.ITEM.getKey(item)) ? collectedItems : missingItems).add(item);
        }

        search = new ItemSearch(allItems);
    }

    private void refresh() {
        shown = search.filter(showCollected ? collectedItems : missingItems, query);
        page = Math.clamp(page, 0, pageCount() - 1);
    }

    private int pageCount() { return Math.max(1, (shown.size() + PER_PAGE - 1) / PER_PAGE); }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER, left, top, 0.0F, 0.0F, IMAGE_WIDTH, GRID_HEIGHT, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER, left, top + GRID_HEIGHT, 0.0F, 215.0F, IMAGE_WIDTH, 7, 256, 256);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        Component title = Component.translatable(showCollected ? "allitems.screen.collected" : "allitems.screen.missing",
                page + 1, pageCount());
        graphics.text(font, title, left + 8, top + 6, LABEL_COLOR, false);

        int hovered = slotAt(mouseX, mouseY);
        if (hovered >= 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK, slotX(hovered) - 4, slotY(hovered) - 4, 24, 24);
        }

        extractHeader(graphics);
        extractItems(graphics);

        if (hovered >= 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT, slotX(hovered) - 4, slotY(hovered) - 4, 24, 24);
            extractTooltip(graphics, hovered, mouseX, mouseY);
        }
    }

    private void extractHeader(@NotNull GuiGraphicsExtractor graphics) {
        if (showCollected) highlight(graphics, SLOT_COLLECTED, 0x8040FF40);
        else highlight(graphics, SLOT_MISSING, 0x80FF4040);

        graphics.item(GREEN, slotX(SLOT_COLLECTED), slotY(SLOT_COLLECTED));
        graphics.item(RED, slotX(SLOT_MISSING), slotY(SLOT_MISSING));
        rotatedItem(graphics, slotX(SLOT_PREV), slotY(SLOT_PREV), 225.0F);
        rotatedItem(graphics, slotX(SLOT_NEXT), slotY(SLOT_NEXT), 45.0F);
        graphics.item(CLOCK, slotX(SLOT_INFO), slotY(SLOT_INFO));

        ItemStack target = ClientChallenge.target();

        for (int slot = 2; slot <= 6; slot++) {
            if (slot == SLOT_INFO) continue;

            graphics.item(slot == SLOT_TARGET && !target.isEmpty() ? target : FILLER, slotX(slot), slotY(slot));
        }
    }

    private void extractItems(@NotNull GuiGraphicsExtractor graphics) {
        int start = page * PER_PAGE;

        for (int i = 0; i < PER_PAGE && start + i < shown.size(); i++) {
            int slot = 9 + i;
            Item item = shown.get(start + i);

            if (isSkipped(item)) highlight(graphics, slot, 0x60FF4040);
            graphics.item(new ItemStack(item), slotX(slot), slotY(slot));
        }

        if (shown.isEmpty()) {
            graphics.centeredText(font, Component.translatable("allitems.screen.no_results"), left + IMAGE_WIDTH / 2,
                    top + 18 + 18 + (ROWS - 1) * 18 / 2 - 4 - 9, 0xFFA0A0A0);
        }
    }

    private void highlight(@NotNull GuiGraphicsExtractor graphics, int slot, int color) {
        graphics.fill(slotX(slot), slotY(slot), slotX(slot) + 16, slotY(slot) + 16, color);
    }

    private void rotatedItem(@NotNull GuiGraphicsExtractor graphics, int x, int y, float degrees) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 8.0F, y + 8.0F);
        graphics.pose().rotate((float) Math.toRadians(degrees));
        graphics.pose().translate(-8.0F, -8.0F);
        graphics.item(ChallengeListScreen.ARROW, 0, 0);
        graphics.pose().popMatrix();
    }

    private void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int slot, int mouseX, int mouseY) {
        if (slot >= 9) {
            int index = page * PER_PAGE + slot - 9;
            if (index >= shown.size()) return;

            Item item = shown.get(index);
            List<Component> lines = new ArrayList<>(getTooltipFromItem(minecraft, new ItemStack(item)));
            if (isSkipped(item)) lines.add(Component.translatable("allitems.screen.skipped").withStyle(ChatFormatting.RED));

            graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
            return;
        }

        switch (slot) {
            case SLOT_COLLECTED -> graphics.setTooltipForNextFrame(font,
                    Component.translatable("allitems.screen.show_collected").withStyle(ChatFormatting.GREEN), mouseX, mouseY);
            case SLOT_MISSING -> graphics.setTooltipForNextFrame(font,
                    Component.translatable("allitems.screen.show_missing").withStyle(ChatFormatting.RED), mouseX, mouseY);
            case SLOT_PREV -> graphics.setComponentTooltipForNextFrame(font, pageLines("allitems.screen.previous_page"), mouseX, mouseY);
            case SLOT_NEXT -> graphics.setComponentTooltipForNextFrame(font, pageLines("allitems.screen.next_page"), mouseX, mouseY);
            case SLOT_INFO -> graphics.setComponentTooltipForNextFrame(font, infoLines(), mouseX, mouseY);
            case SLOT_TARGET -> {
                ItemStack target = ClientChallenge.target();
                if (target.isEmpty()) return;

                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable("allitems.screen.next_item").withStyle(ChatFormatting.AQUA));
                lines.addAll(getTooltipFromItem(minecraft, target));
                graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
            }
            default -> {}
        }
    }

    private @NotNull List<Component> pageLines(@NotNull String key) {
        return List.of(Component.translatable(key),
                Component.translatable("allitems.screen.page", page + 1, pageCount()).withStyle(ChatFormatting.GRAY));
    }

    private @NotNull List<Component> infoLines() {
        StatePayload state = ClientChallenge.state();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("allitems.screen.info").withStyle(ChatFormatting.GOLD));

        if (!state.active()) {
            lines.add(Component.translatable("allitems.screen.no_challenge").withStyle(ChatFormatting.GRAY));
            return lines;
        }

        String stateKey = state.finished() ? "allitems.screen.state_complete"
                : state.paused() ? "allitems.screen.state_paused" : "allitems.screen.state_running";
        ChatFormatting stateColor = state.paused() && !state.finished() ? ChatFormatting.YELLOW : ChatFormatting.GREEN;

        lines.add(Component.translatable(state.order() ? "allitems.screen.mode_order" : "allitems.screen.mode_free").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(stateKey).withStyle(stateColor));
        lines.add(Component.translatable("allitems.screen.progress", state.collected(), state.total()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("allitems.screen.skipped_count", state.skipped()).withStyle(ChatFormatting.GRAY));

        if (TRCAddonClient.SHOW_TIMER.value) {
            lines.add(Component.translatable("allitems.screen.time", ChallengeTime.format(state.ticks())).withStyle(ChatFormatting.GRAY));
        }

        return lines;
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        switch (slotAt(event.x(), event.y())) {
            case SLOT_COLLECTED -> setView(true);
            case SLOT_MISSING -> setView(false);
            case SLOT_PREV -> turnPage(-1);
            case SLOT_NEXT -> turnPage(1);
            default -> { return false; }
        }

        return true;
    }

    private void setView(boolean collectedView) {
        click();
        if (showCollected == collectedView) return;

        showCollected = collectedView;
        page = 0;
        refresh();
    }

    private void turnPage(int delta) {
        int next = Math.clamp(page + delta, 0, pageCount() - 1);
        if (next == page) return;

        page = next;
        click();
    }

    private void click() { minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); }

    private boolean isSkipped(@NotNull Item item) { return skipped.contains(BuiltInRegistries.ITEM.getKey(item)); }

    private int slotX(int slot) { return left + 8 + (slot % 9) * 18; }

    private int slotY(int slot) { return top + 18 + (slot / 9) * 18; }

    private int slotAt(double mouseX, double mouseY) {
        int col = (int) Math.floor((mouseX - left - 7) / 18.0);
        int row = (int) Math.floor((mouseY - top - 17) / 18.0);
        if (col < 0 || col >= 9 || row < 0 || row >= ROWS) return -1;

        return row * 9 + col;
    }
}