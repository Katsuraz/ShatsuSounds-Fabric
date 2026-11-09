package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuRemoteFoodAudio;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-only packet hooks for generic eating emitted by multiplayer servers.
 * Cancel original playback only if Shatsu successfully plays a replacement.
 */
@Mixin(ClientPacketListener.class)
public abstract class ShatsuRemoteFoodSoundMixin {
    @Inject(method = "handleSoundEvent",
        at = @At(value = "INVOKE",
           target = "Lnet/minecraft/client/multiplayer/ClientLevel;playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V"),
        cancellable = true, require = 0)
    private void shatsu$onPositionedFoodSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (ShatsuRemoteFoodAudio.replacePositioned(
                packet.getSound().value(), packet.getSource(),
                packet.getX(), packet.getY(), packet.getZ(),
                packet.getVolume(), packet.getPitch())) ci.cancel();
    }

    @Inject(method = "handleSoundEntityEvent",
        at = @At(value = "INVOKE",
           target = "Lnet/minecraft/client/multiplayer/ClientLevel;playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V"),
        cancellable = true, require = 0)
    private void shatsu$onEntityFoodSound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var player = level.getEntity(packet.getId());
        if (player != null && ShatsuRemoteFoodAudio.replaceEntity(
                player, packet.getSound().value(), packet.getSource(),
                packet.getVolume(), packet.getPitch())) ci.cancel();
    }
}
