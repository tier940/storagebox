package com.github.tier940.storagebox.slot;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Slot-0 view that always reports empty, accepts insertions, and forwards them to a delegate.
 * Useful as the "IN" slot for bulk storage GUIs where the player only ever places items but
 * never sees what is held.
 */
public final class SingleSlotInputView implements IItemHandlerModifiable {

    private final IItemHandlerModifiable delegate;

    public SingleSlotInputView(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return delegate.insertItem(0, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(0);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        // Drag-and-drop / putStack flow: route through insertItem so we never overwrite the
        // delegate. Empty stacks are ignored so Forge's isItemValid round-trip
        // (setStackInSlot(EMPTY) + setStackInSlot(currentStack)) is a no-op.
        if (slot != 0 || stack.isEmpty()) return;
        delegate.insertItem(0, stack, false);
    }
}
