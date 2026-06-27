package com.github.tier940.storagebox;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Routes picked-up items into any matching StorageBox in the player's inventory before they hit
 * vanilla pickup logic. If everything fits, the EntityItem is consumed.
 */
public class StorageBoxPickupHandler {

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        EntityItem entityItem = event.getItem();
        if (player == null || entityItem == null) return;
        ItemStack picked = entityItem.getItem();
        if (picked.isEmpty()) return;

        InventoryPlayer inventory = player.inventory;
        ItemStack remainder = picked;
        for (int i = 0; i < inventory.getSizeInventory() && !remainder.isEmpty(); i++) {
            ItemStack box = inventory.getStackInSlot(i);
            if (!(box.getItem() instanceof ItemStorageBox)) continue;
            if (!StorageBoxNBT.isAutoCollectEnabled(box)) continue;
            ItemStack template = StorageBoxNBT.peekTemplate(box);
            if (template.isEmpty() || !StorageBoxNBT.matches(template, remainder)) continue;
            remainder = StorageBoxNBT.insert(box, remainder, StorageBoxConfigHolder.capacity);
            inventory.markDirty();
        }

        if (remainder.isEmpty()) {
            entityItem.setDead();
            event.setResult(Result.ALLOW);
        } else if (remainder.getCount() != picked.getCount()) {
            entityItem.setItem(remainder);
        }
    }
}
