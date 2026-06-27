package com.github.tier940.storagebox.slot;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Slot-0 view that exposes at most one max-stack-size chunk of the delegate's content, rejects
 * insertions, and translates putStack calls into delegate extractions so MUI's shift-click
 * transfer pulls items out of the underlying handler one stack at a time.
 */
public final class SingleSlotOutputView implements IItemHandlerModifiable {

    private final IItemHandlerModifiable delegate;
    private int lastVisible = 0;

    public SingleSlotOutputView(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack stack = delegate.getStackInSlot(0);
        if (stack.isEmpty()) {
            lastVisible = 0;
            return ItemStack.EMPTY;
        }
        int visible = Math.min(stack.getCount(), stack.getMaxStackSize());
        lastVisible = visible;
        ItemStack copy = stack.copy();
        copy.setCount(visible);
        return copy;
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0) return ItemStack.EMPTY;
        return delegate.extractItem(0, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        // Translate the change against the last-reported visible count into an extraction so
        // MUI's transferStackInSlot (which calls putStack(EMPTY) after pulling a stack) ends up
        // removing exactly that visible chunk from the delegate.
        if (slot != 0) return;
        int newCount = stack.isEmpty() ? 0 : stack.getCount();
        int delta = newCount - lastVisible;
        if (delta < 0) {
            delegate.extractItem(0, -delta, false);
        }
        lastVisible = newCount;
    }
}
