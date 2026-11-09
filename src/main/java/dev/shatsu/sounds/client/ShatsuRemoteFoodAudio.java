package dev.shatsu.sounds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Handles server-transmitted generic chewing events, which bypass the local
 * Consumable mixin. Only player eating is matched, not mobs or drinks.
 * Does not require the separate Eating Sounds mod or its recordings.
 */
public final class ShatsuRemoteFoodAudio {
    private ShatsuRemoteFoodAudio() {}

    private static boolean isPlayerEating(SoundEvent event, SoundSource source) {
        return source == SoundSource.PLAYERS && SoundEvents.GENERIC_EAT.value().equals(event);
    }

    private static ItemStack likelyFood(Player player) {
        ItemStack using = player.getUseItem();
        if (!using.isEmpty()) return using;
        ItemStack main = player.getMainHandItem();
        if (ShatsuFoodAudio.hasProfile(main)) return main;
        ItemStack off = player.getOffhandItem();
        return ShatsuFoodAudio.hasProfile(off) ? off : ItemStack.EMPTY;
    }

    public static boolean replacePositioned(SoundEvent original, SoundSource source,
                                             double x, double y, double z,
                                             float volume, float pitch) {
        if (!isPlayerEating(original, source)) return false;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        Player closest = null;
        double smallest = 6.25; // 2.5 block attribution radius, not all nearby sounds
        for (var p : level.players()) {
            if (!ShatsuFoodAudio.hasProfile(likelyFood(p))) continue;
            double dist = p.distanceToSqr(x, y, z);
            if (dist < smallest) { closest = p; smallest = dist; }
        }
        if (closest == null) return false;
        SoundEvent replacement = ShatsuFoodAudio.select(likelyFood(closest), original, "remote");
        if (replacement == original) return false;
        level.playLocalSound(closest, replacement, source, volume, pitch);
        return true;
    }

    public static boolean replaceEntity(Entity entity, SoundEvent original,
                                         SoundSource source, float volume, float pitch) {
        if (!isPlayerEating(original, source) || !(entity instanceof Player player)) return false;
        SoundEvent replacement = ShatsuFoodAudio.select(likelyFood(player), original, "remote");
        if (replacement == original) return false;
        entity.level().playLocalSound(entity, replacement, source, volume, pitch);
        return true;
    }
}
