package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuThrowableImpactAudio;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clientbound particle event for snowball impact. */
@Mixin(Snowball.class)
public abstract class ShatsuSnowballImpactMixin {
    @Inject(method = "handleEntityEvent(B)V", at = @At("HEAD"), require = 0)
    private void shatsu$onSnowballImpact(byte event, CallbackInfo ci) {
        if (event == 3) {
            ShatsuThrowableImpactAudio.onImpact((Snowball)(Object)this,
                "shatsufresh:combat060.throw.snowball_impact");
        }
    }
}
