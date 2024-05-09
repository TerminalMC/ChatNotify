/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.screen;

import com.notryken.chatnotify.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.notryken.chatnotify.gui.widget.list.MainOptionsList;

/**
 * <b>Note:</b> If creating a ChatNotify config screen (e.g. for ModMenu
 * integration), instantiate this class, not {@code OptionsScreen}.
 */
public class GlobalOptionsScreen extends OptionsScreen {

    public GlobalOptionsScreen(Screen lastScreen) {
        super(lastScreen, Component.translatable("screen.chatnotify.title.default"),
                new MainOptionsList(Minecraft.getInstance(), 0, 0, 0,
                        0, -120, 240, 20, 320));
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
