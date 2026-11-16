package trc.allitems.client;

import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public final class ListKey {
    private static boolean wasDown;

    private ListKey() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ListKey::tick);
    }

    private static void tick(@NotNull Minecraft minecraft) {
        boolean down = isDown(minecraft);
        boolean pressed = down && !wasDown;
        wasDown = down;

        if (!pressed || minecraft.screen != null || minecraft.player == null) return;

        minecraft.player.connection.sendCommand("allitems list");
    }

    private static boolean isDown(@NotNull Minecraft minecraft) {
        if (TRCAddonClient.LIST_KEY.isUnbound()) return false;

        InputConstants.Key key;
        try {
            key = InputConstants.getKey(TRCAddonClient.LIST_KEY.value);
        } catch (IllegalArgumentException e) {
            return false;
        }

        Window window = minecraft.getWindow();

        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
    }
}
