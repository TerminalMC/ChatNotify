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
import dev.terminalmc.chatnotify.gui.widget.list.FilterList;
import dev.terminalmc.chatnotify.gui.widget.list.root.ControlList;
import dev.terminalmc.chatnotify.gui.widget.list.root.DefaultList;
import dev.terminalmc.chatnotify.gui.widget.list.root.PrefixList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.List;

import static dev.terminalmc.chatnotify.util.Localization.localized;
import static dev.terminalmc.chatnotify.util.Localization.translationKey;

/**
 * Supports a series of
 * {@link dev.terminalmc.chatnotify.gui.widget.list.OptionList}s for mod
 * configuration, and one to display the list of
 * {@link dev.terminalmc.chatnotify.config.Notification}s with widgets for
 * superficial configuration.
 *
 * <p><b>Note:</b> Configuration is saved only when this screen is closed.</p>
 */
public class RootScreen extends OptionScreen {

    public RootScreen(Screen lastScreen) {
        this(lastScreen, TabKey.NOTIFICATION.key);
    }

    public RootScreen(Screen lastScreen, String defaultKey) {
        super(lastScreen);
        addTabs(defaultKey);
    }

    private void addTabs(String defaultKey) {
        List<Tab> tabs = List.of(
                new Tab(TabKey.NOTIFICATION.key, (screen) -> new FilterList<>(
                        Minecraft.getInstance(), 0, 0, 0, 0,
                        BASE_LIST_ENTRY_WIDTH, LIST_ENTRY_HEIGHT, LIST_ENTRY_SPACING,
                        FilterList.Entry.NotifOptions.class,
                        (source, dest) -> Config.get().moveNotif(++source, ++dest),
                        localized("option", "notif.list", "\u2139"),
                        localized("option", "notif.list.tooltip"),
                        null,
                        null,
                        () -> Config.get().getNotifs(),
                        (x, width, height, list, notif, index) -> index == 0
                                ? new FilterList.Entry.NotifOptions.Locked(
                                x, width, height, list, notif)
                                : new FilterList.Entry.NotifOptions(
                                x, width, height, list, notif, index),
                        null,
                        () -> Config.get().addNotif()
                )),
                new Tab(TabKey.CONTROL.key, (screen) ->
                        new ControlList(Minecraft.getInstance(), 0, 0, 0, 0,
                                BASE_LIST_ENTRY_WIDTH, LIST_ENTRY_HEIGHT, LIST_ENTRY_SPACING
                        )),
                new Tab(TabKey.DEFAULT.key, (screen) ->
                        new DefaultList(Minecraft.getInstance(), 0, 0, 0, 0,
                                BASE_LIST_ENTRY_WIDTH, LIST_ENTRY_HEIGHT, LIST_ENTRY_SPACING
                        )),
                new Tab(TabKey.PREFIX.key, (screen) ->
                        new PrefixList(Minecraft.getInstance(), 0, 0, 0, 0,
                                BASE_LIST_ENTRY_WIDTH, LIST_ENTRY_HEIGHT, LIST_ENTRY_SPACING
                        ))
        );
        super.setTabs(tabs, defaultKey);
    }

    public enum TabKey {
        NOTIFICATION(translationKey("option", "notif")),
        CONTROL(translationKey("option", "control")),
        DEFAULT(translationKey("option", "default")),
        PREFIX(translationKey("option", "prefix"));

        public final String key;
        TabKey(String key) {
            this.key = key;
        }
    }

    @Override
    protected void addFooter() {
        int spacing = 4;
        int w = BASE_LIST_ENTRY_WIDTH / 2 - spacing;
        int h = LIST_ENTRY_HEIGHT;
        int x1 = width / 2 - w - spacing / 2;
        int x2 = width / 2 + spacing / 2;
        int y = Math.min(
                height - h, // Bottom of screen
                height - FOOTER_MARGIN / 2 - h / 2 // Center of margin
        );

        // Cancel button
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,
                        (button) -> Minecraft.getInstance().setScreen(new ConfirmScreen(
                                (confirm) -> {
                                    if (confirm) {
                                        Config.reload();
                                        Minecraft.getInstance().setScreen(this);
                                        onClose();
                                    } else {
                                        Minecraft.getInstance().setScreen(this);
                                    }
                                },
                                localized("option", "root.exit_without_saving"),
                                localized("option", "root.exit_without_saving.confirm"))))
                .pos(x1, y)
                .size(w, h)
                .build());

        // Done button
        addRenderableWidget(Button.builder(
                        CommonComponents.GUI_DONE,
                        (button) -> onClose())
                .pos(x2, y)
                .size(w, h)
                .build());
    }

    @Override
    public void onClose() {
        super.onClose();
        Config.save();
    }
}
