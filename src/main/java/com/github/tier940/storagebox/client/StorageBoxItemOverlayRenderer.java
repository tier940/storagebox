package com.github.tier940.storagebox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.github.tier940.storagebox.StorageBoxMod;
import com.github.tier940.storagebox.StorageBoxNBT;
import com.github.tier940.storagebox.Tags;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public class StorageBoxItemOverlayRenderer {

    private static final float ICON_SCALE = 6f / 16f;

    /** Inventory / container slots. DrawForeground runs after items are already rendered. */
    @SubscribeEvent
    public static void onDrawForeground(GuiContainerEvent.DrawForeground event) {
        Minecraft mc = Minecraft.getMinecraft();
        for (Slot slot : event.getGuiContainer().inventorySlots.inventorySlots) {
            ItemStack stack = slot.getStack();
            if (stack.getItem() != StorageBoxMod.itemStorageBox) continue;
            if (StorageBoxNBT.isEmpty(stack)) continue;
            drawBoxIcon(mc, slot.xPos, slot.yPos);
        }
    }

    /** Hotbar slots. */
    @SubscribeEvent
    public static void onRenderHotbar(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        // Item render origin within each 20px hotbar slot: +2 left, +3 top of 22px bar.
        int baseX = sr.getScaledWidth() / 2 - 91 + 2;
        int baseY = sr.getScaledHeight() - 22 + 3;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() != StorageBoxMod.itemStorageBox) continue;
            if (StorageBoxNBT.isEmpty(stack)) continue;
            drawBoxIcon(mc, baseX + i * 20, baseY);
        }
    }

    /**
     * Draws a 6px storage box icon at the top-right corner of the given slot position.
     * The slot occupies a 16x16 logical area starting at (x, y).
     */
    private static void drawBoxIcon(Minecraft mc, int x, int y) {
        GlStateManager.pushMatrix();
        // Top-right: offset by (16 - 6) = 10 px from slot left edge.
        GlStateManager.translate(x + 10, y, 250f);
        GlStateManager.scale(ICON_SCALE, ICON_SCALE, 1f);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemIntoGUI(new ItemStack(StorageBoxMod.itemStorageBox), 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
