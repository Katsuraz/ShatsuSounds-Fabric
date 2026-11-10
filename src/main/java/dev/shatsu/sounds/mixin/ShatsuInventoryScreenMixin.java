package dev.shatsu.sounds.mixin;

import dev.shatsu.sounds.client.ShatsuInventoryAudio;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Normal containers, including inherited slot click handlers. */
@Mixin(AbstractContainerScreen.class)
public abstract class ShatsuInventoryScreenMixin {
    @Shadow protected AbstractContainerMenu menu;

    @Inject(
        method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
        at = @At("HEAD"), require = 1)
    private void shatsu$inventorySound(Slot slot, int slotId, int mouseButton,
            ContainerInput type, CallbackInfo ci) {
        ShatsuInventoryAudio.clicked(slot, menu.getCarried(), type, mouseButton);
    }
}
