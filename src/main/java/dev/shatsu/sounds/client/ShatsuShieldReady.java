package dev.shatsu.sounds.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Items;

/** Plays only on the transition from lowered shield to active blocking.
 * Vanilla shield BLOCK impact and break sounds are intentionally untouched.
 */
public final class ShatsuShieldReady {
    private static final Identifier ID = Identifier.parse("shatsupolish:shield.raise");
    private static final SoundEvent EVENT = SoundEvent.createVariableRangeEvent(ID);
    private static boolean previous = false;
    private static int cooldown = 0;

    private ShatsuShieldReady() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (cooldown > 0) cooldown--;
            boolean active = client.player != null
                && client.gui.screen() == null
                && client.player.isUsingItem()
                && client.player.getUseItem().is(Items.SHIELD);

            if (active && !previous && cooldown == 0
                    && client.getSoundManager().getSoundEvent(ID) != null) {
                client.getSoundManager().play(SimpleSoundInstance.forUI(EVENT, 1.0f, 0.55f));
                cooldown = 7;
            }
            previous = active;
        });
    }
}
