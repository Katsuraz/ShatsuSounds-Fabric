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

/**
 * Slot action hook: picking up, placing, swapping, and shift-transferring items.
 * Runs only on the client, never changes the inventory action, and does not
 * intercept the separately approved inventory-open event.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ShatsuInventoryScreenMixin {
    @Shadow protected AbstractContainerMenu menu;

    @Inject(method = "slotClicked", at = @At("HEAD"), require = 0)
    private void shatsu$inventorySound(Slot slot, int slotId, int mouseButton,
                                       ContainerInput clickType, CallbackInfo ci) {
        ShatsuInventoryAudio.clicked(slot, menu.getCarried(), clickType);
    }
}
