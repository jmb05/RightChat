package net.jmb19905.rightchat;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;

@Environment(EnvType.CLIENT)
public class RightChat implements ClientModInitializer {

    public static final String MOD_ID = "rightchat";

    public static ModConfig config = null;
    public static SimpleOption<Boolean> chatOnRight;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        chatOnRight = SimpleOption.ofBoolean("fun", true, b -> config.chatOnRight = b);
    }
}
