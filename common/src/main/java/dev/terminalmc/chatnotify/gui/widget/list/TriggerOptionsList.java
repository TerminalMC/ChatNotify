/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import dev.terminalmc.chatnotify.config.Trigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains a text field for a {@link Trigger}, and a series of buttons to
 * quick-fill translation keys.
 */
public class TriggerOptionsList extends OptionsList {
    public static final String[] CHAT_KEYS = {
            ".",
            "chat.type",
            "chat.type.text",
            "chat.type.announcement",
            "chat.type.admin",
    };
    public static final String[] PLAYER_KEYS = {
            "multiplayer.player.joined",
            "multiplayer.player.left",
            "death.",
    };
    public static final String[] ADVANCEMENT_KEYS = {
            "chat.type.advancement",
            "chat.type.advancement.task",
            "chat.type.advancement.goal",
            "chat.type.advancement.challenge",
    };
    public static final String[] COMMAND_KEYS = {
            "commands.",
            "commands.message.display",
            "commands.message.display.incoming",
            "commands.message.display.outgoing",
    };

    private final Trigger trigger;

    public TriggerOptionsList(Minecraft mc, int width, int height, int y, int rowWidth,
                              int itemHeight, int entryWidth, int entryHeight, Trigger trigger) {
        super(mc, width, height, y, rowWidth, itemHeight, entryWidth, entryHeight);
        this.trigger = trigger;

        addEntry(new Entry.TriggerTypeEntry(entryX, entryWidth, entryHeight, this, trigger));

        addEntry(new Entry.TriggerFieldEntry(entryX, entryWidth, entryHeight, trigger));

        if (trigger.isKey) {
            addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    localized("option", "trigger.keys", "\u2139"),
                    Tooltip.create(localized("option", "trigger.keys.tooltip")), 500));

            for (String s : CHAT_KEYS) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
            }

            addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("--------------------"), null, -1));

            for (String s : PLAYER_KEYS) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
            }

            addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("--------------------"), null, -1));

            for (String s : ADVANCEMENT_KEYS) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
            }

            addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("--------------------"), null, -1));

            for (String s : COMMAND_KEYS) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
            }
        }
    }

    @Override
    public OptionsList reload(int width, int height, double scrollAmount) {
        TriggerOptionsList newListWidget = new TriggerOptionsList(minecraft, width, height,
                getY(), getRowWidth(), itemHeight, entryWidth, entryHeight, trigger);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class TriggerTypeEntry extends Entry {
            TriggerTypeEntry(int x, int width, int height, TriggerOptionsList listWidget, Trigger trigger) {
                super();
                CycleButton<Boolean> triggerTypeButton = CycleButton.booleanBuilder(
                        localized("option", "trigger.type.key", "\u2139"),
                                localized("option", "trigger.type.normal", "\u2139"))
                        .withInitialValue(trigger.isKey)
                        .withTooltip((value) -> value
                                ? Tooltip.create(localized("option", "trigger.type.key.tooltip"))
                                : Tooltip.create(localized("option", "trigger.type.normal.tooltip")))
                        .create(x, 0, width, height, localized("option", "trigger.type"),
                                (button, status) -> {
                                    trigger.isKey = status;
                                    listWidget.reload();
                                });
                triggerTypeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerTypeButton);
            }
        }

        private static class TriggerFieldEntry extends TriggerOptionsList.Entry {
            TriggerFieldEntry(int x, int width, int height, Trigger trigger) {
                super();
                EditBox triggerField = new EditBox(Minecraft.getInstance().font, x, 0,
                        width, height, Component.empty());
                triggerField.setMaxLength(120);
                triggerField.setValue(trigger.string);
                triggerField.setResponder((string) -> trigger.string = string.strip());
                elements.add(triggerField);
            }
        }

        private static class KeyOption extends TriggerOptionsList.Entry {
            KeyOption(int x, int width, int height, TriggerOptionsList listWidget,
                      Trigger trigger, String value) {
                super();
                elements.add(Button.builder(localized("option", "trigger.key." + value),
                        (button) -> {
                            trigger.string = value;
                            listWidget.reload();
                        })
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }
    }
}
