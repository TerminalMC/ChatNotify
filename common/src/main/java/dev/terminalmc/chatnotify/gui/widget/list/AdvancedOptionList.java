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

package dev.terminalmc.chatnotify.gui.widget.list;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.ResponseMessage;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.field.MultiLineTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.mixin.accessor.KeyAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for advanced options of a {@link Notification}, including
 * exclusion triggers, response messages, and reset options.
 */
public class AdvancedOptionList extends DragReorderList {
    private final Notification notif;
    private OptionList.Entry.ActionButton addExclTrigEntry;
    private OptionList.Entry.ActionButton addRespMsgEntry;

    public AdvancedOptionList(Minecraft mc, int width, int height, int top, int bottom, int entryWidth,
                              int entryHeight, int entrySpacing, Notification notif) {
        super(mc, width, height, top, bottom, entryWidth, entryHeight, entrySpacing, () -> {},
                new HashMap<>(Map.of(
                        Entry.ExclusionOptions.class, notif::moveExclusionTrigger,
                        Entry.ResponseOptions.class, notif::moveResponseMessage
                )));
        this.notif = notif;
        
        addExclTrigEntry = new OptionList.Entry.ActionButton(
                entryX, entryWidth, entryHeight, Component.literal("+"), null, -1,
                (button) -> {
                    notif.exclusionTriggers.add(new Trigger());
                    init();
                    ensureVisible(addExclTrigEntry);
                });
        
        addRespMsgEntry = new OptionList.Entry.ActionButton(
                entryX, entryWidth, entryHeight, Component.literal("+"), null, -1,
                (button) -> {
                    notif.responseMessages.add(new ResponseMessage());
                    init();
                    ensureVisible(addRespMsgEntry);
                });
    }

    @Override
    protected void addEntries() {
        Minecraft mc = Minecraft.getInstance();
        
        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "advanced.control"), null, -1));
        addEntry(new Entry.Controls(entryX, entryWidth, entryHeight, notif));

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "advanced.msg", "\u2139"),
                Tooltip.create(localized("option", "advanced.msg.info.format_codes")
                        .append("\n\n")
                        .append(localized("option", "advanced.msg.info.regex_groups"))), -1));
        addEntry(new Entry.CustomMessage(dynWideEntryX, dynWideEntryWidth, entryHeight,
                () -> notif.replacementMsg, (str) -> notif.replacementMsg = str,
                () -> notif.replacementMsgEnabled, (val) -> notif.replacementMsgEnabled = val,
                localized("option", "advanced.msg.replacement")
                        .withStyle(Style.EMPTY.withColor(TextField.TEXT_COLOR_HINT)),
                localized("option", "advanced.msg.replacement").append(".\n")
                        .append(localized("option", "advanced.msg.replacement.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_hide"))));
        addEntry(new Entry.CustomMessage(dynWideEntryX, dynWideEntryWidth, entryHeight,
                () -> notif.statusBarMsg, (str) -> notif.statusBarMsg = str,
                () -> notif.statusBarMsgEnabled, (val) -> notif.statusBarMsgEnabled = val,
                localized("option", "advanced.msg.status_bar")
                        .withStyle(Style.EMPTY.withColor(TextField.TEXT_COLOR_HINT)),
                localized("option", "advanced.msg.status_bar").append(".\n")
                        .append(localized("option", "advanced.msg.status_bar.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_original"))));
        addEntry(new Entry.CustomMessage(dynWideEntryX, dynWideEntryWidth, entryHeight,
                () -> notif.titleMsg, (str) -> notif.titleMsg = str,
                () -> notif.titleMsgEnabled, (val) -> notif.titleMsgEnabled = val,
                localized("option", "advanced.msg.title")
                        .withStyle(Style.EMPTY.withColor(TextField.TEXT_COLOR_HINT)),
                localized("option", "advanced.msg.title").append(".\n")
                        .append(localized("option", "advanced.msg.title.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_original"))));
        addEntry(new Entry.CustomMessage(dynWideEntryX, dynWideEntryWidth, entryHeight,
                () -> notif.toastMsg, (str) -> notif.toastMsg = str,
                () -> notif.toastMsgEnabled, (val) -> notif.toastMsgEnabled = val,
                localized("option", "advanced.msg.toast")
                        .withStyle(Style.EMPTY.withColor(TextField.TEXT_COLOR_HINT)),
                localized("option", "advanced.msg.toast").append(".\n")
                        .append(localized("option", "advanced.msg.toast.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_original"))));

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "advanced.exclusion", "\u2139"),
                Tooltip.create(localized("option", "advanced.exclusion.tooltip")), -1));
        addEntry(new Entry.ExclusionToggle(entryX, entryWidth, entryHeight, notif, this));

        if (notif.exclusionEnabled) {
            for (int i = 0; i < this.notif.exclusionTriggers.size(); i ++) {
                addEntry(new Entry.ExclusionOptions(dynWideEntryX, dynWideEntryWidth, entryHeight,
                        this, notif, notif.exclusionTriggers.get(i), i));
            }
            addExclTrigEntry.setBounds(entryX, entryWidth, entryHeight);
            addEntry(addExclTrigEntry);
        }

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "advanced.response", "\u2139"),
                Tooltip.create(localized("option", "advanced.response.tooltip")
                        .append("\n")
                        .append(localized("option", "advanced.response.tooltip.warning")
                                .withStyle(ChatFormatting.RED))), -1));
        addEntry(new Entry.ResponseToggle(entryX, entryWidth, entryHeight, notif, this));

        if (notif.responseEnabled) {
            for (int i = 0; i < notif.responseMessages.size(); i ++) {
                Entry e = new Entry.ResponseOptions(dynWideEntryX, dynWideEntryWidth, entryHeight, this,
                        notif, notif.responseMessages.get(i), i);
                addEntry(e);
                addEntry(new Entry.Space(e));
            }
            addRespMsgEntry.setBounds(entryX, entryWidth, entryHeight);
            addEntry(addRespMsgEntry);
        }

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.broken"), null, -1));

        addEntry(new OptionList.Entry.ActionButton(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.level_1"),
                Tooltip.create(localized("option", "advanced.reset.level_1.tooltip")),
                -1,
                (button) -> {
                    notif.resetAdvanced();
                    init();
                }));

        addEntry(new OptionList.Entry.ActionButton(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.level_2"),
                Tooltip.create(localized("option", "advanced.reset.level_2.tooltip")),
                -1,
                (button) -> mc.setScreen(new ConfirmScreen(
                        (value) -> {
                            if (value) {
                                for (Notification notif2 : Config.get().getNotifs()) {
                                    notif2.resetAdvanced();
                                }
                            }
                            mc.setScreen(screen);
                            init();
                        },
                        localized("option", "advanced.reset.level_2"),
                        localized("option", "advanced.reset.level_2.confirm")))));

        addEntry(new OptionList.Entry.ActionButton(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.level_3"),
                Tooltip.create(localized("option", "advanced.reset.level_3.tooltip")),
                -1,
                (button) -> mc.setScreen(new ConfirmScreen(
                        (value) -> {
                            if (value) {
                                Config.resetAndSave();
                                mc.setScreen(null);
                            }
                            else {
                                mc.setScreen(screen);
                                init();
                            }},
                        localized("option", "advanced.reset.level_3"),
                        localized("option", "advanced.reset.level_3.confirm")))));
    }
    
    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class Controls extends Entry {
            Controls(int x, int width, int height, Notification notif) {
                super();

                elements.add(CycleButton.<Notification.CheckOwnMode>builder((status) ->
                                localized("option", "advanced.self_notify." + status.name()))
                        .withValues(Notification.CheckOwnMode.values())
                        .withInitialValue(notif.checkOwnMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option", "advanced.self_notify." + status.name() + ".tooltip")))
                        .create(x, 0, width, height,
                                localized("option", "global.self_notify"),
                                (button, status) ->
                                        notif.checkOwnMode = status));
            }
        }

        private static class CustomMessage extends NotifOptionList.Entry {
            CustomMessage(int x, int width, int height,
                          Supplier<String> textSupplier, Consumer<String> textConsumer,
                          Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                          Component hint, Component tooltip) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int fieldWidth = width - statusButtonWidth - SPACE;

                // String field
                TextField titleField = new TextField(x, 0, fieldWidth, height);
                titleField.setMaxLength(256);
                titleField.setValue(textSupplier.get());
                titleField.setResponder(textConsumer);
                titleField.setHint(hint);
                titleField.setTooltip(Tooltip.create(tooltip));
                elements.add(titleField);

                // Status button
                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                        CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(statusSupplier.get())
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) ->
                                        statusConsumer.accept(status)));
            }
        }

        private static class ExclusionToggle extends Entry {
            ExclusionToggle(int x, int width, int height, Notification notif,
                            AdvancedOptionList list) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                            Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.exclusionEnabled)
                        .create(x, 0, width, height, localized("option", "advanced.status"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    list.init();
                                }));
            }
        }

        private static class ExclusionOptions extends Entry {
            ExclusionOptions(int x, int width, int height, AdvancedOptionList list,
                             Notification notif, Trigger trigger, int index) {
                super();
                int fieldSpacing = 1;
                int triggerFieldWidth = width - list.tinyWidgetWidth - fieldSpacing;
                TextField triggerField = new TextField(0, 0, triggerFieldWidth, height);
                int movingX = x;

                // Drag reorder button
                elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                (button) -> {
                                    this.setDragging(true);
                                    list.startDragging(this, null, false);
                                })
                        .pos(x - list.smallWidgetWidth - SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());

                // Type button
                CycleButton<Trigger.Type> typeButton = CycleButton.<Trigger.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(Trigger.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(trigger.type)
                        .withTooltip((type) -> Tooltip.create(localized(
                                "option", "trigger.type." + type + ".tooltip")))
                        .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                (button, type) -> {
                                    trigger.type = type;
                                    list.init();
                                });
                typeButton.setTooltipDelay(500);
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                // Trigger field
                triggerField.setPosition(movingX, 0);
                if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.setMaxLength(240);
                triggerField.setResponder((string) -> trigger.string = string.strip());
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(localized(
                        "option", "trigger.field.tooltip")));
                triggerField.setTooltipDelay(500);
                elements.add(triggerField);

                // Delete button
                elements.add(Button.builder(
                        Component.literal("\u274C").withStyle(ChatFormatting.RED),
                        (button) -> {
                            notif.exclusionTriggers.remove(index);
                            list.init();
                        })
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class ResponseToggle extends Entry {
            ResponseToggle(int x, int width, int height, Notification notif,
                           AdvancedOptionList listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN), 
                            Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.responseEnabled)
                        .create(x, 0, width, height, localized("option", "advanced.status"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.init();
                                }));
            }
        }

        private static class ResponseOptions extends Entry {
            ResponseOptions(int x, int width, int height, AdvancedOptionList list,
                            Notification notif, ResponseMessage response, int index) {
                super();
                int fieldSpacing = 1;
                int timeFieldWidth = Minecraft.getInstance().font.width("00000");
                int msgFieldWidth = width - timeFieldWidth - list.tinyWidgetWidth - fieldSpacing * 2;
                int movingX = x;

                // Drag reorder button
                elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                (button) -> {
                                    this.setDragging(true);
                                    list.startDragging(this, null, false);
                                })
                        .pos(x - list.smallWidgetWidth - SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());

                // Type button
                CycleButton<ResponseMessage.Type> typeButton = CycleButton.<ResponseMessage.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(ResponseMessage.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(response.type)
                        .withTooltip((type) -> Tooltip.create(localized(
                                "option", "advanced.response." + type.name() + ".tooltip")))
                        .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                (button, type) -> {
                                    response.type = type;
                                    list.init();
                                });
                typeButton.setTooltipDelay(500);
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                if (response.type.equals(ResponseMessage.Type.COMMANDKEYS)) {
                    int keyFieldWidth = msgFieldWidth / 2;
                    List<String> keys = KeyAccessor.getNameMap().keySet().stream().sorted().toList();
                    FakeTextField keyField1 = new FakeTextField(movingX, 0, keyFieldWidth, height,
                            () -> {
                                int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                                int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynWideEntryWidth);
                                int wX = x + (width / 2) - (wWidth / 2);
                                int wY = list.y0;
                                list.screen.setOverlay(new DropdownTextField(
                                        wX, wY, wWidth, wHeight, Component.empty(),
                                        () -> response.string.matches(".+-.+") ? response.string.split("-")[0] : "", 
                                        (val) -> response.string = val + "-" + (response.string.matches(".+-.+") ? response.string.split("-")[1] : "key.keyboard.unknown"),
                                        (widget) -> {
                                            list.screen.removeOverlayWidget();
                                            list.init();
                                        }, keys));
                            });
                    MutableComponent label1 = localized(
                            "option", "advanced.response.commandkeys.limit_key");
                    keyField1.setHint(label1.copy().withStyle(Style.EMPTY.withColor(0x555555)));
                    keyField1.setTooltip(Tooltip.create(label1.append("\n\n").append(localized(
                            "option", "advanced.response.commandkeys.limit_key.tooltip"))));
                    keyField1.setMaxLength(240);
                    keyField1.withValidator(new TextField.Validator.InputKey(keys));
                    keyField1.setValue(response.string.matches(".+-.+") ? response.string.split("-")[0] : "");
                    elements.add(keyField1);
                    movingX += keyFieldWidth;
                    FakeTextField keyField2 = new FakeTextField(movingX, 0, keyFieldWidth, height,
                            () -> {
                                int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                                int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynWideEntryWidth);
                                int wX = x + (width / 2) - (wWidth / 2);
                                int wY = list.y0;
                                list.screen.setOverlay(new DropdownTextField(
                                        wX, wY, wWidth, wHeight, Component.empty(),
                                        () -> response.string.matches(".+-.+") ? response.string.split("-")[1] : "",
                                        (val) -> response.string = (response.string.matches(".+-.+") ? response.string.split("-")[0] + "-" + val : "key.keyboard.unknown"),
                                        (widget) -> {
                                            list.screen.removeOverlayWidget();
                                            list.init();
                                        }, keys));
                            });
                    MutableComponent label2 = localized(
                            "option", "advanced.response.commandkeys.key");
                    keyField2.setHint(label2.copy().withStyle(Style.EMPTY.withColor(0x555555)));
                    keyField2.setTooltip(Tooltip.create(label2.append("\n\n").append(localized(
                            "option", "advanced.response.commandkeys.key.tooltip"))));
                    keyField2.setMaxLength(240);
                    keyField2.withValidator(new TextField.Validator.InputKey(keys));
                    keyField2.setValue(response.string.matches(".+-.+") ? response.string.split("-")[1] : "");
                    elements.add(keyField2);
                } else {
                    // Response field
                    MultiLineTextField msgField = new MultiLineTextField(
                            movingX, 0, msgFieldWidth, height * 2);
                    msgField.setCharacterLimit(256);
                    msgField.setValue(response.string);
                    msgField.setValueListener((val) -> response.string = val.strip());
                    elements.add(msgField);
                }

                // Delay field
                TextField timeField = new TextField(
                        x + width - timeFieldWidth, 0, timeFieldWidth, height);
                timeField.posIntValidator().strict();
                timeField.setTooltip(Tooltip.create(localized(
                        "option", "advanced.response.time.tooltip")));
                timeField.setTooltipDelay(500);
                timeField.setMaxLength(5);
                timeField.setResponder((str) -> response.delayTicks = Integer.parseInt(str.strip()));
                timeField.setValue(String.valueOf(response.delayTicks));
                elements.add(timeField);

                // Delete button
                elements.add(Button.builder(
                        Component.literal("\u274C").withStyle(ChatFormatting.RED),
                        (button) -> {
                            notif.responseMessages.remove(index);
                            list.init();
                        })
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
