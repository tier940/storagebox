package com.github.tier940.storagebox.mixins.modularui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.github.tier940.storagebox.slot.SingleStackTransferSlot;

@Mixin(value = ModularContainer.class, remap = false)
public abstract class ModularContainerMixin {

    @Inject(method = "handleQuickMove", at = @At("HEAD"), cancellable = true)
    private void storageBoxOneStackOnly(EntityPlayer player, int slotId, Slot fromSlot,
                                        CallbackInfoReturnable<ItemStack> cir) {
        if (!(fromSlot instanceof SingleStackTransferSlot)) return;
        ItemStack remainder = ((ModularContainer) (Object) this).transferStackInSlot(player, slotId);
        cir.setReturnValue(remainder);
    }
}
