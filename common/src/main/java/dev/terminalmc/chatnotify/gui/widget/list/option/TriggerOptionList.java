/*
 * Copyright 2024 TerminalMC
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

package dev.terminalmc.chatnotify.gui.widget.list.option;

import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.mixin.accessor.ChatComponentAccessor;
import dev.terminalmc.chatnotify.util.MessageProcessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.StringUtil;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class TriggerOptionList extends OptionList {
    private final Trigger trigger;
    private final TextStyle textStyle;
    private boolean filter;
    final TextField textDisplayBox;
    final TextField keyDisplayBox;
    
    public TriggerOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                             int entryWidth, int entryHeight, Trigger trigger, TextStyle textStyle, 
                             String displayText, String displayKey, boolean filter) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);
        this.trigger = trigger;
        this.textStyle = textStyle;
        this.filter = filter;
        
        addEntry(new Entry.TriggerFieldEntry(dynEntryX, dynEntryWidth, entryHeight, 
                this, trigger));
        if (trigger.styleString != null) {
            addEntry(new Entry.StyleStringFieldEntry(dynEntryX, dynEntryWidth, entryHeight, 
                    this, trigger));
        }

        textDisplayBox = new TextField(dynEntryX, 0, dynEntryWidth, entryHeight, true);
        textDisplayBox.setMaxLength(256);
        textDisplayBox.setValue(displayText);
        keyDisplayBox = new TextField(dynEntryX, 0, dynEntryWidth, entryHeight, true);
        textDisplayBox.setMaxLength(256);
        keyDisplayBox.setValue(displayKey);
        
        addEntry(new Entry.MessageFieldEntry(dynEntryX, dynEntryWidth, entryHeight, textDisplayBox, 
                localized("option", "notif.message.text")));
        addEntry(new Entry.MessageFieldEntry(dynEntryX, dynEntryWidth, entryHeight, keyDisplayBox, 
                localized("option", "notif.message.key")));

        addEntry(new OptionList.Entry.TextEntry(dynEntryX, dynEntryWidth, entryHeight,
                localized("option", "notif.recent_messages"), null, -1));
        
        addEntry(new OptionList.Entry.ActionButtonEntry(dynEntryX, dynEntryWidth, entryHeight, 
                localized("option", "notif.filter", this.filter 
                        ? CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN)
                        : CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)), 
                null, -1, (button) -> {
                    this.filter = !this.filter;
                    reload();
                }));
        
        addChat();
    }
    
    private void addChat() {
        Minecraft mc = Minecraft.getInstance();
        ((ChatComponentAccessor)mc.gui.getChat()).getAllMessages().stream()
                .filter((msg) -> !filter || trigger.string.isBlank() || switch (trigger.type) {
                    case NORMAL -> MessageProcessor.normalSearch(
                            msg.content().getString(), trigger.string).find();
                    case REGEX -> {
                        if (trigger.pattern != null) {
                            yield trigger.pattern.matcher(msg.content().getString()).find();
                        }
                        yield false;
                    }
                    case KEY -> MessageProcessor.keySearch(msg.content(), trigger.string);
                })
                .map((msg) -> {
                    if (!filter || trigger.string.isBlank()) return msg.content();
                    String cleanStr = StringUtil.stripColor(msg.content().getString());
                    if (trigger.type != Trigger.Type.NORMAL) {
                        if (trigger.styleString != null && !trigger.styleString.isBlank() 
                                && MessageProcessor.styleSearch(
                                cleanStr, trigger.styleString).find()) {
                            return MessageProcessor.complexRestyle(
                                    msg.content(), trigger.styleString, textStyle);
                        } else {
                            return MessageProcessor.simpleRestyle(msg.content(), textStyle);
                        }
                    } else {
                        if (trigger.styleString != null && !trigger.styleString.isBlank() 
                                && MessageProcessor.styleSearch(
                                cleanStr, trigger.styleString).find()) {
                            return MessageProcessor.complexRestyle(
                                    msg.content(), trigger.styleString, textStyle);
                        } else if (MessageProcessor.styleSearch(cleanStr, trigger.string).find()) {
                            return MessageProcessor.complexRestyle(
                                    msg.content(), trigger.string, textStyle);
                        } else {
                            return MessageProcessor.simpleRestyle(msg.content(), textStyle);
                        }
                    }
                })
                .forEach((msg) -> {
                    Entry.MessageEntry entry = new Entry.MessageEntry(dynEntryX, dynEntryWidth,
                            this, msg);
                    addEntry(entry);
                    int requiredHeight = 
                            mc.font.wordWrapHeight(msg.getString(), dynEntryWidth) - itemHeight;
                    while (requiredHeight > 0) {
                        SpaceEntry spaceEntry = new SpaceEntry(entry);
                        addEntry(spaceEntry);
                        requiredHeight -= itemHeight;
                    }
                });
    }

    @Override
    protected OptionList reload(int width, int height, double scrollAmount) {
        TriggerOptionList newList = new TriggerOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight, trigger, textStyle, 
                textDisplayBox.getValue(), keyDisplayBox.getValue(), filter);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    abstract static class Entry extends OptionList.Entry {

        private static class MessageFieldEntry extends Entry {
            MessageFieldEntry(int x, int width, int height, TextField widget, Component label) {
                super();
                int labelWidth = 40;
                int fieldWidth = width - labelWidth - SPACING;

                Button labelButton = Button.builder(label, (button -> {}))
                        .pos(x, 0)
                        .size(labelWidth, height)
                        .build();
                labelButton.active = false;
                elements.add(labelButton);
                
                widget.setWidth(fieldWidth);
                widget.setX(x + width - fieldWidth);
                elements.add(widget);
            }
        }
        
        private static class MessageEntry extends Entry {
            private final TriggerOptionList list;
            private final Component message;
            
            MessageEntry(int x, int width, TriggerOptionList list, Component message) {
                super();
                this.list = list;
                this.message = message;
                MultiLineTextWidget widget = new MultiLineTextWidget(x, 0, message, 
                        Minecraft.getInstance().font);
                widget.setMaxWidth(width);
                elements.add(widget);
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                list.textDisplayBox.setValue(message.getString());
                list.keyDisplayBox.setValue(message.getContents() instanceof TranslatableContents tc 
                        ? tc.getKey() : "No translation key");
                list.setScrollAmount(0);
                return true;
            }
        }

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, TriggerOptionList list, 
                              Trigger trigger) {
                super();
                int fieldSpacing = 1;
                int triggerFieldWidth = width - (list.tinyWidgetWidth * 2) - (fieldSpacing * 2);
                TextField triggerField = new TextField(0, 0, triggerFieldWidth, height, true);
                int movingX = x;

                // Type button
                CycleButton<Trigger.Type> typeButton = CycleButton.<Trigger.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(Trigger.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(trigger.type)
                        .withTooltip((type) -> Tooltip.create(switch(type) {
                            case NORMAL -> localized("option", "notif.trigger.tooltip.normal");
                            case REGEX -> localized("option", "notif.trigger.tooltip.regex");
                            case KEY -> localized("option", "notif.trigger.tooltip.key");
                        }))
                        .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                (button, type) -> {
                                    trigger.type = type;
                                    list.reload();
                                });
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                // Trigger field
                triggerField.setPosition(movingX, 0);
                if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.setMaxLength(240);
                triggerField.setResponder((str) -> {
                    trigger.string = str.strip();
                    if (list.children().size() > 4) {
                        list.children().removeIf((entry) ->
                                entry instanceof MessageEntry || entry instanceof SpaceEntry);
                        list.addChat();
                    }
                });
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "notif.trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);
                movingX = x + width - list.tinyWidgetWidth;

                // Style string add button
                Button styleButton = Button.builder(Component.literal("+"),
                                (button) -> {
                                    trigger.styleString = "";
                                    list.reload();
                                })
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                if (trigger.styleString == null) {
                    styleButton.setTooltip(Tooltip.create(
                            localized("option", "notif.style_string.add.tooltip")));
                    styleButton.setTooltipDelay(Duration.ofMillis(500));
                } else {
                    styleButton.active = false;
                }
                elements.add(styleButton);
            }
        }

        private static class StyleStringFieldEntry extends Entry {
            StyleStringFieldEntry(int x, int width, int height, TriggerOptionList list, 
                                  Trigger trigger) {
                super();
                int fieldSpacing = 1;
                int stringFieldWidth = width - (list.tinyWidgetWidth * 3) - (fieldSpacing * 2);
                int movingX = x + list.tinyWidgetWidth;

                // Info icon
                StringWidget infoIcon = new StringWidget(movingX, 0, list.tinyWidgetWidth, height,
                        Component.literal("\u2139"), Minecraft.getInstance().font);
                infoIcon.alignCenter();
                infoIcon.setTooltip(Tooltip.create(
                        localized("option", "notif.style_string.tooltip")));
                infoIcon.setTooltipDelay(Duration.ofMillis(500));
                elements.add(infoIcon);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                // Style string field
                TextField stringField = new TextField(movingX, 0, stringFieldWidth, height);
                stringField.setMaxLength(240);
                stringField.setValue(trigger.styleString);
                stringField.setResponder((string) -> {
                    trigger.styleString = string.strip();
                    list.children().removeIf((entry) ->
                            entry instanceof MessageEntry || entry instanceof SpaceEntry);
                    list.addChat();
                });
                stringField.setTooltip(Tooltip.create(
                        localized("option", "notif.style_string.field.tooltip")));
                stringField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(stringField);
                movingX = x + width - list.tinyWidgetWidth;

                // Delete button
                elements.add(Button.builder(Component.literal("\u274C"),
                                (button) -> {
                                    trigger.styleString = null;
                                    list.reload();
                                })
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build());
            }
        }
    }
}
