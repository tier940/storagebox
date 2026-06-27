package com.github.tier940.storagebox.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.github.tier940.storagebox.slot.SingleStackTransferSlot;

/**
 * Caps shift-click extraction at a single stack for slots tagged with
 * {@link SingleStackTransferSlot}. MUI's vanilla loop keeps calling {@code transferStackInSlot}
 * until the source slot reports empty, which drains bulk-storage slots in one click. We run the
 * transfer exactly once for those slots and return the result.
 */
@Mixin(value = ModularContainer.class, remap = false)
public abstract class MixinModularContainer {

    @Inject(method = "handleQuickMove", at = @At("HEAD"), cancellable = true)
    private void storageBoxOneStackOnly(EntityPlayer player, int slotId, Slot fromSlot,
                                        CallbackInfoReturnable<ItemStack> cir) {
        if (!(fromSlot instanceof SingleStackTransferSlot)) return;
        ItemStack remainder = ((ModularContainer) (Object) this).transferStackInSlot(player, slotId);
        cir.setReturnValue(remainder);
    }
}
