package com.notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.notryken.chatnotify.ChatNotify;
import com.notryken.chatnotify.gui.component.listwidget.GlobalConfigListWidget;

/**
 * <b>Note:</b> If creating a ChatNotify config screen (e.g. for ModMenu
 * integration), instantiate this class, not {@code ConfigScreen}.
 */
public class GlobalConfigScreen extends ConfigScreen {

    public GlobalConfigScreen(Screen lastScreen) {
        super(lastScreen, Component.translatable("screen.chatnotify.title.default"),
                new GlobalConfigListWidget(Minecraft.getInstance(), 0, 0, 0, 0,
                        0, -120, 240, 20, 320));
    }

    @Override
    public void onClose() {
        ChatNotify.config().writeToFile();
        super.onClose();
    }
}
