package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuFoodAudio;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Change only the sound chosen by the food-consumption method. No extra
 * sound is added, so vanilla eating isn't layered on top. Client-only:
 * server-side item consumption and sounds remain untouched.
 */
@Mixin(Consumable.class)
public abstract class ShatsuConsumableSoundMixin {
    @ModifyArg(
        method = "emitParticlesAndSounds",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"),
        index = 0,
        require = 0
    )
    private SoundEvent shatsu$selectFoodSound(SoundEvent original, RandomSource random,
                                               LivingEntity user, ItemStack stack, int particleCount) {
        if (!user.level().isClientSide()) return original;
        return ShatsuFoodAudio.select(stack, original);
    }
}
