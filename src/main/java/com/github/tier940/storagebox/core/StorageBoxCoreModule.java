package com.github.tier940.storagebox.core;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.github.tier940.storagebox.StorageBoxMod;
import com.github.tier940.storagebox.Tags;
import com.github.tier940.storagebox.api.ModValues;
import com.github.tier940.storagebox.api.modules.IModule;
import com.github.tier940.storagebox.api.modules.TModule;
import com.github.tier940.storagebox.common.CommonProxy;
import com.github.tier940.storagebox.modules.Modules;

@TModule(
         moduleID = Modules.MODULE_CORE,
         containerID = ModValues.MODID,
         name = "Storage Box Core",
         description = "Core of StorageBox",
         coreModule = true)
public class StorageBoxCoreModule implements IModule {

    public static final Logger logger = LogManager.getLogger(Tags.MODNAME + " Core");

    @SidedProxy(modId = ModValues.MODID,
                clientSide = "com.github.tier940.storagebox.client.ClientProxy",
                serverSide = "com.github.tier940.storagebox.common.CommonProxy")
    public static CommonProxy proxy;

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public void construction(FMLConstructionEvent event) {}

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        logger.info("Hello World!");
    }

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
}
