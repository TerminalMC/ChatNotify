/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.screen;

import dev.terminalmc.chatnotify.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.terminalmc.chatnotify.gui.widget.list.option.MainOptionList;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * The root {@link OptionsScreen}, containing a {@link MainOptionList}.
 */
public class MainOptionsScreen extends OptionsScreen {
    public MainOptionsScreen(Screen lastScreen) {
        super(lastScreen, localized("option", "main"),
                new MainOptionList(Minecraft.getInstance(), 0, 0, OptionsScreen.TOP_MARGIN,
                        OptionsScreen.LIST_ENTRY_SPACE, OptionsScreen.BASE_LIST_ENTRY_WIDTH,
                        OptionsScreen.LIST_ENTRY_HEIGHT));
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
