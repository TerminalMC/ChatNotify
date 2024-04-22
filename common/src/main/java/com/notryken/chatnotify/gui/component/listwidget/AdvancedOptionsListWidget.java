/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.component.listwidget;

import com.notryken.chatnotify.config.Config;
import com.notryken.chatnotify.config.Notification;
import com.notryken.chatnotify.config.ResponseMessage;
import com.notryken.chatnotify.config.Trigger;
import com.notryken.chatnotify.gui.screen.OptionsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Contains controls for advanced options of a {@link Notification}, including
 * exclusion triggers, response messages, and reset options.
 */
public class AdvancedOptionsListWidget extends OptionsListWidget {
    private final Notification notif;

    public AdvancedOptionsListWidget(Minecraft mc, int width, int height, int top, int bottom,
                                     int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                                     int scrollWidth, Notification notif) {
        super(mc, width, height, top, bottom, itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth);
        this.notif = notif;

        addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Exclusion Triggers \u2139"),
                Tooltip.create(Component.literal("If an exclusion trigger is detected in a " +
                        "message, it will prevent this notification from activating.")), -1));
        addEntry(new Entry.ExclusionToggleButton(entryX, entryWidth, entryHeight, notif, this));

        if (notif.exclusionEnabled) {
            for (int i = 0; i < this.notif.exclusionTriggers.size(); i ++) {
                addEntry(new Entry.ExclusionTriggerField(entryX, entryWidth, entryHeight,
                        this, notif, notif.exclusionTriggers.get(i), i));
            }
            addEntry(new OptionsListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.exclusionTriggers.add(new Trigger());
                        reload();
                    }));
        }

        addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Auto Response Messages \u2139"),
                Tooltip.create(Component.literal("Chat messages or commands to be sent when " +
                        "this notification is activated.\n").append(Component.literal(
                                "Warning: Can crash the game, use with caution.")
                        .withStyle(ChatFormatting.RED))), -1));
        addEntry(new Entry.ResponseToggleButton(entryX, entryWidth, entryHeight, notif, this));

        if (notif.responseEnabled) {
            for (int i = 0; i < notif.responseMessages.size(); i ++) {
                addEntry(new Entry.ResponseMessageField(entryX, entryWidth, entryHeight, this, notif, i));
            }
            addEntry(new OptionsListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.responseMessages.add(new ResponseMessage());
                        reload();
                    }));
        }

        addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Broken Everything?"), null, -1));

        addEntry(new OptionsListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Reset Advanced Options"), Tooltip.create(
                        Component.literal("Resets all advanced settings for THIS notification.")),
                -1,
                (button) -> {
                    notif.resetAdvanced();
                    reload();
                }));

        addEntry(new OptionsListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Reset All Advanced Options"), Tooltip.create(
                Component.literal("Resets all advanced settings for ALL notifications.")),
                -1,
                (button) -> mc.setScreen(new ConfirmScreen(
                        (value) -> {
                            if (value) {
                                for (Notification notif2 : Config.get().getNotifs()) {
                                    notif2.resetAdvanced();
                                }
                                mc.setScreen(null);
                            }
                            else {
                                reload();
                            }},
                        Component.literal("Advanced Options Reset"),
                        Component.literal("Are you sure you want to reset all advanced settings " +
                                "for ALL notifications?")))));

        addEntry(new OptionsListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Nuclear Reset"), Tooltip.create(
                Component.literal("Deletes all notifications and resets all settings.")),
                -1,
                (button) -> mc.setScreen(new ConfirmScreen(
                        (value) -> {
                            if (value) {
                                Config.resetAndSave();
                                mc.setScreen(null);
                            }
                            else {
                                reload();
                            }},
                        Component.literal("Nuclear Reset"),
                        Component.literal("Are you sure you want to delete all " +
                                "notifications and reset all settings?")))));
    }

    @Override
    public AdvancedOptionsListWidget resize(int width, int height, int top, int bottom,
                                            int itemHeight, double scrollAmount) {
        AdvancedOptionsListWidget newListWidget = new AdvancedOptionsListWidget(
                minecraft, width, height, top, bottom, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth, notif);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private void openKeyConfig(Trigger trigger) {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.key"),
                new KeyOptionsListWidget(minecraft, screen.width, screen.height, y0, y1,
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, trigger)));
    }

    private abstract static class Entry extends OptionsListWidget.Entry {

        private static class ExclusionToggleButton extends Entry {
            ExclusionToggleButton(int x, int width, int height, Notification notif,
                              AdvancedOptionsListWidget listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.exclusionEnabled)
                        .create(x, 0, width, height, Component.literal("Exclusion Triggers"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ExclusionTriggerField extends Entry {

            ExclusionTriggerField(int x, int width, int height, AdvancedOptionsListWidget listWidget,
                                  Notification notif, Trigger trigger, int index) {
                super();

                int spacing = 5;
                int regexButtonWidth = 15;
                int keyButtonWidth = Config.get().allowRegex ? 15 : 20;
                int removeButtonWidth = 20;

                EditBox triggerEditBox = new EditBox(Minecraft.getInstance().font,
                        x, 0, width, height, Component.literal("Notification Trigger"));
                triggerEditBox.setMaxLength(120);
                triggerEditBox.setValue(trigger.string);
                triggerEditBox.setResponder((string) -> trigger.string = string.strip());

                Button keyButton;
                if (Config.get().allowRegex) {
                    Button regexButton;
                    if (trigger.isKey()) {
                        regexButton = Button.builder(Component.literal(".*")
                                                .withStyle(ChatFormatting.GRAY),
                                        (button) -> {})
                                .pos(x - spacing - regexButtonWidth - regexButtonWidth, 0)
                                .size(regexButtonWidth, height)
                                .build();
                        regexButton.setTooltip(Tooltip.create(Component.literal(
                                "Regex Disabled [Key trigger]")));
                        regexButton.setTooltipDelay(500);
                    }
                    else if (trigger.isRegex) {
                        regexButton = Button.builder(Component.literal(".*")
                                                .withStyle(ChatFormatting.GREEN),
                                        (button) -> {
                                            trigger.isRegex = false;
                                            listWidget.reload();
                                        })
                                .pos(x - spacing - regexButtonWidth - regexButtonWidth, 0)
                                .size(regexButtonWidth, height)
                                .build();
                        regexButton.setTooltip(Tooltip.create(Component.literal(
                                "Regex Enabled")));
                        regexButton.setTooltipDelay(500);
                    }
                    else {
                        regexButton = Button.builder(Component.literal(".*")
                                                .withStyle(ChatFormatting.RED),
                                        (button) -> {
                                            trigger.isRegex = true;
                                            listWidget.reload();
                                        })
                                .pos(x - spacing - regexButtonWidth - regexButtonWidth, 0)
                                .size(regexButtonWidth, height)
                                .build();
                        regexButton.setTooltip(Tooltip.create(Component.literal(
                                "Regex Disabled")));
                        regexButton.setTooltipDelay(500);
                    }
                    elements.add(regexButton);
                }
                if (trigger.isKey()) {
                    keyButton = Button.builder(Component.literal("\uD83D\uDD11")
                                            .withStyle(ChatFormatting.GREEN),
                                    (button) -> {
                                        if (Screen.hasShiftDown()) {
                                            trigger.setIsKey(false);
                                            listWidget.reload();
                                        } else {
                                            listWidget.openKeyConfig(trigger);
                                        }})
                            .pos(x - spacing - keyButtonWidth, 0)
                            .size(keyButtonWidth, height)
                            .build();
                    keyButton.setTooltip(Tooltip.create(Component.literal(
                            "Translation key trigger")));
                    keyButton.setTooltipDelay(500);
                }
                else {
                    keyButton = Button.builder(Component.literal("\uD83D\uDD11")
                                            .withStyle(ChatFormatting.RED),
                                    (button) -> {
                                        if (Screen.hasShiftDown()) {
                                            trigger.setIsKey(true);
                                            listWidget.reload();
                                        } else {
                                            listWidget.openKeyConfig(trigger);
                                        }})
                            .pos(x - spacing - keyButtonWidth, 0)
                            .size(keyButtonWidth, height)
                            .build();
                    keyButton.setTooltip(Tooltip.create(Component.literal(
                            "Normal trigger")));
                    keyButton.setTooltipDelay(500);
                }
                elements.add(keyButton);
                elements.add(triggerEditBox);
                elements.add(Button.builder(Component.literal("\u274C"),
                                (button) -> {
                                    notif.exclusionTriggers.remove(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + spacing, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }

        private static class ResponseToggleButton extends Entry {
            ResponseToggleButton(int x, int width, int height, Notification notif,
                                 AdvancedOptionsListWidget listWidget)
            {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.responseEnabled)
                        .create(x, 0, width, height, Component.literal("Response Messages"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ResponseMessageField extends Entry {

            ResponseMessageField(int x, int width, int height, AdvancedOptionsListWidget listWidget,
                                 Notification notif, int index) {
                super();

                int spacing = 5;
                int moveButtonWidth = 12;
                int regexButtonWidth = 15;
                int removeButtonWidth = 20;
                int timeFieldWidth = 40;
                int msgFieldWidth = width - timeFieldWidth - spacing;
                ResponseMessage resMsg = notif.responseMessages.get(index);

                EditBox messageEditBox = new EditBox(Minecraft.getInstance().font,
                        x, 0, msgFieldWidth, height, Component.literal("Response Message"));
                messageEditBox.setMaxLength(256);
                messageEditBox.setValue(resMsg.string);
                messageEditBox.setResponder((val) -> resMsg.string = val.strip());
                elements.add(messageEditBox);

                EditBox timeEditBox = new EditBox(Minecraft.getInstance().font,
                        x + width - timeFieldWidth, 0, timeFieldWidth, height,
                        Component.literal("Delay Ticks"));
                timeEditBox.setTooltip(Tooltip.create(Component.literal(
                        "Time in ticks to wait before sending.")));
                timeEditBox.setTooltipDelay(500);
                timeEditBox.setMaxLength(5);
                timeEditBox.setValue(String.valueOf(resMsg.delayTicks));
                timeEditBox.setResponder((val) -> {
                    try {
                        resMsg.delayTicks = Integer.parseInt(val.strip());
                        timeEditBox.setTextColor(16777215);
                    }
                    catch (NumberFormatException ignored) {
                        timeEditBox.setTextColor(16711680);
                    }
                });
                elements.add(timeEditBox);

                if (Config.get().allowRegex) {
                    if (Config.get().allowRegex) {
                        CycleButton<Boolean> regexButton = CycleButton.booleanBuilder(
                                        Component.literal(".*").withStyle(ChatFormatting.GREEN),
                                        Component.literal(".*").withStyle(ChatFormatting.RED))
                                .displayOnlyValue()
                                .withInitialValue(resMsg.regexGroups)
                                .withTooltip((status) -> Tooltip.create(Component.literal(
                                        status ? "Regex Groups Enabled.\nUse (1), (2) etc in the response message " +
                                                "to access regex groups from the trigger." : "Regex Groups Disabled")))
                                .create(x - spacing - regexButtonWidth, 0, regexButtonWidth, height, Component.empty(),
                                        (button, status) -> resMsg.regexGroups = status);
                        regexButton.setTooltipDelay(500);
                        elements.add(regexButton);
                    }
                }

                Button upButton = Button.builder(Component.literal("\u2191"),
                                (button) -> {
                                    if (Screen.hasShiftDown()) {
                                        if (index > 0) {
                                            notif.responseMessages.add(0, notif.responseMessages.get(index));
                                            notif.responseMessages.remove(index + 1);
                                            listWidget.reload();
                                        }
                                    } else {
                                        if (index > 0) {
                                            ResponseMessage temp = notif.responseMessages.get(index);
                                            notif.responseMessages.set(index, notif.responseMessages.get(index - 1));
                                            notif.responseMessages.set(index - 1, temp);
                                            listWidget.reload();
                                        }
                                    }})
                        .pos(x - 2 * moveButtonWidth - spacing
                                - (Config.get().allowRegex ? spacing + regexButtonWidth : 0), 0)
                        .size(moveButtonWidth, height)
                        .build();
                if (index == 0) upButton.active = false;
                elements.add(upButton);

                Button downButton = Button.builder(Component.literal("\u2193"),
                                (button) -> {
                                    if (Screen.hasShiftDown()) {
                                        if (index < notif.responseMessages.size() - 1) {
                                            notif.responseMessages.add(notif.responseMessages.get(index));
                                            notif.responseMessages.remove(index);
                                            listWidget.reload();
                                        }
                                    } else {
                                        if (index < notif.responseMessages.size() - 1) {
                                            ResponseMessage temp = notif.responseMessages.get(index);
                                            notif.responseMessages.set(index, notif.responseMessages.get(index + 1));
                                            notif.responseMessages.set(index + 1, temp);
                                            listWidget.reload();
                                        }
                                    }})
                        .pos(x - moveButtonWidth - spacing
                                - (Config.get().allowRegex ? spacing + regexButtonWidth : 0), 0)
                        .size(moveButtonWidth, height)
                        .build();
                if (index == notif.responseMessages.size() - 1) downButton.active = false;
                elements.add(downButton);

                elements.add(Button.builder(Component.literal("\u274C"),
                                (button) -> {
                                    notif.responseMessages.remove(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + spacing, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }
    }
}