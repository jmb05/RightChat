package net.jmb19905.rightchat;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = RightChat.MOD_ID)
public class ModConfig implements ConfigData {
    public boolean chatOnRight = true;
}