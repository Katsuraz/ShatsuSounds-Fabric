package dev.shatsu.sounds.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Separate entrypoint lets the combat patch attach to the newer 0.6
 * inventory-alpha JAR without replacing its existing food/arrow listeners.
 */
public final class ShatsuCombatClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ShatsuCombatChargeFeedback.register();
        System.out.println("[Shatsu Sounds] Combat charging feedback registered");
    }
}
