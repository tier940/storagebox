package com.github.tier940.storagebox.client;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.github.tier940.storagebox.StorageBoxMod;
import com.github.tier940.storagebox.Tags;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public class StorageBoxClientEvents {

    private static final ModelResourceLocation BOX_MRL =
            new ModelResourceLocation(new ResourceLocation(Tags.MODID, "storagebox"), "inventory");

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(StorageBoxMod.itemStorageBox, 0, BOX_MRL);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IBakedModel base = event.getModelRegistry().getObject(BOX_MRL);
        if (base != null) {
            event.getModelRegistry().putObject(BOX_MRL, new StorageBoxBakedModel(base));
        }
    }
}
