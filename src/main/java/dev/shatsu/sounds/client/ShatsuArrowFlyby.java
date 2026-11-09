package dev.shatsu.sounds.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import dev.shatsu.sounds.ShatsuConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Local positional arrow-flight whoosh, not an additional launch or impact.
 * Uses Shatsu's bundled CC0 arrow/fly samples. Once per nearby moving arrow;
 * throttled for volleys and suppressed for tridents / stationary projectiles.
 */
public final class ShatsuArrowFlyby {
    private static final Identifier ID = Identifier.parse("shatsufresh:preview.arrow.fly");
    private static final SoundEvent SOUND = SoundEvent.createVariableRangeEvent(ID);
    private static final Set<UUID> FIRED = new HashSet<>();
    private static ClientLevel previousLevel;
    private static int tick;
    private static int lastSoundTick = -100;

    private ShatsuArrowFlyby() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) {
                previousLevel = null;
                FIRED.clear();
                return;
            }
            if (previousLevel != client.level) {
                previousLevel = client.level;
                FIRED.clear();
                tick = 0;
                lastSoundTick = -100;
            }
            tick++;
            if (!ShatsuConfig.current().arrowWhooshEnabled) return;
            if (tick % 2 != 0 || client.gui.screen() != null) return;
            var soundSet = client.getSoundManager().getSoundEvent(ID);
            if (soundSet == null || soundSet.getWeight() <= 0) return;

            var seen = new HashSet<UUID>();
            for (var entity : client.level.getEntities(client.player,
                    client.player.getBoundingBox().inflate(14.0),
                    e -> {
                        String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
                        return type.equals("arrow") || type.equals("spectral_arrow");
                    })) {
                var arrow = entity;
                var uuid = arrow.getUUID();
                seen.add(uuid);
                double distance = client.player.distanceToSqr(arrow);
                double speedSq = arrow.getDeltaMovement().lengthSqr();
                if (FIRED.contains(uuid) || distance < 2.25 || distance > 144.0
                        || speedSq < 0.36 || tick - lastSoundTick < 4) continue;
                float volume = (float)Math.max(0.65, 1.15 - Math.sqrt(distance) * 0.035);
                client.level.playLocalSound(arrow, SOUND, SoundSource.PLAYERS,
                        volume, 0.94f + (uuid.hashCode() & 7) * 0.014f);
                FIRED.add(uuid);
                lastSoundTick = tick;
            }
            FIRED.retainAll(seen);
        });
    }
}
