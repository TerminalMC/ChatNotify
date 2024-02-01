package com.notryken.chatnotify;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import com.notryken.chatnotify.gui.screen.GlobalConfigScreen;

@Mod(ChatNotify.MOD_ID)
public class ChatNotifyNeoForge {
    public ChatNotifyNeoForge() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new GlobalConfigScreen(parent))
                );

        ChatNotify.init();
    }
}