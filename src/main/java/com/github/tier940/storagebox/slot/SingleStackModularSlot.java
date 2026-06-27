package com.github.tier940.storagebox.slot;

import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

/**
 * {@link ModularSlot} flavored with the {@link SingleStackTransferSlot} marker so MUI's
 * shift-click handler stops after extracting one stack instead of looping until empty.
 */
public class SingleStackModularSlot extends ModularSlot implements SingleStackTransferSlot {

    public SingleStackModularSlot(IItemHandler handler, int index) {
        super(handler, index);
    }
}
