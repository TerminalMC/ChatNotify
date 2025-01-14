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

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.ResponseMessage;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.mixin.accessor.KeyAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for advanced options of a {@link Notification}, including
 * exclusion triggers, response messages, and reset options.
 */
public class AdvancedOptionList extends OptionList {
    private final Notification notif;
    private int dragSourceSlot = -1;

    public AdvancedOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                              int entryWidth, int entryHeight, Notification notif) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);
        this.notif = notif;

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.msg", "\u2139"), 
                Tooltip.create(localized("option", "advanced.msg.info.format_codes").append("\n\n")
                        .append(localized("option", "advanced.msg.info.regex_groups"))), -1));
        addEntry(new Entry.MessageConfigEntry(dynEntryX, dynEntryWidth, entryHeight,
                () -> notif.replacementMsg, (str) -> notif.replacementMsg = str,
                () -> notif.replacementMsgEnabled, (val) -> notif.replacementMsgEnabled = val,
                localized("option", "advanced.msg.replacement").getString(),
                localized("option", "advanced.msg.replacement").append(".\n")
                        .append(localized("option", "advanced.msg.replacement.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_hide"))));
        addEntry(new Entry.MessageConfigEntry(dynEntryX, dynEntryWidth, entryHeight,
                () -> notif.statusBarMsg, (str) -> notif.statusBarMsg = str,
                () -> notif.statusBarMsgEnabled, (val) -> notif.statusBarMsgEnabled = val,
                localized("option", "advanced.msg.status_bar").getString(),
                localized("option", "advanced.msg.status_bar").append(".\n")
                        .append(localized("option", "advanced.msg.status_bar.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_original"))));
        addEntry(new Entry.MessageConfigEntry(dynEntryX, dynEntryWidth, entryHeight,
                () -> notif.titleMsg, (str) -> notif.titleMsg = str,
                () -> notif.titleMsgEnabled, (val) -> notif.titleMsgEnabled = val,
                localized("option", "advanced.msg.title").getString(),
                localized("option", "advanced.msg.title").append(".\n")
                        .append(localized("option", "advanced.msg.title.tooltip"))
                        .append("\n\n").append(localized("option", "advanced.msg.info.blank_original"))));
        
        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.exclusion", "\u2139"),
                Tooltip.create(localized("option", "advanced.exclusion.tooltip")), -1));
        addEntry(new Entry.ExclusionToggleEntry(entryX, entryWidth, entryHeight, notif, this));

        if (notif.exclusionEnabled) {
            for (int i = 0; i < this.notif.exclusionTriggers.size(); i ++) {
                addEntry(new Entry.ExclusionFieldEntry(dynEntryX, dynEntryWidth, entryHeight,
                        this, notif, notif.exclusionTriggers.get(i), i));
            }
            addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.exclusionTriggers.add(new Trigger());
                        reload();
                    }));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.response", "\u2139"),
                Tooltip.create(localized("option", "advanced.response.tooltip")
                        .append(localized("option", "advanced.response.tooltip.warning")
                        .withStyle(ChatFormatting.RED))), -1));
        addEntry(new Entry.ResponseToggleEntry(entryX, entryWidth, entryHeight, notif, this));

        if (notif.responseEnabled) {
            for (int i = 0; i < notif.responseMessages.size(); i ++) {
                Entry e = new Entry.ResponseFieldEntry(dynEntryX, dynEntryWidth, entryHeight, this,
                        notif, notif.responseMessages.get(i), i);
                addEntry(e);
                addEntry(new SpaceEntry(e));
            }
            addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.responseMessages.add(new ResponseMessage());
                        reload();
                    }));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.broken"), null, -1));

        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.level_1"),
                Tooltip.create(localized("option", "advanced.reset.level_1.tooltip")),
                -1,
                (button) -> {
                    notif.resetAdvanced();
                    reload();
                }));

        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
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
                            reload();
                            },
                        localized("option", "advanced.reset.level_2"),
                        localized("option", "advanced.reset.level_2.confirm")))));

        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
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
                                reload();
                            }},
                        localized("option", "advanced.reset.level_3"),
                        localized("option", "advanced.reset.level_3.confirm")))));
    }

    @Override
    public AdvancedOptionList reload(int width, int height, double scrollAmount) {
        AdvancedOptionList newList = new AdvancedOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight, notif);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    // Field dragging

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        if (dragSourceSlot != -1) {
            super.renderItem(graphics, mouseX, mouseY, delta, dragSourceSlot,
                    mouseX, mouseY, entryWidth, entryHeight);
        }
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (dragSourceSlot != -1 && button == InputConstants.MOUSE_BUTTON_LEFT) {
            dropDragged(x, y);
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    private void dropDragged(double mouseX, double mouseY) {
        OptionList.Entry sourceEntry = children().get(dragSourceSlot);
        OptionList.Entry hoveredEntry = getEntryAtPosition(mouseX, mouseY);
        switch(children().get(dragSourceSlot)) {
            case Entry.ExclusionFieldEntry e -> dropDraggedExclusion(hoveredEntry);
            case Entry.ResponseFieldEntry e -> dropDraggedResponse(hoveredEntry);
            default -> throw new IllegalStateException("Unexpected value: " + sourceEntry);
        }
        this.dragSourceSlot = -1;
    }

    private void dropDraggedExclusion(OptionList.Entry hoveredEntry) {
        int targetSlot = children().indexOf(hoveredEntry);
        int offset = exclusionListOffset();
        // Check whether the drop location is valid
        if (hoveredEntry instanceof Entry.ExclusionFieldEntry || targetSlot == offset - 1) {
            int hoveredSlot = children().indexOf(hoveredEntry);
            // Check whether the move operation would actually change anything
            if (targetSlot > dragSourceSlot || targetSlot < dragSourceSlot - 1) {
                // Account for the list not starting at slot 0
                int sourceIndex = dragSourceSlot - offset;
                int destIndex = hoveredSlot - offset;
                // I can't really explain why
                if (sourceIndex > destIndex) destIndex += 1;
                // Move
                if (notif.moveExclusionTrigger(sourceIndex, destIndex)) {
                    reload();
                }
            }
        }
    }

    private void dropDraggedResponse(OptionList.Entry hoveredEntry) {
        int hoveredSlot = children().indexOf(hoveredEntry);
        // Check whether the drop location is valid
        if (hoveredEntry instanceof Entry.ResponseFieldEntry || hoveredSlot == responseListOffset() - 1) {
            // pass
        } else if (hoveredEntry instanceof SpaceEntry) {
            hoveredSlot -= 1; // Reference the 'parent' Entry
        } else {
            return;
        }
        // Check whether the move operation would actually change anything
        if (hoveredSlot > dragSourceSlot || hoveredSlot < dragSourceSlot - 1) {
            // Account for the list not starting at slot 0
            int sourceIndex = dragSourceSlot - responseOffset(dragSourceSlot);
            int destIndex = hoveredSlot - responseOffset(hoveredSlot);
            // I can't really explain why
            if (sourceIndex > destIndex) destIndex += 1;
            // Move
            if (notif.moveResponseMessage(sourceIndex, destIndex)) {
                reload();
            }
        }
    }

    /**
     * @return The index of the first {@link Entry.ExclusionFieldEntry} in the
     * {@link OptionList}.
     */
    private int exclusionListOffset() {
        int i = 0;
        for (OptionList.Entry entry : children()) {
            if (entry instanceof Entry.ExclusionFieldEntry) return i;
            i++;
        }
        throw new IllegalStateException("Exclusion list not found");
    }

    /**
     * @return The index of the first {@link Entry.ResponseFieldEntry} in the
     * {@link OptionList}.
     */
    private int responseListOffset() {
        int i = 0;
        for (OptionList.Entry entry : children()) {
            if (entry instanceof Entry.ResponseFieldEntry) return i;
            i++;
        }
        throw new IllegalStateException("Response list not found");
    }

    /**
     * @return The number of non-{@link Entry.ResponseFieldEntry} entries in the
     * {@link OptionList} before (and including) the specified index.
     */
    private int responseOffset(int index) {
        int i = 0;
        int offset = 0;
        for (OptionList.Entry entry : children()) {
            if (!(entry instanceof Entry.ResponseFieldEntry)) offset++;
            if (i++ == index) return offset;
        }
        throw new IllegalStateException("Response index out of range");
    }

    private abstract static class Entry extends OptionList.Entry {

        private static class MessageConfigEntry extends NotifOptionList.Entry {
            MessageConfigEntry(int x, int width, int height,
                               Supplier<String> textSupplier, Consumer<String> textConsumer,
                               Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                               String placeholder, Component tooltip) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int fieldWidth = width - statusButtonWidth - SPACING;

                // Title text field
                TextField titleField = new TextField(x, 0, fieldWidth, height);
                titleField.setMaxLength(256);
                if (textSupplier.get().isBlank()) {
                    // Shenanigans for placeholder text
                    titleField.setValue(placeholder);
                    titleField.setTextColor(0x555555);
                    titleField.setResponder((str) -> {
                        titleField.setTextColor(0xffffff);
                        titleField.setResponder(textConsumer);
                        titleField.setValue("");
                    });
                } else {
                    titleField.setValue(textSupplier.get());
                    titleField.setResponder(textConsumer);
                }
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

        private static class ExclusionToggleEntry extends Entry {
            ExclusionToggleEntry(int x, int width, int height, Notification notif,
                                 AdvancedOptionList list) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.exclusionEnabled)
                        .create(x, 0, width, height, localized("option", "advanced.status"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    list.reload();
                                }));
            }
        }

        private static class ExclusionFieldEntry extends Entry {
            ExclusionFieldEntry(int x, int width, int height, AdvancedOptionList list,
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
                                    list.dragSourceSlot = list.children().indexOf(this);
                                })
                        .pos(x - list.smallWidgetWidth - SPACING, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());

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
                triggerField.setResponder((string) -> trigger.string = string.strip());
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);

                // Delete button
                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    notif.exclusionTriggers.remove(index);
                                    list.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class ResponseToggleEntry extends Entry {
            ResponseToggleEntry(int x, int width, int height, Notification notif,
                                AdvancedOptionList listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.responseEnabled)
                        .create(x, 0, width, height, localized("option", "advanced.status"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ResponseFieldEntry extends Entry {
            ResponseFieldEntry(int x, int width, int height, AdvancedOptionList list,
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
                                    list.dragSourceSlot = list.children().indexOf(this);
                                })
                        .pos(x - list.smallWidgetWidth - SPACING, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());

                // Type button
                CycleButton<ResponseMessage.Type> typeButton = CycleButton.<ResponseMessage.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(ResponseMessage.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(response.type)
                        .withTooltip((type) -> Tooltip.create(
                                localized("option", "advanced.response." + type.name() + ".tooltip")))
                        .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                (button, type) -> {
                                    response.type = type;
                                    list.reload();
                                });
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                if (response.type.equals(ResponseMessage.Type.COMMANDKEYS)) {
                    int keyFieldWidth = msgFieldWidth / 2;
                    List<String> keys = KeyAccessor.getNameMap().keySet().stream().sorted().toList();
                    FakeTextField keyField1 = new FakeTextField(movingX, 0, keyFieldWidth, height,
                            () -> {
                                int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                                int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynEntryWidth);
                                int wX = x + (width / 2) - (wWidth / 2);
                                int wY = list.getY();
                                list.screen.setOverlayWidget(new DropdownTextField(
                                        wX, wY, wWidth, wHeight, Component.empty(),
                                        () -> response.string.matches(".+-.+") ? response.string.split("-")[0] : "", 
                                        (val) -> response.string = val + "-" + (response.string.matches(".+-.+") ? response.string.split("-")[1] : "key.keyboard.unknown"),
                                        (widget) -> {
                                            list.screen.removeOverlayWidget();
                                            list.reload();
                                        }, keys));
                            });
                    Component tooltip1 = localized("option", "advanced.response.commandkeys.limit_key.tooltip");
                    keyField1.setTooltip(Tooltip.create(tooltip1));
                    keyField1.setMaxLength(240);
                    keyField1.setValidator((val) -> {
                        if (keys.contains(val)) return Optional.empty();
                        else return Optional.of(tooltip1.copy().append("\n").append(
                                localized("option", "advanced.key.error").withStyle(ChatFormatting.RED)));
                    });
                    keyField1.setValue(response.string.matches(".+-.+") ? response.string.split("-")[0] : "");
                    elements.add(keyField1);
                    movingX += keyFieldWidth;
                    FakeTextField keyField2 = new FakeTextField(movingX, 0, keyFieldWidth, height,
                            () -> {
                                int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                                int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynEntryWidth);
                                int wX = x + (width / 2) - (wWidth / 2);
                                int wY = list.getY();
                                list.screen.setOverlayWidget(new DropdownTextField(
                                        wX, wY, wWidth, wHeight, Component.empty(),
                                        () -> response.string.matches(".+-.+") ? response.string.split("-")[1] : "",
                                        (val) -> response.string = (response.string.matches(".+-.+") ? response.string.split("-")[0] + "-" + val : "key.keyboard.unknown"),
                                        (widget) -> {
                                            list.screen.removeOverlayWidget();
                                            list.reload();
                                        }, keys));
                            });
                    Component tooltip2 = localized("option", "advanced.response.commandkeys.key.tooltip");
                    keyField2.setTooltip(Tooltip.create(tooltip2));
                    keyField2.setMaxLength(240);
                    keyField2.setValidator((val) -> {
                        if (keys.contains(val)) return Optional.empty();
                        else return Optional.of(tooltip2.copy().append("\n").append(
                                localized("option", "advanced.key.error").withStyle(ChatFormatting.RED)));
                    });
                    keyField2.setValue(response.string.matches(".+-.+") ? response.string.split("-")[1] : "");
                    elements.add(keyField2);
                } else {
                    // Response field
                    MultiLineEditBox msgField = new MultiLineEditBox(Minecraft.getInstance().font,
                            movingX, 0, msgFieldWidth, height * 2, Component.empty(), Component.empty());
                    msgField.setCharacterLimit(256);
                    msgField.setValue(response.string);
                    msgField.setValueListener((val) -> response.string = val.strip());
                    elements.add(msgField);
                }

                // Delay field
                TextField timeField = new TextField(
                        x + width - timeFieldWidth, 0, timeFieldWidth, height);
                timeField.posIntValidator();
                timeField.setTooltip(Tooltip.create(
                        localized("option", "advanced.response.time.tooltip")));
                timeField.setTooltipDelay(Duration.ofMillis(500));
                timeField.setMaxLength(5);
                timeField.setResponder((str) -> response.delayTicks = Integer.parseInt(str.strip()));
                timeField.setValue(String.valueOf(response.delayTicks));
                elements.add(timeField);

                // Delete button
                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    notif.responseMessages.remove(index);
                                    list.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
