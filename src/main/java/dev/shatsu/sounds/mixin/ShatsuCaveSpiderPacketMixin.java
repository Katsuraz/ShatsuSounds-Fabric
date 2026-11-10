package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuCaveSpiderRouting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-only packet substitution: cancel original Spider audio only on a
 * positively identified Cave Spider, so no legacy/double-play noise.
 */
@Mixin(ClientPacketListener.class)
public abstract class ShatsuCaveSpiderPacketMixin {
    @Inject(method = "handleSoundEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private void shatsu$caveSpiderPositionSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        // Packet handlers also run on a network thread before handing off to client.
        if (!Minecraft.getInstance().isSameThread()) return;
        if (ShatsuCaveSpiderRouting.positionedSound(packet.getX(), packet.getY(), packet.getZ(),
                packet.getSound().value(), packet.getSource(), packet.getVolume(), packet.getPitch()))
            ci.cancel();
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private void shatsu$caveSpiderEntitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) return;
        if (ShatsuCaveSpiderRouting.entitySound(packet.getId(), packet.getSound().value(),
                packet.getSource(), packet.getVolume(), packet.getPitch()))
            ci.cancel();
    }
}
