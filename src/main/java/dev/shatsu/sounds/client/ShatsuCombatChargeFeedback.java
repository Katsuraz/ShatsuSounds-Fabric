package dev.shatsu.sounds.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Items;

/**
 * Audible local charging feedback independently of optional Clangor.
 *
 * Do not repeatedly play a charge sound on every tick: it is split into
 * two short, non-looping stages and resets when the item stops being used.
 * This is strictly client-side feedback; real impacts/throwing remain
 * driven by Minecraft's normal sound events.
 */
public final class ShatsuCombatChargeFeedback {
    private static final Identifier BOW_START =
        Identifier.parse("shatsufresh:combat060.bow.nock");
    private static final Identifier BOW_DRAW =
        Identifier.parse("shatsufresh:combat060.bow.draw");
    private static final Identifier TRIDENT_START =
        Identifier.parse("shatsufresh:combat060.trident.charge_start");
    private static final Identifier TRIDENT_READY =
        Identifier.parse("shatsufresh:combat060.trident.charge_ready");

    private static boolean bowActive;
    private static boolean tridentActive;
    private static boolean bowTensionPlayed;
    private static boolean tridentReadyPlayed;
    private static int bowTicks;
    private static int tridentTicks;

    private ShatsuCombatChargeFeedback() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ShatsuCombatChargeFeedback::tick);
    }

    private static void reset() {
        bowActive = false;
        tridentActive = false;
        bowTensionPlayed = false;
        tridentReadyPlayed = false;
        bowTicks = 0;
        tridentTicks = 0;
    }

    private static void play(Minecraft client, Identifier id, float volume) {
        var manager = client.getSoundManager();
        var entry = manager.getSoundEvent(id);
        if (entry == null || entry.getWeight() <= 0) return;
        manager.play(SimpleSoundInstance.forUI(
            SoundEvent.createVariableRangeEvent(id), 1.0f, volume));
    }

    private static void tick(Minecraft client) {
        if (client.level == null || client.player == null
                || client.player.isSpectator() || client.gui.screen() != null
                || !client.player.isUsingItem()) {
            reset();
            return;
        }

        boolean bow = client.player.getUseItem().is(Items.BOW);
        boolean trident = client.player.getUseItem().is(Items.TRIDENT);
        if (!bow && !trident) {
            reset();
            return;
        }

        if (bow) {
            tridentActive = false;
            tridentReadyPlayed = false;
            tridentTicks = 0;
            if (!bowActive) {
                bowActive = true;
                bowTensionPlayed = false;
                bowTicks = 0;
                play(client, BOW_START, .96f);
            }
            bowTicks++;
            // Bow is noticeably taut once fully drawn.
            if (bowTicks >= 6 && !bowTensionPlayed) {
                play(client, BOW_DRAW, 1.06f);
                bowTensionPlayed = true;
            }
        } else {
            bowActive = false;
            bowTensionPlayed = false;
            bowTicks = 0;
            if (!tridentActive) {
                tridentActive = true;
                tridentReadyPlayed = false;
                tridentTicks = 0;
                play(client, TRIDENT_START, .98f);
            }
            tridentTicks++;
            if (tridentTicks >= 9 && !tridentReadyPlayed) {
                play(client, TRIDENT_READY, 1.02f);
                tridentReadyPlayed = true;
            }
        }
    }
}
