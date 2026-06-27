package com.github.tier940.storagebox;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.github.tier940.storagebox.slot.SingleSlotInputView;
import com.github.tier940.storagebox.slot.SingleSlotOutputView;
import com.github.tier940.storagebox.slot.SingleStackModularSlot;

/**
 * Re-imagined StorageBox: a single inventory slot that holds many stacks of one item template.
 * Right-click opens the MUI panel; sneak right-click empties everything held in one go.
 */
public class ItemStorageBox extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    public ItemStorageBox() {
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return !StorageBoxNBT.isEmpty(stack);
    }

    /**
     * Sneak + Q (the drop key) ejects one stack of the box's contents on the ground while keeping
     * the box in the player's hand. Plain Q drops the box itself as normal.
     */
    @Override
    public boolean onDroppedByPlayer(ItemStack box, EntityPlayer player) {
        if (!player.isSneaking() || StorageBoxNBT.isEmpty(box)) return true;
        if (!player.world.isRemote) {
            ItemStack template = StorageBoxNBT.peekTemplate(box);
            if (!template.isEmpty()) {
                ItemStack drop = StorageBoxNBT.extract(box, template.getMaxStackSize());
                if (!drop.isEmpty()) player.dropItem(drop, false);
            }
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack box = player.getHeldItem(hand);
        // Sneak right-click toggles auto-collect (fires only when not aimed at a block — Vanilla
        // routes block targets through onItemUseFirst first).
        if (player.isSneaking()) {
            boolean newState = !StorageBoxNBT.isAutoCollectEnabled(box);
            StorageBoxNBT.setAutoCollectEnabled(box, newState);
            if (!world.isRemote) {
                player.sendStatusMessage(
                        new TextComponentTranslation(newState ? "message.storagebox.storagebox.auto_on" :
                                "message.storagebox.storagebox.auto_off"),
                        true);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, box);
        }
        // Empty-air right-click opens the GUI.
        if (!world.isRemote) {
            PlayerInventoryGuiFactory.INSTANCE.openFromHand(player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, box);
    }

    /**
     * Right-clicking a block with the box. Priority:
     * <ol>
     * <li>Target exposes an IItemHandler (chest, barrel, modded inventory): dump as much of the
     * box's contents into it as fits.</li>
     * <li>Box contains an ItemBlock: place one.</li>
     * <li>Otherwise fall through.</li>
     * </ol>
     */
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           EnumFacing facing, float hitX, float hitY, float hitZ,
                                           EnumHand hand) {
        ItemStack box = player.getHeldItem(hand);

        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
            if (inventory == null) {
                inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            }
            if (inventory != null) {
                if (!world.isRemote && !StorageBoxNBT.isEmpty(box)) {
                    dumpIntoInventory(box, inventory);
                }
                return EnumActionResult.SUCCESS;
            }
        }

        if (StorageBoxNBT.isEmpty(box)) return EnumActionResult.PASS;
        ItemStack template = StorageBoxNBT.peekTemplate(box);
        if (!(template.getItem() instanceof ItemBlock)) return EnumActionResult.PASS;

        ItemStack one = StorageBoxNBT.extract(box, 1);
        if (one.isEmpty()) return EnumActionResult.PASS;
        // Temporarily swap the held item to the extracted single stack so the ItemBlock places
        // through its own onItemUse, then put the box back and reabsorb any leftover.
        player.setHeldItem(hand, one);
        EnumActionResult result = one.getItem().onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        ItemStack leftover = player.getHeldItem(hand);
        player.setHeldItem(hand, box);
        if (!leftover.isEmpty()) {
            StorageBoxNBT.insert(box, leftover, StorageBoxConfigHolder.capacity);
        }
        return result == EnumActionResult.PASS ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    }

    /**
     * Pushes everything in the box into the given inventory, one max-stack chunk at a time.
     * Leftover (what did not fit) stays in the box.
     */
    private static void dumpIntoInventory(ItemStack box, IItemHandler inventory) {
        ItemStack template = StorageBoxNBT.peekTemplate(box);
        if (template.isEmpty()) return;
        int remaining = StorageBoxNBT.getCount(box);
        while (remaining > 0) {
            int chunk = Math.min(remaining, template.getMaxStackSize());
            ItemStack toInsert = template.copy();
            toInsert.setCount(chunk);
            ItemStack leftover = ItemHandlerHelper.insertItem(inventory, toInsert, false);
            int accepted = chunk - leftover.getCount();
            if (accepted <= 0) break;
            remaining -= accepted;
        }
        StorageBoxNBT.clear(box);
        if (remaining > 0) {
            ItemStack restore = template.copy();
            restore.setCount(remaining);
            StorageBoxNBT.insert(box, restore, StorageBoxConfigHolder.capacity);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ItemStack template = StorageBoxNBT.peekTemplate(stack);
        if (template.isEmpty()) return super.getItemStackDisplayName(stack);
        return super.getItemStackDisplayName(stack) + " (" + template.getDisplayName() + ")";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        ItemStack template = StorageBoxNBT.peekTemplate(stack);
        if (template.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.storagebox.storagebox.empty"));
        } else {
            int count = StorageBoxNBT.getCount(stack);
            tooltip.add(TextFormatting.WHITE + template.getDisplayName());
            tooltip.add(TextFormatting.YELLOW.toString() + count + TextFormatting.GRAY + " / " +
                    StorageBoxConfigHolder.capacity);
        }
        boolean auto = StorageBoxNBT.isAutoCollectEnabled(stack);
        tooltip.add((auto ? TextFormatting.GREEN : TextFormatting.RED) + I18n.format(
                auto ? "tooltip.storagebox.storagebox.auto_on" : "tooltip.storagebox.storagebox.auto_off"));
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.storagebox.storagebox.usage.right_click"));
            tooltip.add(
                    TextFormatting.DARK_GRAY + I18n.format("tooltip.storagebox.storagebox.usage.sneak_right_click"));
            tooltip.add(TextFormatting.DARK_GRAY +
                    I18n.format("tooltip.storagebox.storagebox.usage.right_click_inventory"));
            tooltip.add(
                    TextFormatting.DARK_GRAY + I18n.format("tooltip.storagebox.storagebox.usage.right_click_block"));
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.storagebox.storagebox.usage.sneak_drop"));
        } else {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.storagebox.storagebox.shift_hint"));
        }
    }

    /** Empty boxes only — a stored template would make creative-tab listing balloon. */
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) items.add(new ItemStack(this));
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack box = data.getUsedItemStack();
        StorageBoxItemHandler handler = new StorageBoxItemHandler(box, StorageBoxConfigHolder.capacity);
        ModularPanel panel = ModularPanel.defaultPanel("storagebox").size(176, 173);
        // Not singleton: icon / IN / OUT all share handler index 0 and singleton rejects more
        // than one slot at runtime.
        SlotGroup group = new SlotGroup("storagebox", 1, SlotGroup.STORAGE_SLOT_PRIO, true);
        syncManager.registerSlotGroup(group);

        // Read-only icon at the very top-left mirroring the stored template.
        ModularSlot iconSlot = new ModularSlot(handler, 0) {

            @Override
            public int getSlotStackLimit() {
                return StorageBoxConfigHolder.capacity;
            }
        }.ignoreMaxStackSize(true).slotGroup(group).accessibility(false, false);
        panel.child(new ItemSlot().slot(iconSlot).pos(9, 9));

        // Two info lines to the right of the icon: template name and item count.
        panel.child(new TextWidget<>(IKey.dynamic(() -> renderTemplateName(box))).pos(31, 9).size(140, 10));
        panel.child(new TextWidget<>(IKey.dynamic(() -> renderItemCount(box))).pos(31, 19).size(140, 10));
        panel.child(new TextWidget<>(IKey.dynamic(() -> renderLcCount(box))).pos(31, 29).size(30, 10));

        // IN slot — insert-only, always shows empty.
        ModularSlot inSlot = new SingleStackModularSlot(new SingleSlotInputView(handler), 0) {

            @Override
            public int getSlotStackLimit() {
                return StorageBoxConfigHolder.capacity;
            }
        }.ignoreMaxStackSize(true).slotGroup(group).canTake(false);
        panel.child(new TextWidget<>(IKey.lang("gui.storagebox.storagebox.in")).pos(116, 45).size(16, 8));
        panel.child(new ItemSlot().slot(inSlot).pos(116, 55));

        // OUT slot — take-only, capped at one max-stack-size chunk.
        ModularSlot outSlot = new SingleStackModularSlot(new SingleSlotOutputView(handler), 0)
                .ignoreMaxStackSize(true).slotGroup(group).canPut(false);
        panel.child(new TextWidget<>(IKey.lang("gui.storagebox.storagebox.out")).pos(149, 45).size(20, 8));
        panel.child(new ItemSlot().slot(outSlot).pos(149, 55));

        // Inventory label sits just above the player inventory grid.
        panel.child(new TextWidget<>(IKey.lang("container.inventory")).pos(8, 76).size(80, 10));

        panel.bindPlayerInventory(9);
        return panel;
    }

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Tags.MODID, mainPanel);
    }

    private static String renderTemplateName(ItemStack box) {
        ItemStack template = StorageBoxNBT.peekTemplate(box);
        return template.isEmpty() ? I18n.format("gui.storagebox.storagebox.template_empty") : template.getDisplayName();
    }

    private static String renderItemCount(ItemStack box) {
        return I18n.format("gui.storagebox.storagebox.items", StorageBoxNBT.getCount(box));
    }

    private static String renderLcCount(ItemStack box) {
        return I18n.format("gui.storagebox.storagebox.lc", StorageBoxNBT.getCount(box) / 3456);
    }
}
