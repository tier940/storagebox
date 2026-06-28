package com.github.tier940.storagebox.common;

import net.minecraftforge.common.config.Config;

import com.github.tier940.storagebox.Tags;

@Config.LangKey(Tags.MODID + ".config.storagebox")
@Config(modid = Tags.MODID,
        name = Tags.MODID + "/storagebox",
        category = "StorageBox")
public class StorageBoxConfigHolder {

    @Config.Comment({ "Maximum number of items a single StorageBox can hold.",
            "Default: " + Integer.MAX_VALUE + " (matches the original mod, effectively unlimited)" })
    @Config.RangeInt(min = 64, max = Integer.MAX_VALUE)
    @Config.RequiresMcRestart
    public static int capacity = Integer.MAX_VALUE;
}
