package com.github.tier940.storagebox.client;

import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedModelWrapper;

import com.github.tier940.storagebox.StorageBoxMod;
import com.github.tier940.storagebox.StorageBoxNBT;

public class StorageBoxBakedModel extends BakedModelWrapper<IBakedModel> {

    private final ItemOverrideList overrides;

    public StorageBoxBakedModel(IBakedModel base) {
        super(base);
        this.overrides = new OverrideList(base);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    private static final class OverrideList extends ItemOverrideList {

        private final IBakedModel emptyModel;

        OverrideList(IBakedModel emptyModel) {
            super(Collections.emptyList());
            this.emptyModel = emptyModel;
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                           @Nullable World world, @Nullable EntityLivingBase entity) {
            ItemStack template = StorageBoxNBT.peekTemplate(stack);
            // Guard against empty or nested storage box to avoid infinite delegation.
            if (template.isEmpty() || template.getItem() == StorageBoxMod.itemStorageBox) {
                return emptyModel;
            }
            return Minecraft.getMinecraft().getRenderItem()
                    .getItemModelWithOverrides(template, world, entity);
        }
    }
}
