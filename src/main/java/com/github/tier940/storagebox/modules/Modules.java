package com.github.tier940.storagebox.modules;

import com.github.tier940.storagebox.Tags;
import com.github.tier940.storagebox.api.modules.IModuleContainer;

public class Modules implements IModuleContainer {

    public static final String MODULE_CORE = "core";

    @Override
    public String getID() {
        return Tags.MODID;
    }
}
