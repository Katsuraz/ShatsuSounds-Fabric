package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuCatalog;
import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class ShatsuClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ShatsuConfig.load();
        ShatsuGenericAttackFeedback.register();
        ShatsuShieldReady.register();
        ShatsuArrowFlyby.register();
        System.out.println("[Shatsu Sounds] Audio catalog events: " + ShatsuCatalog.size());
        for (String id : new String[] {"modmenu","clangor","sound","customblocksounds","sound_physics_remastered"}) {
            System.out.println("[Shatsu Sounds] " + id + ": " +
                    (FabricLoader.getInstance().isModLoaded(id) ? "detected" : "not installed"));
        }
    }
}
