package trc.allitems.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import trc.allitems.client.gui.ChallengeListScreen;
import trc.allitems.network.CollectedPayload;
import trc.allitems.network.ListPayload;
import trc.allitems.network.StatePayload;

import org.jetbrains.annotations.NotNull;

public final class ClientChallenge {
    private static StatePayload state = StatePayload.INACTIVE;

    private ClientChallenge() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(StatePayload.TYPE, (payload, _) -> state = payload);
        ClientPlayNetworking.registerGlobalReceiver(CollectedPayload.TYPE, ClientChallenge::onCollected);
        ClientPlayNetworking.registerGlobalReceiver(ListPayload.TYPE, (payload, context) ->
                context.client().setScreenAndShow(new ChallengeListScreen(payload.collected(), payload.skipped())));
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> state = StatePayload.INACTIVE);
    }

    public static @NotNull StatePayload state() { return state; }

    public static boolean barVisible() { return state.active() && Minecraft.getInstance().player != null; }

    public static @NotNull ItemStack target() {
        if (!state.active() || !state.order() || state.finished()) return ItemStack.EMPTY;

        return state.target().flatMap(BuiltInRegistries.ITEM::getOptional).map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
    }

    private static void onCollected(@NotNull CollectedPayload payload, @NotNull ClientPlayNetworking.Context context) {
        context.client().getSoundManager().play(payload.finished()
                ? SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F)
                : SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F, 0.6F));

        if (!TRCAddonClient.CHAT_MESSAGES.value || payload.item().isEmpty()) return;
        else context.player();

        Identifier id = payload.item().get();
        Component name = BuiltInRegistries.ITEM.getOptional(id)
                .map(item -> item.getDefaultInstance().getHoverName())
                .orElse(Component.literal(id.toString()));

        context.player().sendSystemMessage(Component.literal("✔ Collected ").withStyle(ChatFormatting.GREEN)
                .append(name.copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" (" + payload.collected() + "/" + payload.total() + ")").withStyle(ChatFormatting.GRAY)));
    }
}