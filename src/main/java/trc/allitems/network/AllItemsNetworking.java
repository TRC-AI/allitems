package trc.allitems.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class AllItemsNetworking {
    private static boolean registered;

    private AllItemsNetworking() {}

    public static synchronized void register() {
        if (registered) return;
        registered = true;

        PayloadTypeRegistry.clientboundPlay().register(StatePayload.TYPE, StatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ListPayload.TYPE, ListPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CollectedPayload.TYPE, CollectedPayload.CODEC);
    }
}