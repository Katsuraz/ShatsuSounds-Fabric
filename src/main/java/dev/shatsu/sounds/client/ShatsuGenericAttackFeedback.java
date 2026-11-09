package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

/**
 * Provides missing empty-hand style swing feedback for ordinary carried items.
 *
 * This is ONLY for a click initiating a swing at air/entity, not mining a block.
 * Clangor remains responsible for mapped swords, tools, mace, spear, bow, etc.
 * Vanilla combat hit sound events already use the soft Shatsu empty-hand samples.
 */
public final class ShatsuGenericAttackFeedback {
    private static final Identifier EVENT = Identifier.parse("clangor:swing.empty_hand");
    private static final SoundEvent SOUND = SoundEvent.createVariableRangeEvent(EVENT);
    private static boolean previouslyPressed = false;

    private ShatsuGenericAttackFeedback() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean pressed = client.options.keyAttack.isDown() && client.gui.screen() == null;
            boolean start = pressed && !previouslyPressed;
            previouslyPressed = pressed;
            if (!start || client.player == null || client.player.isSpectator()
                    || client.hitResult == null
                    || client.hitResult.getType() == HitResult.Type.BLOCK) {
                return; // no duplicate impacts when mining blocks
            }

            ItemStack item = client.player.getMainHandItem();
            if (!isOrdinaryItem(item)
                    || ShatsuConfig.current().multiplier(EVENT.toString()) <= 0f
                    || client.getSoundManager().getSoundEvent(EVENT) == null) {
                return;
            }
            // The same sound event the resource pack assigns to bare-hand swings.
            // The vanilla attack impact event retains its own empty-hand punch.
            client.getSoundManager().play(SimpleSoundInstance.forUI(SOUND, 1f, 0.55f));
        });
    }

    static boolean isOrdinaryItem(ItemStack stack) {
        if (stack.isEmpty()) return false; // preserve Clangor's empty hand behavior
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);

        // Respect all weapon/tool categories already customized in Clangor.
        if (path.endsWith("_sword") || path.endsWith("_axe")
                || path.endsWith("_pickaxe") || path.endsWith("_shovel")
                || path.endsWith("_hoe") || path.contains("trident")
                || path.contains("spear") || path.contains("mace")
                || path.contains("crossbow") || path.equals("bow")
                || path.contains("dagger") || path.contains("club")
                || path.contains("hammer") || path.contains("flail")
                || path.contains("staff") || path.contains("wand")) {
            return false;
        }
        // Clangor maps sticks/bones/rods/torches into its own rods category.
        // Our asset overlay already makes those rod hits sound like empty hands.
        if (path.equals("stick") || path.equals("bone") || path.equals("bamboo")
                || path.equals("torch") || path.endsWith("_torch")
                || path.endsWith("_rod")) {
            return false;
        }
        // Common block items, food, and unrelated inventory items get a soft swing.
        return true;
    }
}
