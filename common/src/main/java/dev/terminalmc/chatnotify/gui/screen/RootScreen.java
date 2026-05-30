/*
 * Copyright 2026 TerminalMC
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
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import dev.terminalmc.chatnotify.gui.widget.list.root.ControlList;
import dev.terminalmc.chatnotify.gui.widget.list.root.DefaultList;
import dev.terminalmc.chatnotify.gui.widget.list.root.DetectionList;
import dev.terminalmc.chatnotify.util.Unicode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

import static dev.terminalmc.chatnotify.util.Localization.localized;
import static dev.terminalmc.chatnotify.util.Localization.translationKey;

/**
 * Supports a series of {@link dev.terminalmc.chatnotify.gui.widget.list.OptionList}s for mod
 * configuration, and one to display the list of
 * {@link dev.terminalmc.chatnotify.config.Notification}s with widgets for superficial
 * configuration.
 * <p>
 * Note: Configuration is saved only when this screen is closed.
 */
public class RootScreen extends OptionScreen {

    public enum TabKey {
        NOTIFICATION(translationKey("option", "notif")),
        CONTROL(translationKey("option", "control")),
        DEFAULT(translationKey("option", "default")),
        DETECTION(translationKey("option", "detection"));

        public final String key;

        TabKey(String key) {
            this.key = key;
        }
    }

    public RootScreen(Screen lastScreen) {
        this(lastScreen, TabKey.NOTIFICATION.key);
    }

    public RootScreen(Screen lastScreen, String defaultKey) {
        super(lastScreen);
        addTabs(defaultKey);
        updateTabTitles();
    }

    private void addTabs(String defaultKey) {
        List<Tab> tabs = List.of(
                new Tab(TabKey.NOTIFICATION.key, this::getNotificationList),
                new Tab(TabKey.CONTROL.key, RootScreen::getControlList),
                new Tab(TabKey.DEFAULT.key, RootScreen::getDefaultList),
                new Tab(TabKey.DETECTION.key, RootScreen::getDetectionList)
        );
        super.setTabs(tabs, defaultKey);
    }

    public void updateTabTitles() {
        for (TabKey tabKey : TabKey.values()) {
            updateTabTitle(tabKey);
        }
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private void updateTabTitle(TabKey tabKey) {
        MutableComponent title = Component.translatable(tabKey.key);
        switch (tabKey) {
            case NOTIFICATION -> {
                if (!Config.get().getNotifs().isEmpty()) {
                    title.append(" ");
                    title.append(localized("common", "count", Config.get().getNotifs().size()));
                }
            }
        }
        super.updateTabTitle(tabKey.key, title);
    }

    @Override
    protected void addFooter() {
        int spacing = 4;
        int buttonWidth = BASE_LIST_ENTRY_WIDTH / 2 - spacing;
        int buttonHeight = LIST_ENTRY_HEIGHT;
        int x1 = width / 2 - buttonWidth - spacing / 2;
        int x2 = width / 2 + spacing / 2;
        int y = Math.min(
                height - buttonHeight, // Bottom of screen
                height - FOOTER_MARGIN / 2 - buttonHeight / 2 // Center of margin
        );

        // Cancel button
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> onCancel())
                .pos(x1, y)
                .size(buttonWidth, buttonHeight)
                .build());

        // Done button
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> onClose())
                .pos(x2, y)
                .size(buttonWidth, buttonHeight)
                .build());
    }

    private void onCancel() {
        Minecraft.getInstance().gui.setScreen(new ConfirmScreen(
                (confirm) -> {
                    if (confirm) {
                        Config.reload();
                        Minecraft.getInstance().gui.setScreen(this);
                        onClose();
                    } else {
                        Minecraft.getInstance().gui.setScreen(this);
                    }
                },
                localized("option", "root.exit_without_saving"),
                localized("option", "root.exit_without_saving.confirm")
        ));
    }

    @Override
    public void onClose() {
        super.onClose();
        Config.save();
    }

    private OptionList getNotificationList(OptionScreen screen) {
        return new FilterList<>(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                FilterList.Entry.NotifOptions.class,
                (srcIdx, dstIdx) -> Config.get().moveNotif(++srcIdx, ++dstIdx),
                () -> updateTabTitle(TabKey.NOTIFICATION),
                localized("option", "notif.list", Unicode.INFO.str),
                localized("option", "notif.list.tooltip"),
                null,
                null,
                () -> Config.get().getNotifs(),
                (x, width, height, list, notif, index) -> index == 0
                        ? new FilterList.Entry.NotifOptions.Locked(x, width, height, list, notif)
                        : new FilterList.Entry.NotifOptions(x, width, height, list, notif, index),
                null,
                () -> Config.get().addNotif()
        );
    }

    private static OptionList getControlList(OptionScreen screen) {
        return new ControlList(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING
        );
    }

    private static OptionList getDetectionList(OptionScreen screen) {
        return new DetectionList(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING
        );
    }

    private static OptionList getDefaultList(OptionScreen screen) {
        return new DefaultList(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING
        );
    }
}
