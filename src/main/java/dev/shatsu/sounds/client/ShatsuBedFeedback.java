package dev.shatsu.sounds.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** A single soft fabric/wood cue on each local-player bed transition. */
public final class ShatsuBedFeedback {
    private static final Identifier SLEEP = Identifier.parse("shatsufresh:world062.bed.sleep");
    private static final Identifier WAKE = Identifier.parse("shatsufresh:world062.bed.wake");
    private static boolean previouslySleeping;
    private static boolean hasPlayer;
    private ShatsuBedFeedback() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ShatsuBedFeedback::tick);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            hasPlayer = false;
            previouslySleeping = false;
            return;
        }
        boolean sleeping = client.player.isSleeping();
        if (!hasPlayer) {
            hasPlayer = true;
            previouslySleeping = sleeping;
            return;
        }
        if (sleeping == previouslySleeping) return;
        previouslySleeping = sleeping;
        Identifier id = sleeping ? SLEEP : WAKE;
        var manager = client.getSoundManager();
        var sound = manager.getSoundEvent(id);
        if (sound == null || sound.getWeight() <= 0) return;
        manager.play(SimpleSoundInstance.forUI(
            SoundEvent.createVariableRangeEvent(id), 1.0f, .65f));
    }
}
