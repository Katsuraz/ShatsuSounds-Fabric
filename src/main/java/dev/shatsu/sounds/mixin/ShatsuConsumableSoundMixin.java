package dev.shatsu.sounds.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shatsu.sounds.client.ShatsuFoodAudio;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wrap, rather than redirect, the actual vanilla chewing call. This chains
 * with other compatible sound injectors. Consume a different sound without
 * creating a duplicate vanilla eating event.
 */
@Mixin(Consumable.class)
public abstract class ShatsuConsumableSoundMixin {
    @WrapOperation(method = "emitParticlesAndSounds",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"),
        require = 0)
    private void shatsu$playFood(
            LivingEntity receiver, SoundEvent original, float volume, float pitch,
            Operation<Void> operation,
            RandomSource random, LivingEntity user, ItemStack stack, int particleCount) {
        SoundEvent chosen = receiver.level().isClientSide()
                ? ShatsuFoodAudio.select(stack, original, "local") : original;
        operation.call(receiver, chosen, volume, pitch);
    }
}
