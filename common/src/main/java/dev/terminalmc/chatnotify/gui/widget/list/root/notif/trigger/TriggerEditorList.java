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

package dev.terminalmc.chatnotify.gui.widget.list.root.notif.trigger;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.StyleTarget;
import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.MultiLineTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import dev.terminalmc.chatnotify.util.Unicode;
import dev.terminalmc.chatnotify.util.text.FormatUtil;
import dev.terminalmc.chatnotify.util.text.MessageUtil;
import dev.terminalmc.chatnotify.util.text.StyleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class TriggerEditorList extends OptionList {

    private final Trigger trigger;
    private final TextStyle textStyle;
    private final List<JsonElement> recentChatMaster;
    private final List<Component> recentChat;
    private boolean filter;
    private boolean restyle;
    private MultiLineTextField textDisplayField;
    private String displayText = "";
    private MultiLineTextField keyDisplayField;
    private String displayKeys = "";

    public TriggerEditorList(
            Minecraft mc,
            OptionScreen screen,
            int width,
            int height,
            int y,
            int entryWidth,
            int entryHeight,
            int entrySpacing,
            Trigger trigger,
            TextStyle textStyle
    ) {
        super(mc, screen, width, height, y, entryWidth, entryHeight, entrySpacing);
        this.trigger = trigger;
        this.textStyle = textStyle;
        this.recentChatMaster = ChatNotify.unmodifiedChat.stream()
                .map(text -> ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text))
                .filter(dataResult -> dataResult.isSuccess() && dataResult.result().isPresent())
                .map(dataResult -> dataResult.result().get())
                .toList()
                .reversed();
        this.recentChat = new ArrayList<>();
    }

    @Override
    protected void init() {
        this.recentChat.clear();
        this.recentChat.addAll(this.recentChatMaster.stream()
                .map((json) -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json))
                .filter(dataResult -> dataResult.isSuccess() && dataResult.result().isPresent())
                .map(dataResult -> dataResult.result().get())
                .toList());
        super.init();
    }

    @Override
    protected void addEntries() {
        // Trigger editor
        addSpacedEntry(new Entry.TriggerOptions(
                dynWideEntryX,
                dynWideEntryWidth,
                entryHeight + defaultEntryHeight,
                this,
                trigger
        ));
        if (trigger.styleTarget.enabled) {
            addEntry(new Entry.StyleTargetOptions(
                    dynWideEntryX,
                    dynWideEntryWidth,
                    entryHeight,
                    this,
                    trigger.styleTarget
            ));
        }

        // Text display field
        textDisplayField = new MultiLineTextField(
                dynWideEntryX,
                0,
                dynWideEntryWidth,
                entryHeight,
                localized("option", "notif.trigger.editor.display.text.hint")
        );
        textDisplayField.setValue(displayText);
        addSpacedEntry(new Entry.DisplayField(
                dynWideEntryX,
                dynWideEntryWidth,
                entryHeight + defaultEntryHeight,
                textDisplayField,
                localized("option", "notif.trigger.editor.display.text")
        ));

        // Key display field
        keyDisplayField = new MultiLineTextField(
                dynWideEntryX,
                0,
                dynWideEntryWidth,
                entryHeight
        );
        keyDisplayField.setValue(displayKeys);
        addSpacedEntry(new Entry.DisplayField(
                dynWideEntryX,
                dynWideEntryWidth,
                entryHeight + defaultEntryHeight,
                keyDisplayField,
                localized("option", "notif.trigger.editor.display.key")
        ));

        // Filter, restyle and color controls
        addEntry(new Entry.Controls(dynWideEntryX, dynWideEntryWidth, entryHeight, this));

        // Chat message list
        addChatMessages(this.recentChat);
    }

    // Display field utils

    private void setTextDisplayValue(String text) {
        displayText = text;
        textDisplayField.setValue(displayText);
    }

    private void setKeyDisplayValue(String keys) {
        displayKeys = keys;
        keyDisplayField.setValue(displayKeys);
    }

    // Chat message list

    private void addChatMessages(List<Component> recentChat) {
        boolean restyleAll = Config.get().restyleMode.equals(Config.RestyleMode.ALL_INSTANCES);

        // Filter and restyle, retaining original copies of messages to use
        // when displaying text and key of a clicked message.
        List<Pair<Component, Component>> displayChat = new ArrayList<>();
        for (Component msg : recentChat) {
            Component restyledMsg = msg.copy();
            Matcher matcher = null;
            Component keyMatch = null;
            String msgStr = FormatUtil.stripCodes(msg.getString());
            boolean hit = switch (trigger.type) {
                case NORMAL -> {
                    matcher = MessageUtil.normalSearch(msgStr, trigger.string);
                    yield matcher.find();
                }
                case REGEX -> {
                    try {
                        matcher = Pattern.compile(trigger.string).matcher(msgStr);
                        yield matcher.find();
                    } catch (PatternSyntaxException ignored) {
                        yield false;
                    }
                }
                case KEY -> {
                    keyMatch = MessageUtil.keySearch(msg, trigger.string);
                    yield keyMatch != null;
                }
            };
            if (filter && !hit)
                continue;
            else if (restyle && hit) {
                if (trigger.styleTarget.enabled) {
                    // Process style target string if required prior to restyle
                    if (trigger.styleTarget.type == StyleTarget.Type.REGEX) {
                        trigger.styleTarget.tryCompilePattern();
                    } else if (trigger.styleTarget.type == StyleTarget.Type.CAPTURING) {
                        trigger.styleTarget.tryParseIndexes();
                    }
                }
                restyledMsg = StyleUtil.restyle(
                        msg,
                        msgStr,
                        trigger,
                        matcher,
                        keyMatch,
                        textStyle,
                        restyleAll
                );
            }
            displayChat.add(new Pair<>(msg, restyledMsg));
        }

        // Add message entries
        displayChat.forEach((pair) -> {
            Entry.MessageEntry entry = new Entry.MessageEntry(
                    dynWideEntryX,
                    dynWideEntryWidth,
                    this,
                    pair.getFirst(),
                    pair.getSecond()
            );
            addEntry(entry);
            int requiredHeight =
                    mc.font.wordWrapHeight(pair.getFirst(), dynWideEntryWidth)
                            - defaultEntryHeight;
            while (requiredHeight > 0) {
                Entry.Space spaceEntry = new Entry.Space(entry);
                addEntry(spaceEntry);
                requiredHeight -= defaultEntryHeight;
            }
        });

        // If no message entries, add note
        if (!(children().getLast() instanceof Entry.MessageEntry)) {
            addEntry(new OptionList.Entry.Text(
                    dynWideEntryX,
                    dynWideEntryWidth,
                    entryHeight,
                    localized("option", "notif.trigger.editor.recent_messages.none"),
                    null,
                    -1
            ));
        }
    }

    // Custom entries

    abstract static class Entry extends OptionList.Entry {

        private static class TriggerOptions extends Entry {

            TriggerOptions(int x, int width, int height, TriggerEditorList list, Trigger trigger) {
                super();
                int triggerFieldWidth = width - (list.tinyWidgetWidth * 2);
                int movingX = x;

                // Type button
                CycleButton<Trigger.@NotNull Type> typeButton =
                        CycleButton.builder((type) -> Component.literal(type.icon), trigger.type)
                                .withValues(Trigger.Type.values())
                                .displayOnlyValue()
                                .withTooltip((type) -> Tooltip.create(localized(
                                        "option",
                                        "notif.trigger.type." + type + ".tooltip"
                                )))
                                .create(
                                        movingX,
                                        0,
                                        list.tinyWidgetWidth,
                                        height,
                                        Component.empty(),
                                        (button, type) -> {
                                            trigger.type = type;
                                            list.init();
                                        }
                                );
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth;

                // Trigger field
                MultiLineTextField triggerField = new MultiLineTextField(
                        movingX,
                        0,
                        triggerFieldWidth,
                        height,
                        localized("option", "notif.trigger.field.hint")
                );
                if (trigger.type == Trigger.Type.REGEX)
                    triggerField.regexValidator();
                triggerField.setValueListener((str) -> {
                    trigger.string = str.strip();
                    List<OptionList.Entry> children = new ArrayList<>(list.children());
                    if (children.size() > 5) {
                        children.removeIf((entry) -> entry instanceof MessageEntry
                                || entry instanceof Text
                                || (entry instanceof Space && children.indexOf(entry) > 5));
                        list.replaceEntries(children);
                        list.addChatMessages(list.recentChat);
                    }
                });
                triggerField.setValue(trigger.string);
                elements.add(triggerField);
                movingX += triggerFieldWidth;

                // Style string add button
                Button styleButton = Button.builder(
                                Component.literal("+"), (button) -> {
                                    trigger.styleTarget.enabled = true;
                                    list.init();
                                }
                        )
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                if (!trigger.styleTarget.enabled) {
                    styleButton.setTooltip(Tooltip.create(localized(
                            "option",
                            "notif.trigger.style_target.add.tooltip"
                    )));
                    styleButton.setTooltipDelay(Duration.ofMillis(500));
                } else {
                    styleButton.active = false;
                }
                elements.add(styleButton);
            }
        }

        private static class StyleTargetOptions extends Entry {

            StyleTargetOptions(
                    int x,
                    int width,
                    int height,
                    TriggerEditorList list,
                    StyleTarget styleTarget
            ) {
                super();
                int stringFieldWidth = width - (list.tinyWidgetWidth * 4);
                int movingX = x + list.tinyWidgetWidth;

                // Info icon
                StringWidget infoIcon = new StringWidget(
                        movingX,
                        0,
                        list.tinyWidgetWidth,
                        height,
                        Component.literal(Unicode.INFO.str),
                        Minecraft.getInstance().font
                );
                infoIcon.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.trigger.style_target.tooltip"
                )));
                infoIcon.setTooltipDelay(Duration.ofMillis(500));
                elements.add(infoIcon);
                movingX += list.tinyWidgetWidth;

                // Type button
                CycleButton<StyleTarget.@NotNull Type> typeButton =
                        CycleButton.builder(
                                        (type) -> Component.literal(type.icon),
                                        styleTarget.type
                                )
                                .withValues(StyleTarget.Type.values())
                                .displayOnlyValue()
                                .withTooltip((type) -> Tooltip.create(localized(
                                        "option",
                                        "notif.trigger.style_target.type." + type + ".tooltip"
                                )))
                                .create(
                                        movingX,
                                        0,
                                        list.tinyWidgetWidth,
                                        height,
                                        Component.empty(),
                                        (button, type) -> {
                                            styleTarget.type = type;
                                            list.init();
                                        }
                                );
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth;

                // Style string field
                TextField stringField = new TextField(movingX, 0, stringFieldWidth, height);
                if (styleTarget.type == StyleTarget.Type.REGEX)
                    stringField.regexValidator();
                stringField.setMaxLength(240);
                stringField.setValue(styleTarget.string);
                stringField.setResponder((string) -> {
                    styleTarget.string = string.strip();
                    List<OptionList.Entry> children = new ArrayList<>(list.children());
                    children.removeIf((entry) -> entry instanceof MessageEntry
                            || entry instanceof Text
                            || (entry instanceof Space && children.indexOf(entry) > 4));
                    list.replaceEntries(children);
                    list.addChatMessages(list.recentChat);
                });
                stringField.setHint(localized("option", "notif.trigger.style_target.field.hint"));
                elements.add(stringField);
                movingX = x + width - list.tinyWidgetWidth;

                // Delete button
                elements.add(Button.builder(
                                Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                                (button) -> {
                                    styleTarget.enabled = false;
                                    list.init();
                                }
                        )
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build());
            }
        }

        private static class Controls extends Entry {

            Controls(int x, int width, int height, TriggerEditorList list) {
                super();
                int buttonWidth = (width - SPACE * 2) / 3;
                int movingX = x;

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED),
                                list.filter
                        )
                        .create(
                                movingX,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "notif.trigger.editor.filter"),
                                (button, status) -> {
                                    list.filter = status;
                                    list.init();
                                }
                        ));
                movingX += buttonWidth + SPACE;

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED),
                                list.restyle
                        )
                        .create(
                                movingX,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "notif.trigger.editor.restyle"),
                                (button, status) -> {
                                    list.restyle = status;
                                    list.init();
                                }
                        ));
                movingX = x + width - buttonWidth;

                elements.add(Button.builder(
                                localized("option", "notif.format.color")
                                        .setStyle(Style.EMPTY.withColor(list.textStyle.color)),
                                (button) -> {
                                    int cpHeight = HsvColorPicker.MIN_HEIGHT;
                                    int cpWidth = HsvColorPicker.MIN_WIDTH;
                                    list.screen.setOverlayWidget(new HsvColorPicker(
                                            x + width / 2 - cpWidth / 2,
                                            list.screen.height / 2 - cpHeight / 2,
                                            cpWidth,
                                            cpHeight,
                                            () -> list.textStyle.color,
                                            (val) -> list.textStyle.color = val,
                                            (widget) -> list.init()
                                    ));
                                }
                        )
                        .pos(movingX, 0)
                        .size(buttonWidth, height)
                        .build());
            }
        }

        private static class DisplayField extends Entry {

            DisplayField(int x, int width, int height, AbstractWidget widget, Component label) {
                super();
                int labelWidth = 40;
                int fieldWidth = width - labelWidth - SPACE;

                Button labelButton = Button.builder(
                                label,
                                (button -> {
                                })
                        )
                        .pos(x, 0)
                        .size(labelWidth, height)
                        .build();
                labelButton.active = false;
                elements.add(labelButton);

                widget.setWidth(fieldWidth);
                widget.setHeight(height);
                widget.setX(x + width - fieldWidth);
                elements.add(widget);
            }
        }

        private static class MessageEntry extends Entry {

            private final TriggerEditorList list;
            private final Component msg;

            MessageEntry(
                    int x,
                    int width,
                    TriggerEditorList list,
                    Component msg,
                    Component restyledMsg
            ) {
                super();
                this.list = list;
                this.msg = msg;
                MultiLineTextWidget widget =
                        new MultiLineTextWidget(x, 0, restyledMsg, Minecraft.getInstance().font);
                widget.setMaxWidth(width);
                elements.add(widget);
            }

            @Override
            public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
                list.setTextDisplayValue(FormatUtil.stripCodes(msg.getString()));

                List<String> keys = new ArrayList<>();
                getKeys(msg, keys);
                list.setKeyDisplayValue(keys.isEmpty()
                        ? localized("option", "notif.trigger.editor.display.key.none").getString()
                        : String.join("\n", keys));

                list.setScrollAmount(0);
                return true;
            }

            private static void getKeys(Component msg, List<String> keys) {
                if (msg.getContents() instanceof TranslatableContents tc) {
                    keys.add(tc.getKey());
                }

                for (Component sibling : msg.getSiblings()) {
                    getKeys(sibling, keys);
                }
            }
        }
    }
}
