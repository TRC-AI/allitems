package trc.allitems.client;

import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.sdl.SDLMouse;

import org.jetbrains.annotations.NotNull;

public final class ListKey {
    private static boolean wasDown;

    private ListKey() {}

    public static void register() { ClientTickEvents.END_CLIENT_TICK.register(ListKey::tick); }

    private static void tick(@NotNull Minecraft minecraft) {
        boolean down = isDown();
        boolean pressed = down && !wasDown;
        wasDown = down;

        if (!pressed || minecraft.gui.screen() != null || minecraft.player == null) return;

        minecraft.player.connection.sendCommand("allitems list");
    }

    private static boolean isDown() {
        if (TRCAddonClient.LIST_KEY.isUnbound()) return false;

        InputConstants.Key key;
        try {
            key = InputConstants.getKey(TRCAddonClient.LIST_KEY.value);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return switch (key.getType()) {
            case KEYBOARD -> InputConstants.isKeyDown(key.getValue());
            case MOUSE -> (SDLMouse.SDL_GetMouseState(null, null) & (1 << (key.getValue() - 1))) != 0;
        };
    }
}