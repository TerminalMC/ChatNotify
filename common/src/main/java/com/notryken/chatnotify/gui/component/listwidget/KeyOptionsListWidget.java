/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.component.listwidget;

import com.notryken.chatnotify.config.Trigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Contains a text field for a {@link Trigger}, and a list of potential
 * translation key triggers.
 */
public class KeyOptionsListWidget extends OptionsListWidget {
    private final Trigger trigger;

    public static final String[][] chatKeys = {
            {".", "Any Message"},
            {"chat.type", "Any Chat Message"},
            {"chat.type.text", "Player Chat Message"},
            {"chat.type.announcement", "Server Chat Message"},
            {"chat.type.admin", "Operator Info Chat Message"},
    };
    public static final String[][] playerKeys = {
            {"multiplayer.player.joined", "Player Joined"},
            {"multiplayer.player.left", "Player Left"},
            {"death.", "Player/Pet Died"},
    };
    public static final String[][] advancementKeys = {
            {"chat.type.advancement", "Any Advancement"},
            {"chat.type.advancement.task", "Task Advancement"},
            {"chat.type.advancement.goal", "Goal Advancement"},
            {"chat.type.advancement.challenge", "Challenge Advancement"},
    };
    public static final String[][] commandKeys = {
            {"commands.", "Any Command Feedback"},
            {"commands.message.display", "Any Private Message"},
            {"commands.message.display.incoming", "Incoming Private Message"},
            {"commands.message.display.outgoing", "Outgoing Private Message"},
    };

    public KeyOptionsListWidget(Minecraft mc, int width, int height, int top, int bottom,
                                int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                                int scrollWidth, Trigger trigger) {
        super(mc, width, height, top, bottom, itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth);
        this.trigger = trigger;

        addEntry(new Entry.TriggerTypeEntry(entryX, entryWidth, entryHeight, this, trigger));

        addEntry(new Entry.TriggerFieldEntry(entryX, entryWidth, entryHeight, trigger));

        if (trigger.isKey()) {
            addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("Key Options \u2139"), Tooltip.create(Component.literal(
                    "You can find a full list of translation keys in Minecraft's language .json files, " +
                            "path assets/minecraft/lang/ in the Minecraft jar")), 500));

            for (String[] s : chatKeys) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this,
                        trigger, s[0], s[1]));
            }

            addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("--------------------"), null, -1));

            for (String[] s : playerKeys) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this,
                        trigger, s[0], s[1]));
            }

            addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("--------------------"), null, -1));

            for (String[] s : advancementKeys) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this,
                        trigger, s[0], s[1]));
            }

            addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("--------------------"), null, -1));

            for (String[] s : commandKeys) {
                addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this,
                        trigger, s[0], s[1]));
            }
        }
    }

    @Override
    public OptionsListWidget resize(int width, int height, int top, int bottom,
                                    int itemHeight, double scrollAmount) {
        KeyOptionsListWidget newListWidget = new KeyOptionsListWidget(
                minecraft, width, height, top, bottom, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth, trigger);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private abstract static class Entry extends OptionsListWidget.Entry {

        private static class TriggerTypeEntry extends Entry {
            TriggerTypeEntry(int x, int width, int height, KeyOptionsListWidget listWidget, Trigger trigger) {
                super();
                CycleButton<Boolean> triggerTypeButton = CycleButton.booleanBuilder(
                                Component.literal("Translation Key \u2139"), Component.literal("Normal \u2139"))
                        .withInitialValue(trigger.isKey())
                        .withTooltip((value) -> {
                            if (value) {
                                return Tooltip.create(Component.literal("This type of trigger " +
                                        "will activate the notification if an incoming message is " +
                                        "translatable and the translation key contains the trigger. " +
                                        "May not work on some servers."));
                            }
                            else {
                                return Tooltip.create(Component.literal("This type of trigger " +
                                        "will activate the notification if an incoming message " +
                                        "contains the trigger by itself (not as part of a word). " +
                                        "(Not case-sensitive, allows for symbols including punctuation)."));
                            }
                        })
                        .create(x, 0, width, height, Component.literal("Trigger Type"),
                                (button, status) -> {
                                    trigger.setIsKey(status);
                                    listWidget.reload();
                                });
                triggerTypeButton.setTooltipDelay(500);
                elements.add(triggerTypeButton);
            }
        }

        private static class TriggerFieldEntry extends KeyOptionsListWidget.Entry {
            TriggerFieldEntry(int x, int width, int height, Trigger trigger) {
                super();

                EditBox triggerEditBox = new EditBox(Minecraft.getInstance().font, x, 0, width, height,
                        Component.literal("Notification Trigger"));
                triggerEditBox.setMaxLength(120);
                triggerEditBox.setValue(trigger.string);
                triggerEditBox.setResponder((string) -> trigger.string = string.strip());
                elements.add(triggerEditBox);
            }
        }

        private static class KeyOption extends KeyOptionsListWidget.Entry {
            KeyOption(int x, int width, int height, KeyOptionsListWidget listWidget,
                      Trigger trigger, String value, String label) {
                super();
                elements.add(Button.builder(Component.literal(label),
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
