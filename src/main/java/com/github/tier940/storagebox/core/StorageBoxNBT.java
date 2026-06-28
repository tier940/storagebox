package com.github.tier940.storagebox.core;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * NBT accessor for a StorageBox item stack. The box stores one item template plus a 32-bit count;
 * the count is kept separately from the template's own size so the template can be serialized with
 * count = 1 (avoiding the vanilla 64 limit on the NBT-embedded stack).
 */
public final class StorageBoxNBT {

    private static final String KEY_CONTENTS = "lc:contents";
    private static final String KEY_COUNT = "lc:count";
    private static final String KEY_AUTO_COLLECT = "lc:auto";

    private StorageBoxNBT() {}

    public static boolean isAutoCollectEnabled(ItemStack box) {
        NBTTagCompound tag = box.getTagCompound();
        // Default ON: a newly crafted box should pick things up unless explicitly disabled.
        return tag == null || !tag.hasKey(KEY_AUTO_COLLECT) || tag.getBoolean(KEY_AUTO_COLLECT);
    }

    public static void setAutoCollectEnabled(ItemStack box, boolean enabled) {
        NBTTagCompound tag = ensureTag(box);
        tag.setBoolean(KEY_AUTO_COLLECT, enabled);
    }

    /**
     * @return a copy of the stored item template with count = 1, or {@link ItemStack#EMPTY}.
     */
    public static ItemStack peekTemplate(ItemStack box) {
        NBTTagCompound tag = box.getTagCompound();
        if (tag == null || !tag.hasKey(KEY_CONTENTS)) return ItemStack.EMPTY;
        NBTTagCompound contents = tag.getCompoundTag(KEY_CONTENTS);
        if (contents.isEmpty()) return ItemStack.EMPTY;
        ItemStack template = new ItemStack(contents);
        if (template.isEmpty()) return ItemStack.EMPTY;
        template.setCount(1);
        return template;
    }

    public static int getCount(ItemStack box) {
        NBTTagCompound tag = box.getTagCompound();
        return tag == null ? 0 : Math.max(0, tag.getInteger(KEY_COUNT));
    }

    public static boolean isEmpty(ItemStack box) {
        return getCount(box) <= 0 || peekTemplate(box).isEmpty();
    }

    /**
     * Accepts {@code addition} into {@code box}. Returns the leftover (anything that did not fit
     * either because the template differs or the per-box capacity is exhausted).
     */
    public static ItemStack insert(ItemStack box, ItemStack addition, int capacity) {
        if (addition.isEmpty()) return addition;
        ItemStack template = peekTemplate(box);
        int currentCount = getCount(box);

        if (template.isEmpty() || currentCount == 0) {
            ItemStack newTemplate = addition.copy();
            newTemplate.setCount(1);
            int accepted = Math.min(addition.getCount(), capacity);
            writeTemplate(box, newTemplate);
            writeCount(box, accepted);
            ItemStack leftover = addition.copy();
            leftover.setCount(addition.getCount() - accepted);
            return leftover.getCount() <= 0 ? ItemStack.EMPTY : leftover;
        }

        if (!matches(template, addition)) return addition;

        int space = capacity - currentCount;
        if (space <= 0) return addition;
        int accepted = Math.min(addition.getCount(), space);
        writeCount(box, currentCount + accepted);
        ItemStack leftover = addition.copy();
        leftover.setCount(addition.getCount() - accepted);
        return leftover.getCount() <= 0 ? ItemStack.EMPTY : leftover;
    }

    /**
     * Removes up to {@code maxAmount} items from the box and returns them. May return EMPTY.
     */
    public static ItemStack extract(ItemStack box, int maxAmount) {
        ItemStack template = peekTemplate(box);
        int currentCount = getCount(box);
        if (template.isEmpty() || currentCount <= 0 || maxAmount <= 0) return ItemStack.EMPTY;

        int taken = Math.min(maxAmount, Math.min(currentCount, template.getMaxStackSize()));
        ItemStack out = template.copy();
        out.setCount(taken);
        int remaining = currentCount - taken;
        if (remaining <= 0) {
            clear(box);
        } else {
            writeCount(box, remaining);
        }
        return out;
    }

    public static boolean matches(ItemStack template, ItemStack other) {
        return !template.isEmpty() && !other.isEmpty() && template.getItem() == other.getItem() &&
                template.getMetadata() == other.getMetadata() && ItemStack.areItemStackTagsEqual(template, other);
    }

    public static void clear(ItemStack box) {
        NBTTagCompound tag = box.getTagCompound();
        if (tag == null) return;
        tag.removeTag(KEY_CONTENTS);
        tag.removeTag(KEY_COUNT);
        if (tag.isEmpty()) box.setTagCompound(null);
    }

    private static void writeTemplate(ItemStack box, @Nullable ItemStack template) {
        NBTTagCompound tag = ensureTag(box);
        if (template == null || template.isEmpty()) {
            tag.removeTag(KEY_CONTENTS);
            return;
        }
        NBTTagCompound contents = new NBTTagCompound();
        template.writeToNBT(contents);
        tag.setTag(KEY_CONTENTS, contents);
    }

    static void writeCount(ItemStack box, int count) {
        NBTTagCompound tag = ensureTag(box);
        if (count <= 0) {
            tag.removeTag(KEY_COUNT);
        } else {
            tag.setInteger(KEY_COUNT, count);
        }
    }

    private static NBTTagCompound ensureTag(ItemStack box) {
        NBTTagCompound tag = box.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            box.setTagCompound(tag);
        }
        return tag;
    }
}
