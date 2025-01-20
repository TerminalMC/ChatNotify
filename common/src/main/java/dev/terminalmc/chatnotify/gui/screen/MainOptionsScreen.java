/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.gui.screen;

import dev.terminalmc.chatnotify.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.terminalmc.chatnotify.gui.widget.list.option.MainOptionList;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * The root {@link OptionsScreen}, containing a {@link MainOptionList}.
 * 
 * <p>Config is saved only when this {@link Screen} is closed.</p>
 */
public class MainOptionsScreen extends OptionsScreen {
    public MainOptionsScreen(Screen lastScreen) {
        super(lastScreen, localized("option", "main"),
                new MainOptionList(
                        Minecraft.getInstance(), 
                        0, 
                        0, 
                        OptionsScreen.TOP_MARGIN,
                        OptionsScreen.LIST_ENTRY_SPACE, 
                        OptionsScreen.BASE_LIST_ENTRY_WIDTH,
                        OptionsScreen.LIST_ENTRY_HEIGHT
                ));
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
