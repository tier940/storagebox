package com.github.tier940.storagebox.modules;

import com.github.tier940.storagebox.api.ModValues;
import com.github.tier940.storagebox.api.modules.IModuleContainer;
import com.github.tier940.storagebox.api.modules.ModuleContainer;

@ModuleContainer
public class Modules implements IModuleContainer {

    public static final String MODULE_CORE = "core";
    public static final String MODULE_TOOLS = "tools";
    public static final String MODULE_INTEGRATION = "integration";

    @Override
    public String getID() {
        return ModValues.MODID;
    }
}
