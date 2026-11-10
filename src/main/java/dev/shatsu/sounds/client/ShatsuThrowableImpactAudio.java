package dev.shatsu.sounds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Impact audio for thrown items that lack a suitable vanilla impact sound.
 * Only the client entity event should play it; do not add a second server sound.
 */
public final class ShatsuThrowableImpactAudio {
    private ShatsuThrowableImpactAudio() {}

    public static void onImpact(Entity projectile, String eventId) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null
                || projectile.level() != client.level) return;
        Identifier id = Identifier.parse(eventId);
        var available = client.getSoundManager().getSoundEvent(id);
        if (available == null || available.getWeight() <= 0) return;
        client.level.playLocalSound(projectile,
            SoundEvent.createVariableRangeEvent(id),
            SoundSource.PLAYERS, .8f, 1f);
    }
}
