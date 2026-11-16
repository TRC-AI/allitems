package trc.allitems.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import trc.allitems.challenge.ChallengeTime;
import trc.allitems.network.StatePayload;

import org.jetbrains.annotations.NotNull;

public class ChallengeHud implements HudElement {
    public static final int HEIGHT = 19;

    private static final int BAR_WIDTH = 182;
    private static final int BAR_Y = 12;
    private static final Component SEPARATOR = Component.literal("  •  ").withStyle(ChatFormatting.DARK_GRAY);

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, @NotNull DeltaTracker deltaTracker) {
        if (!ClientChallenge.barVisible()) return;

        Minecraft minecraft = Minecraft.getInstance();
        StatePayload state = ClientChallenge.state();
        int left = graphics.guiWidth() / 2 - BAR_WIDTH / 2;

        String color = state.finished() ? "purple" : state.paused() ? "yellow" : "green";
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("boss_bar/" + color + "_background"),
                BAR_WIDTH, 5, 0, 0, left, BAR_Y, BAR_WIDTH, 5);

        int filled = state.total() == 0 ? 0 : Mth.lerpDiscrete((float) state.collected() / state.total(), 0, BAR_WIDTH);
        if (filled > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("boss_bar/" + color + "_progress"),
                    BAR_WIDTH, 5, 0, 0, left, BAR_Y, filled, 5);
        }

        ItemStack target = ClientChallenge.target();
        Component text = text(state, target);
        int textX = graphics.guiWidth() / 2 - minecraft.font.width(text) / 2;
        graphics.text(minecraft.font, text, textX, BAR_Y - 9, -1);

        if (!target.isEmpty()) graphics.item(target, Math.min(left, textX) - 20, 1);
    }

    private static @NotNull Component text(@NotNull StatePayload state, @NotNull ItemStack target) {
        MutableComponent text = Component.empty();

        if (state.finished()) {
            text.append(Component.translatable("allitems.hud.complete").withStyle(ChatFormatting.GOLD));
            text.append(SEPARATOR);
        }

        text.append(Component.translatable("allitems.hud.progress", state.collected(), state.total()));

        if (!target.isEmpty()) {
            text.append(SEPARATOR);
            text.append(Component.translatable("allitems.hud.next", target.getHoverName().copy().withStyle(ChatFormatting.AQUA)));
        }

        if (TRCAddonClient.SHOW_TIMER.value) {
            text.append(SEPARATOR);
            text.append(Component.literal(ChallengeTime.format(state.ticks())).withStyle(ChatFormatting.GRAY));
        }

        if (state.paused() && !state.finished()) {
            text.append(SEPARATOR);
            text.append(Component.translatable("allitems.hud.paused").withStyle(ChatFormatting.YELLOW));
        }

        return text;
    }
}