package trc.allitems;

import net.minecraft.resources.Identifier;

import trc.core.ITRCAddon;
import trc.core.settings.*;

import trc.allitems.challenge.ChallengeCommands;
import trc.allitems.challenge.ChallengeTracker;
import trc.allitems.network.AllItemsNetworking;
import trc.allitems.network.ChallengeSync;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.NotNull;

public class TRCAddon implements ITRCAddon, ISettingsHolder {
    public static final String MOD_ID = "allitems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static final List<Setting> settings = new ArrayList<>();

    @Override
    public void init() { SettingsManager.getSettingsManager().register(this); }

    @Override
    public void start() {
        AllItemsNetworking.register();
        ChallengeSync.register();
        ChallengeTracker.register();
        ChallengeCommands.register();
    }

    @Override
    public String getModId() { return MOD_ID; }

    // Internal function for TRC-Core to collect all settings. Do not touch.
    @Override
    public List<Setting> getSettings() { return settings; }

    @Override
    public void onSettingsChanged() {}

    public static @NotNull Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
}