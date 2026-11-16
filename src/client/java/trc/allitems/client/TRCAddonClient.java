package trc.allitems.client;

import trc.core.ITRCAddon;
import trc.core.settings.*;

import trc.allitems.TRCAddon;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRCAddonClient implements ITRCAddon, ISettingsHolder {
    public static final Logger LOGGER = LoggerFactory.getLogger(TRCAddon.MOD_ID + "_client");

    static final List<Setting> settings = new ArrayList<>();

    @Override
    public void init() {
        SettingsManager.getSettingsManager().register(this);
    }

    @Override
    public void start() {}

    @Override
    public String getModId() { return TRCAddon.MOD_ID + "_client"; }

    // Internal function for TRC-Core to collect all settings. Do not touch.
    @Override
    public List<Setting> getSettings() { return settings; }

    @Override
    public void onSettingsChanged() {}
}