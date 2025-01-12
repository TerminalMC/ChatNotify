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

package dev.terminalmc.chatnotify.gui.widget.list.option;

import com.mojang.datafixers.util.Pair;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.FormatUtil;
import dev.terminalmc.chatnotify.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class TriggerOptionList extends OptionList {
    private final Trigger trigger;
    private final TextStyle textStyle;
    private boolean filter;
    private boolean restyle;
    final MultiLineEditBox textDisplayBox;
    final EditBox keyDisplayBox;
    
    public TriggerOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                             int entryWidth, int entryHeight, Trigger trigger, TextStyle textStyle, 
                             String displayText, String displayKey, boolean filter, boolean restyle) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);
        this.trigger = trigger;
        this.textStyle = textStyle;
        this.filter = filter;
        this.restyle = restyle;
        
        addEntry(new Entry.TriggerFieldEntry(dynEntryX, dynEntryWidth, entryHeight, 
                this, trigger));
        if (trigger.styleString != null) {
            addEntry(new Entry.StyleStringFieldEntry(dynEntryX, dynEntryWidth, entryHeight, 
                    this, trigger));
        }

        textDisplayBox = new MultiLineEditBox(mc.font, dynEntryX, 0, dynEntryWidth, entryHeight, 
                localized("option", "trigger.text.placeholder"), Component.empty());
        textDisplayBox.setValue(displayText);
        keyDisplayBox = new EditBox(mc.font, dynEntryX, 0, dynEntryWidth, entryHeight, Component.empty());
        keyDisplayBox.setMaxLength(256);
        keyDisplayBox.setValue(displayKey);
        
        Entry e = new Entry.MessageFieldEntry(dynEntryX, dynEntryWidth, entryHeight + itemHeight, 
                textDisplayBox, localized("option", "trigger.message.text"));
        addEntry(e);
        addEntry(new SpaceEntry(e));
        addEntry(new Entry.MessageFieldEntry(dynEntryX, dynEntryWidth, entryHeight, keyDisplayBox, 
                localized("option", "trigger.message.key")));
        
        addEntry(new Entry.FilterAndRestyleEntry(dynEntryX, dynEntryWidth, entryHeight, this));
        
        addChat();
    }
    
    private void addChat() {
        Minecraft mc = Minecraft.getInstance();
        List<Pair<Component, MutableComponent>> allChat = ChatNotify.unmodifiedChat.stream()
                .map((msg) -> new Pair<>(msg, FormatUtil.convertToStyledLiteral(msg.copy())))
                .toList().reversed();
        List<Pair<Component, Component>> filteredChat = new ArrayList<>();
        for (Pair<Component, MutableComponent> pair : allChat) {
            Component msg = pair.getSecond();
            Matcher matcher = null;
            boolean hit = switch(trigger.type) {
                case NORMAL -> {
                    matcher = MessageUtil.normalSearch(msg.getString(), trigger.string);
                    yield matcher.find();
                }
                case REGEX -> {
                    try {
                        matcher = Pattern.compile(trigger.string).matcher(msg.getString());
                        yield matcher.find();
                    } catch (PatternSyntaxException ignored) {
                        yield false;
                    }
                }
                case KEY -> MessageUtil.keySearch(msg, trigger.string);
            };
            if (filter && !hit) continue;
            else if (restyle && hit) {
                // Restyle, using style string if possible
                if (trigger.styleString != null) {
                    Matcher m = MessageUtil.styleSearch(msg.getString(), trigger.styleString);
                    do {
                        msg = MessageUtil.restyleLeaves(msg, textStyle, m.start(), m.end());
                    } while (Config.get().restyleMode.equals(
                            Config.RestyleMode.ALL_INSTANCES) && m.find());
                } else {
                    switch(trigger.type) {
                        case NORMAL -> {
                            do {
                                msg = MessageUtil.restyleLeaves(msg, textStyle,
                                        matcher.start() + matcher.group(1).length(),
                                        matcher.end() - matcher.group(2).length());
                            } while (Config.get().restyleMode.equals(
                                    Config.RestyleMode.ALL_INSTANCES) && matcher.find());
                        }
                        case REGEX -> {
                            do {
                                msg = MessageUtil.restyleLeaves(msg, textStyle,
                                        matcher.start(), matcher.end());
                            } while (Config.get().restyleMode.equals(
                                    Config.RestyleMode.ALL_INSTANCES) && matcher.find());
                        }
                        case KEY -> msg = MessageUtil.restyleRoot(msg, textStyle);
                    }
                }
            }
            filteredChat.add(new Pair<>(pair.getFirst(), msg));
        }
        filteredChat.forEach((pair) -> {
            Entry.MessageEntry entry = new Entry.MessageEntry(dynEntryX, dynEntryWidth,
                    this, pair.getFirst(), pair.getSecond());
            addEntry(entry);
            int requiredHeight = 
                    mc.font.wordWrapHeight(pair.getFirst().getString(), dynEntryWidth) - itemHeight;
            while (requiredHeight > 0) {
                SpaceEntry spaceEntry = new SpaceEntry(entry);
                addEntry(spaceEntry);
                requiredHeight -= itemHeight;
            }
        });
        if (!(children().getLast() instanceof Entry.MessageEntry)) {
            addEntry(new OptionList.Entry.TextEntry(dynEntryX, dynEntryWidth, entryHeight,
                    localized("option", "trigger.recent_messages.none"), null, -1));
        }
    }

    @Override
    protected OptionList reload(int width, int height, double scrollAmount) {
        TriggerOptionList newList = new TriggerOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight, trigger, textStyle, 
                textDisplayBox.getValue(), keyDisplayBox.getValue(), filter, restyle);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    abstract static class Entry extends OptionList.Entry {

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, TriggerOptionList list, 
                              Trigger trigger) {
                super();
                int fieldSpacing = 1;
                int triggerFieldWidth = width - (list.tinyWidgetWidth * 2) - (fieldSpacing * 2);
                TextField triggerField = new TextField(0, 0, triggerFieldWidth, height);
                int movingX = x;

                // Type button
                CycleButton<Trigger.Type> typeButton = CycleButton.<Trigger.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(Trigger.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(trigger.type)
                        .withTooltip((type) -> Tooltip.create(
                                localized("option", "notif.trigger.type." + type + ".tooltip")))
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
                        list.children().removeIf((entry) -> entry instanceof MessageEntry 
                                || entry instanceof TextEntry 
                                || (entry instanceof SpaceEntry && list.children().indexOf(entry) > 4));
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
                            localized("option", "notif.restyle_string.add.tooltip")));
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
                        localized("option", "notif.restyle_string.tooltip")));
                infoIcon.setTooltipDelay(Duration.ofMillis(500));
                elements.add(infoIcon);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                // Style string field
                TextField stringField = new TextField(movingX, 0, stringFieldWidth, height);
                stringField.setMaxLength(240);
                stringField.setValue(trigger.styleString);
                stringField.setResponder((string) -> {
                    trigger.styleString = string.strip();
                    list.children().removeIf((entry) -> entry instanceof MessageEntry
                            || entry instanceof TextEntry
                            || (entry instanceof SpaceEntry && list.children().indexOf(entry) > 4));
                    list.addChat();
                });
                stringField.setTooltip(Tooltip.create(
                        localized("option", "notif.restyle_string.field.tooltip")));
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

        private static class FilterAndRestyleEntry extends MainOptionList.Entry {
            FilterAndRestyleEntry(int x, int width, int height, TriggerOptionList list) {
                super();
                int buttonWidth = (width - SPACING * 2) / 3;
                int movingX = x;

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(list.filter)
                        .create(movingX, 0, buttonWidth, height,
                                localized("option", "trigger.filter"),
                                (button, status) -> {
                                    list.filter = status;
                                    list.reload();
                                }));
                movingX += buttonWidth + SPACING;

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(list.restyle)
                        .create(movingX, 0, buttonWidth, height,
                                localized("option", "trigger.restyle"),
                                (button, status) -> {
                                    list.restyle = status;
                                    list.reload();
                                }));
                movingX = x + width - buttonWidth;
                
                elements.add(Button.builder(localized("option", "notif.color")
                                        .setStyle(Style.EMPTY.withColor(list.textStyle.color)), 
                                (button) -> {
                                    int cpHeight = Math.max(HsvColorPicker.MIN_HEIGHT, list.height / 2);
                                    int cpWidth = Math.max(HsvColorPicker.MIN_WIDTH, width);
                                    list.screen.setOverlayWidget(new HsvColorPicker(
                                            x, list.screen.height / 2 - cpHeight / 2, cpWidth, cpHeight,
                                            Component.empty(), () -> list.textStyle.color,
                                            (val) -> list.textStyle.color = val,
                                            (widget) -> {
                                                list.screen.removeOverlayWidget();
                                                list.reload();
                                            }));
                                })
                        .pos(movingX, 0)
                        .size(buttonWidth, height)
                        .build());
            }
        }

        private static class MessageFieldEntry extends Entry {
            MessageFieldEntry(int x, int width, int height, AbstractWidget widget, Component label) {
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
                widget.setHeight(height);
                widget.setX(x + width - fieldWidth);
                elements.add(widget);
            }
        }

        private static class MessageEntry extends Entry {
            private final TriggerOptionList list;
            private final Component msg;
            private final Component restyledMsg;

            MessageEntry(int x, int width, TriggerOptionList list, 
                         Component msg, Component restyledMsg) {
                super();
                this.list = list;
                this.msg = msg;
                this.restyledMsg = restyledMsg;
                MultiLineTextWidget widget = new MultiLineTextWidget(x, 0, restyledMsg,
                        Minecraft.getInstance().font);
                widget.setMaxWidth(width);
                elements.add(widget);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                list.textDisplayBox.setValue(msg.getString());
                list.keyDisplayBox.setValue(msg.getContents() instanceof TranslatableContents tc
                        ? tc.getKey() : localized("option", "trigger.key.none").getString());
                list.setScrollAmount(0);
                return true;
            }
        }
    }
}
