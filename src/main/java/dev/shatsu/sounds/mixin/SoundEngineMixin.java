package dev.shatsu.sounds.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shatsu.sounds.ShatsuConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Chaining gain control for catalogued Shatsu sounds, without replacing vanilla playback.
 * Nothing is changed for sound events outside the Shatsu catalog.
 */
@Mixin(value = SoundEngine.class, priority = 900)
abstract class SoundEngineMixin {
    @WrapOperation(
        method = "play",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getVolume()F"),
        require = 0)
    private float shatsu$gain(SoundInstance instance, Operation<Float> original) {
        float base=original.call(instance);
        return Math.max(0f,Math.min(2f,base*ShatsuConfig.current().multiplier(instance.getIdentifier().toString())));
    }

    /** Apply per-sound pitch from Sound Studio to real playback as well as previews.
     * Does not change events without a user pitch override.
     */
    @WrapOperation(
        method = "play",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getPitch()F"),
        require = 0)
    private float shatsu$pitch(SoundInstance instance, Operation<Float> original) {
        float base = original.call(instance);
        String id = instance.getIdentifier().toString();
        ShatsuConfig cfg = ShatsuConfig.current();
        if (!cfg.eventPitches.containsKey(id)) return base;
        return Math.max(.5f, Math.min(2f, base * cfg.pitch(id)));
    }
}
