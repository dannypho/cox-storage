package com.coxstorage;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("coxstorage")
public interface CoxStorageConfig extends Config
{
    @ConfigItem(keyName = "uniqueKey", name = "Display text", description = "Hover text")
    default boolean myCheckbox()
    {
        return true;
    }
}
