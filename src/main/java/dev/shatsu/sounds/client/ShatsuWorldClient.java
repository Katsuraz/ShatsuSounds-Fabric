package dev.shatsu.sounds.client;

import net.fabricmc.api.ClientModInitializer;

/** Separate world-audio entrypoint preserves all existing Shatsu listeners. */
public final class ShatsuWorldClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ShatsuBedFeedback.register();
        System.out.println("[Shatsu Sounds] Bed transition sounds registered");
    }
}
