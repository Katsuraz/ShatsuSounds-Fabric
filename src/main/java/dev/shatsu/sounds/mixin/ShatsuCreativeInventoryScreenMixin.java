package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuInventoryAudio;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 26.3 Creative inventory overrides slotClicked: the injection into
 * AbstractContainerScreen alone never sees its normal creative-tab actions.
 * Keep this separate so the original inventory-open audio stays untouched.
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class ShatsuCreativeInventoryScreenMixin {
    @Inject(
        method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
        at = @At("HEAD"), require = 1
    )
    private void shatsu$creativeInventorySound(
            Slot slot, int slotId, int mouseButton, ContainerInput type, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        ShatsuInventoryAudio.clicked(slot, screen.getMenu().getCarried(), type, mouseButton);
    }
}
