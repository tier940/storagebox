package com.github.tier940.storagebox;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.github.tier940.storagebox.core.StorageBoxCoreModule;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION, useMetadata = true,
        acceptedMinecraftVersions = "[1.12,1.13)")
@Mod.EventBusSubscriber(modid = Tags.MODID)
public class StorageBoxMod {

    public static ItemStorageBox itemStorageBox;

    private static final StorageBoxCoreModule coreModule = new StorageBoxCoreModule();

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        coreModule.postInit(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        coreModule.registerItems(event);
    }
}
