package com.github.tier940.storagebox;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Single-slot IItemHandler view over a StorageBox ItemStack. All operations read or write the
 * stack's NBT through {@link StorageBoxNBT}; the visible slot count is clamped to the contained
 * template's max stack size so vanilla slot UIs still behave.
 */
final class StorageBoxItemHandler implements IItemHandlerModifiable {

    private final ItemStack box;
    private final int capacity;

    StorageBoxItemHandler(ItemStack box, int capacity) {
        this.box = box;
        this.capacity = capacity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack template = StorageBoxNBT.peekTemplate(box);
        if (template.isEmpty()) return ItemStack.EMPTY;
        // Return the real count, not clamped to max-stack-size. MUI's transferStackInSlot handles
        // counts greater than maxStackSize correctly (splits with `base`), and Forge's
        // SlotItemHandler.isItemValid does setStackInSlot(EMPTY) + setStackInSlot(currentStack) as
        // a side effect — handing it the real count preserves the chest contents through that.
        ItemStack copy = template.copy();
        copy.setCount(StorageBoxNBT.getCount(box));
        return copy;
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) return stack;
        ItemStack template = StorageBoxNBT.peekTemplate(box);
        int currentCount = StorageBoxNBT.getCount(box);
        if (!template.isEmpty() && !StorageBoxNBT.matches(template, stack)) return stack;
        int room = capacity - currentCount;
        if (room <= 0) return stack;
        int accepted = Math.min(stack.getCount(), room);
        if (!simulate) {
            StorageBoxNBT.insert(box, stack, capacity);
        }
        ItemStack leftover = stack.copy();
        leftover.setCount(stack.getCount() - accepted);
        return leftover.getCount() <= 0 ? ItemStack.EMPTY : leftover;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || amount <= 0) return ItemStack.EMPTY;
        ItemStack template = StorageBoxNBT.peekTemplate(box);
        int currentCount = StorageBoxNBT.getCount(box);
        if (template.isEmpty() || currentCount <= 0) return ItemStack.EMPTY;
        int taken = Math.min(amount, Math.min(currentCount, template.getMaxStackSize()));
        if (simulate) {
            ItemStack out = template.copy();
            out.setCount(taken);
            return out;
        }
        return StorageBoxNBT.extract(box, taken);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot != 0) return;
        // Vanilla-faithful complete replacement so Forge's isItemValid side-effect
        // (setStackInSlot(EMPTY) followed by setStackInSlot(currentStack)) round-trips
        // without corrupting the box.
        StorageBoxNBT.clear(box);
        if (!stack.isEmpty()) {
            ItemStack toStore = stack.copy();
            toStore.setCount(Math.min(stack.getCount(), capacity));
            StorageBoxNBT.insert(box, toStore, capacity);
        }
    }
}
