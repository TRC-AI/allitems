package trc.allitems.client;

import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import trc.core.ITRCAddon;
import trc.core.settings.*;

import trc.allitems.TRCAddon;
import trc.allitems.network.AllItemsNetworking;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRCAddonClient implements ITRCAddon, ISettingsHolder {
    public static final Logger LOGGER = LoggerFactory.getLogger(TRCAddon.MOD_ID + "_client");

    public static final BoolSetting SHOW_TIMER =
            new BoolSetting("show_timer", Component.translatable("allitems_client.setting.show_timer"), true, "Show timer");

    public static final BoolSetting CHAT_MESSAGES =
            new BoolSetting("chat_messages", Component.translatable("allitems_client.setting.chat_messages"), true, "Chat message on collect");

    public static final KeySetting LIST_KEY =
            new KeySetting("list_key", Component.translatable("allitems_client.setting.list_key"), KeySetting.UNBOUND, "Open item list");

    static final List<Setting> settings = new ArrayList<>();

    @Override
    public void init() {
        settings.add(SHOW_TIMER);
        settings.add(CHAT_MESSAGES);
        settings.add(LIST_KEY);

        SettingsManager.getSettingsManager().register(this);
    }

    @Override
    public void start() {
        AllItemsNetworking.register();
        ClientChallenge.register();
        ListKey.register();
        HudElementRegistry.attachElementBefore(VanillaHudElements.BOSS_BAR, TRCAddon.id("challenge_bar"), new ChallengeHud());
    }

    @Override
    public String getModId() { return TRCAddon.MOD_ID + "_client"; }

    // Internal function for TRC-Core to collect all settings. Do not touch.
    @Override
    public List<Setting> getSettings() { return settings; }

    @Override
    public void onSettingsChanged() {}
}