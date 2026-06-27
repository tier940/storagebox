package com.github.tier940.storagebox.core;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.tier940.storagebox.ItemStorageBox;
import com.github.tier940.storagebox.StorageBoxMod;
import com.github.tier940.storagebox.StorageBoxPickupHandler;
import com.github.tier940.storagebox.Tags;
import com.github.tier940.storagebox.api.modules.IModule;
import com.github.tier940.storagebox.api.modules.TModule;
import com.github.tier940.storagebox.modules.Modules;

@TModule(moduleID = Modules.MODULE_CORE, containerID = Tags.MODID, name = "Storage Box Core", coreModule = true)
public class StorageBoxCoreModule implements IModule {

    private static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new StorageBoxPickupHandler());
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        StorageBoxMod.itemStorageBox = new ItemStorageBox();
        StorageBoxMod.itemStorageBox.setRegistryName(Tags.MODID, "storagebox");
        StorageBoxMod.itemStorageBox.setTranslationKey(Tags.MODID + ".storagebox");
        event.getRegistry().register(StorageBoxMod.itemStorageBox);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
