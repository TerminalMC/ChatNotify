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
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.Response;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.FilterList;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import dev.terminalmc.chatnotify.gui.widget.list.root.notif.FormatList;
import dev.terminalmc.chatnotify.gui.widget.list.root.notif.MiscOptionList;
import dev.terminalmc.chatnotify.gui.widget.list.root.notif.SoundList;
import dev.terminalmc.chatnotify.util.Unicode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

import static dev.terminalmc.chatnotify.util.Localization.localized;
import static dev.terminalmc.chatnotify.util.Localization.translationKey;

/**
 * Supports a series of {@link OptionList}s for configuration of a {@link Notification}.
 * <p>
 * Note: {@link Notification#editing} should be set to {@link true}
 * when opening this screen, and will be set to {@link false} when it is closed.
 */
public class NotifScreen extends OptionScreen {

    public enum TabKey {
        TRIGGERS(translationKey("option", "notif.trigger")),
        FORMAT(translationKey("option", "notif.format")),
        SOUND(translationKey("option", "notif.sound")),
        INCLUSION(translationKey("option", "notif.inclusion")),
        EXCLUSION(translationKey("option", "notif.exclusion")),
        RESPONSES(translationKey("option", "notif.response")),
        MISC(translationKey("option", "notif.misc"));

        public final String key;

        TabKey(String key) {
            this.key = key;
        }
    }

    private final Notification notif;

    public NotifScreen(Screen lastScreen, Notification notif) {
        this(lastScreen, notif, TabKey.TRIGGERS.key);
    }

    public NotifScreen(Screen lastScreen, Notification notif, String defaultKey) {
        super(lastScreen);
        this.notif = notif;
        notif.editing = true;
        addTabs(defaultKey);
        updateTabTitles();
    }

    private void addTabs(String defaultKey) {
        List<Tab> tabs = List.of(
                new Tab(TabKey.TRIGGERS.key, NotifScreen::getTriggerList),
                new Tab(TabKey.FORMAT.key, NotifScreen::getFormatList),
                new Tab(TabKey.SOUND.key, NotifScreen::getSoundList),
                new Tab(TabKey.INCLUSION.key, NotifScreen::getInclusionList),
                new Tab(TabKey.EXCLUSION.key, NotifScreen::getExclusionList),
                new Tab(TabKey.RESPONSES.key, NotifScreen::getResponsesList),
                new Tab(TabKey.MISC.key, NotifScreen::getMiscList)
        );
        super.setTabs(tabs, defaultKey);
    }

    public void updateTabTitles() {
        for (TabKey tabKey : TabKey.values()) {
            updateTabTitle(tabKey);
        }
    }

    private void updateTabTitle(TabKey tabKey) {
        MutableComponent title = Component.translatable(tabKey.key);
        switch (tabKey) {
            case TRIGGERS -> {
                if (!notif.triggers.isEmpty()) {
                    title.append(" ");
                    title.append(localized("common", "count", notif.triggers.size()));
                }
            }
            case INCLUSION -> {
                if (notif.inclusionEnabled && !notif.inclusionTriggers.isEmpty()) {
                    title.append(" ");
                    title.append(localized("common", "count", notif.inclusionTriggers.size()));
                }
            }
            case EXCLUSION -> {
                if (notif.exclusionEnabled && !notif.exclusionTriggers.isEmpty()) {
                    title.append(" ");
                    title.append(localized("common", "count", notif.exclusionTriggers.size()));
                }
            }
            case RESPONSES -> {
                if (notif.responseEnabled && !notif.responses.isEmpty()) {
                    title.append(" ");
                    title.append(localized("common", "count", notif.responses.size()));
                }
            }
        }
        super.updateTabTitle(tabKey.key, title);
    }

    @Override
    public void onClose() {
        notif.editing = false;
        super.onClose();
    }

    private static OptionList getTriggerList(OptionScreen screen) {
        Notification notif = cast(screen).notif;
        return new FilterList<>(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                FilterList.Entry.TriggerOptions.class,
                (srcIdx, dstIdx) -> notif == Config.get().getUserNotif()
                        ? notif.moveTrigger(srcIdx + 2, dstIdx + 2)
                        : notif.moveTrigger(srcIdx, dstIdx),
                () -> cast(screen).updateTabTitle(TabKey.TRIGGERS),
                localized("option", "notif.trigger.list", Unicode.INFO.str),
                localized("option", "notif.trigger.list.tooltip"),
                null,
                null,
                () -> notif.triggers,
                (x, width, height, list, trigger, index) -> {
                    if (notif.equals(Config.get().getUserNotif()) && index <= 1) {
                        return new FilterList.Entry.LockedTriggerOptions(
                                x,
                                width,
                                height,
                                trigger,
                                localized(
                                        "option",
                                        index == 0
                                                ? "notif.trigger.list.special.profile_name"
                                                : "notif.trigger.list.special.display_name"
                                )
                        );
                    } else {
                        return new FilterList.Entry.TriggerOptions(
                                x,
                                width,
                                height,
                                list,
                                trigger,
                                notif.textStyle,
                                index,
                                (i) -> notif.triggers.remove((int) i),
                                new TextField.Validator.UniqueTrigger(notif, trigger),
                                true
                        );
                    }
                },
                (x, width, height, list, trigger) -> trigger.styleTarget.enabled
                        ? new FilterList.Entry.StyleTargetOptions(
                        x,
                        width,
                        height,
                        list,
                        trigger.styleTarget
                )
                        : null,
                () -> notif.triggers.add(new Trigger())
        );
    }

    private static OptionList getFormatList(OptionScreen screen) {
        return new FormatList(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                cast(screen).notif
        );
    }

    private static OptionList getSoundList(OptionScreen screen) {
        return new SoundList(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                cast(screen).notif.sound
        );
    }

    private static OptionList getInclusionList(OptionScreen screen) {
        Notification notif = cast(screen).notif;
        return new FilterList<>(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                FilterList.Entry.TriggerOptions.class,
                notif::moveInclusionTrigger,
                () -> cast(screen).updateTabTitle(TabKey.INCLUSION),
                localized("option", "notif.inclusion.list", Unicode.INFO.str),
                localized("option", "notif.inclusion.list.tooltip"),
                () -> notif.inclusionEnabled,
                (status) -> notif.inclusionEnabled = status,
                () -> notif.inclusionTriggers,
                (x, width, height, list, trigger, index) -> new FilterList.Entry.TriggerOptions(
                        x,
                        width,
                        height,
                        list,
                        trigger,
                        notif.textStyle,
                        index,
                        (i) -> notif.inclusionTriggers.remove((int) i),
                        TextField.Validator.UniqueTrigger.inclusion(notif, trigger),
                        false
                ),
                null,
                () -> notif.inclusionTriggers.add(new Trigger())
        );
    }

    private static OptionList getExclusionList(OptionScreen screen) {
        Notification notif = cast(screen).notif;
        return new FilterList<>(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                FilterList.Entry.TriggerOptions.class,
                notif::moveExclusionTrigger,
                () -> cast(screen).updateTabTitle(TabKey.EXCLUSION),
                localized("option", "notif.exclusion.list", Unicode.INFO.str),
                localized("option", "notif.exclusion.list.tooltip"),
                () -> notif.exclusionEnabled,
                (status) -> notif.exclusionEnabled = status,
                () -> notif.exclusionTriggers,
                (x, width, height, list, trigger, index) -> new FilterList.Entry.TriggerOptions(
                        x,
                        width,
                        height,
                        list,
                        trigger,
                        notif.textStyle,
                        index,
                        (i) -> notif.exclusionTriggers.remove((int) i),
                        TextField.Validator.UniqueTrigger.exclusion(notif, trigger),
                        false
                ),
                null,
                () -> notif.exclusionTriggers.add(new Trigger())
        );
    }

    private static OptionList getResponsesList(OptionScreen screen) {
        Notification notif = cast(screen).notif;
        return new FilterList<>(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                FilterList.Entry.ResponseOptions.class,
                notif::moveResponseMessage,
                () -> cast(screen).updateTabTitle(TabKey.RESPONSES),
                localized("option", "notif.response.list", Unicode.INFO.str),
                localized("option", "notif.response.list.tooltip"),
                () -> notif.responseEnabled,
                (status) -> notif.responseEnabled = status,
                () -> notif.responses,
                (x, width, height, list, trigger, index) -> new FilterList.Entry.ResponseOptions(
                        x,
                        width,
                        height,
                        list,
                        trigger,
                        index,
                        (i) -> notif.responses.remove((int) i)
                ),
                null,
                () -> notif.responses.add(new Response())
        );
    }

    private static OptionList getMiscList(OptionScreen screen) {
        return new MiscOptionList(
                Minecraft.getInstance(),
                screen,
                0,
                0,
                0,
                BASE_LIST_ENTRY_WIDTH,
                LIST_ENTRY_HEIGHT,
                LIST_ENTRY_SPACING,
                cast(screen).notif
        );
    }

    private static NotifScreen cast(OptionScreen screen) {
        if (!(screen instanceof NotifScreen s))
            throw new IllegalArgumentException(String.format(
                    "Option list supplier for class %s cannot use screen type %s",
                    NotifScreen.class.getName(),
                    screen.getClass().getName()
            ));
        return s;
    }
}
