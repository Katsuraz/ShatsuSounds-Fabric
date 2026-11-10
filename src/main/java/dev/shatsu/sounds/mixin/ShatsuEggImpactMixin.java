package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuThrowableImpactAudio;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clientbound egg-particle event, without adding a second server impact sound. */
@Mixin(ThrownEgg.class)
public abstract class ShatsuEggImpactMixin {
    @Inject(method = "handleEntityEvent(B)V", at = @At("HEAD"), require = 0)
    private void shatsu$onEggImpact(byte event, CallbackInfo ci) {
        if (event == 3) {
            ShatsuThrowableImpactAudio.onImpact((ThrownEgg)(Object)this,
                "shatsufresh:combat060.throw.egg_impact");
        }
    }
}
