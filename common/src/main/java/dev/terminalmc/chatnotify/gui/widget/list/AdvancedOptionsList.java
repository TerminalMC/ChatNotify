/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.ResponseMessage;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.LenientEditBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for advanced options of a {@link Notification}, including
 * exclusion triggers, response messages, and reset options.
 */
public class AdvancedOptionsList extends OptionsList {
    private final Notification notif;
    private int dragSourceSlot = -1;

    public AdvancedOptionsList(Minecraft mc, int width, int height, int y, int rowWidth,
                               int itemHeight, int entryWidth, int entryHeight, Notification notif) {
        super(mc, width, height, y, rowWidth, itemHeight, entryWidth, entryHeight);
        this.notif = notif;

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.exclusion", "\u2139"),
                Tooltip.create(localized("option", "advanced.exclusion.tooltip")), -1));
        addEntry(new Entry.ExclusionToggleEntry(entryX, entryWidth, entryHeight, notif, this));

        if (notif.exclusionEnabled) {
            for (int i = 0; i < this.notif.exclusionTriggers.size(); i ++) {
                addEntry(new Entry.ExclusionFieldEntry(entryX, entryWidth, entryHeight,
                        this, notif, notif.exclusionTriggers.get(i), i));
            }
            addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.exclusionTriggers.add(new Trigger());
                        reload();
                    }));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.response", "\u2139"),
                Tooltip.create(localized("option", "advanced.response.tooltip")
                        .append(localized("option", "advanced.response.tooltip.warning")
                        .withStyle(ChatFormatting.RED))
                        .append(Config.get().allowRegex
                                ? localized("option", "advanced.response.tooltip.regex")
                                : Component.empty())), -1));
        addEntry(new Entry.ResponseToggleEntry(entryX, entryWidth, entryHeight, notif, this));

        if (notif.responseEnabled) {
            for (int i = 0; i < notif.responseMessages.size(); i ++) {
                Entry e = new Entry.ResponseFieldEntry(entryX, entryWidth, entryHeight, this, notif, i);
                addEntry(e);
                addEntry(new SpaceEntry(e));
            }
            addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.responseMessages.add(new ResponseMessage());
                        reload();
                    }));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.broken"), null, -1));

        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced.reset.level_1"),
                Tooltip.create(localized("option", "advanced.reset.level_1.tooltip")),
                -1,
                (button) -> {
                    notif.resetAdvanced();
                    reload();
                }));

        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
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

        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
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
    public AdvancedOptionsList reload(int width, int height, double scrollAmount) {
        AdvancedOptionsList newListWidget = new AdvancedOptionsList(minecraft, width, height, 
                getY(), getRowWidth(), itemHeight, entryWidth, entryHeight, notif);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private void openTriggerConfig(Trigger trigger) {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                localized("option", "trigger"),
                new TriggerOptionsList(minecraft, width, height, getY(), getRowWidth(),
                        itemHeight, entryWidth, entryHeight, trigger)));
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
        OptionsList.Entry sourceEntry = children().get(dragSourceSlot);
        OptionsList.Entry hoveredEntry = getEntryAtPosition(mouseX, mouseY);
        switch(children().get(dragSourceSlot)) {
            case Entry.ExclusionFieldEntry e -> dropDraggedExclusion(hoveredEntry);
            case Entry.ResponseFieldEntry e -> dropDraggedResponse(hoveredEntry);
            default -> throw new IllegalStateException("Unexpected value: " + sourceEntry);
        }
        this.dragSourceSlot = -1;
    }

    private void dropDraggedExclusion(OptionsList.Entry hoveredEntry) {
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
                notif.moveExclusionTrigger(sourceIndex, destIndex);
                reload();
            }
        }
    }

    private void dropDraggedResponse(OptionsList.Entry hoveredEntry) {
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
            notif.moveResponseMessage(sourceIndex, destIndex);
            reload();
        }
    }

    /**
     * @return The index of the first {@link Entry.ExclusionFieldEntry} in the
     * {@link OptionsList}.
     */
    private int exclusionListOffset() {
        int i = 0;
        for (OptionsList.Entry entry : children()) {
            if (entry instanceof Entry.ExclusionFieldEntry) return i;
            i++;
        }
        throw new IllegalStateException("Exclusion list not found");
    }

    /**
     * @return The index of the first {@link Entry.ResponseFieldEntry} in the
     * {@link OptionsList}.
     */
    private int responseListOffset() {
        int i = 0;
        for (OptionsList.Entry entry : children()) {
            if (entry instanceof Entry.ResponseFieldEntry) return i;
            i++;
        }
        throw new IllegalStateException("Response list not found");
    }

    /**
     * @return The number of non-{@link Entry.ResponseFieldEntry} entries in the
     * {@link OptionsList} before (and including) the specified index.
     */
    private int responseOffset(int index) {
        int i = 0;
        int offset = 0;
        for (OptionsList.Entry entry : children()) {
            if (!(entry instanceof Entry.ResponseFieldEntry)) offset++;
            if (i++ == index) return offset;
        }
        throw new IllegalStateException("Response index out of range");
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class ExclusionToggleEntry extends Entry {
            ExclusionToggleEntry(int x, int width, int height, Notification notif,
                                 AdvancedOptionsList listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.exclusionEnabled)
                        .create(x, 0, width, height, localized("common", "status"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ExclusionFieldEntry extends Entry {
            ExclusionFieldEntry(int x, int width, int height, AdvancedOptionsList listWidget,
                                Notification notif, Trigger trigger, int index) {
                super();
                boolean regex = Config.get().allowRegex;
                int fieldSpacing = 1;
                int smallButtonWidth = 16;
                int sideButtonWidth = Math.max(16, height);
                int regexButtonX = x;
                int keyButtonX = x + (regex ? smallButtonWidth : 0);
                int triggerFieldWidth = width - smallButtonWidth - fieldSpacing
                        - (regex ? smallButtonWidth : 0);
                int triggerFieldX = x + smallButtonWidth + fieldSpacing + (regex ? smallButtonWidth : 0);

                EditBox triggerField = new LenientEditBox(Minecraft.getInstance().font,
                        triggerFieldX, 0, triggerFieldWidth, height, Component.empty());
                triggerField.setMaxLength(240);
                triggerField.setValue(trigger.string);
                triggerField.setResponder((string) -> trigger.string = string.strip());
                elements.add(triggerField);

                // Regex button
                if (regex) {
                    String icon = ".*";
                    CycleButton<Boolean> regexButton = CycleButton.booleanBuilder(
                            Component.literal(icon).withStyle(ChatFormatting.GREEN),
                                    Component.literal(icon).withStyle(ChatFormatting.RED))
                            .displayOnlyValue()
                            .withInitialValue(trigger.isRegex)
                            .withTooltip((status) -> Tooltip.create(status
                                    ? localized("option", "notif.regex.enabled")
                                    : localized("option", "notif.regex.disabled")))
                            .create(regexButtonX, 0, smallButtonWidth, height,
                                    Component.empty(), (button, status) -> trigger.isRegex = status);
                    regexButton.setTooltipDelay(Duration.ofMillis(500));
                    if (trigger.isKey) {
                        regexButton.setMessage(Component.literal(icon).withStyle(ChatFormatting.GRAY));
                        regexButton.setTooltip(Tooltip.create(
                                localized("option", "notif.regex.disabled.key")));
                        regexButton.active = false;
                    }
                    elements.add(regexButton);
                }

                // Key button
                CycleButton<Boolean> keyButton = CycleButton.booleanBuilder(
                                Component.literal("\uD83D\uDD11").withStyle(ChatFormatting.GREEN),
                                Component.literal("\uD83D\uDD11").withStyle(ChatFormatting.RED))
                        .withInitialValue(trigger.isKey)
                        .displayOnlyValue()
                        .withTooltip((status) -> Tooltip.create(status
                                ? localized("option", "notif.key.enabled")
                                : localized("option", "notif.key.disabled")))
                        .create(keyButtonX, 0, smallButtonWidth, height, Component.empty(),
                                (button, status) -> {
                                    if (Screen.hasShiftDown()) {
                                        trigger.isKey = status;
                                        listWidget.reload();
                                    } else {
                                        listWidget.openTriggerConfig(trigger);
                                    }});
                keyButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(keyButton);

                // Delete button
                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    notif.exclusionTriggers.remove(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(sideButtonWidth, height)
                        .build());

                // Drag reorder button
                elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                (button) -> {
                                    this.setDragging(true);
                                    listWidget.dragSourceSlot = listWidget.children().indexOf(this);
                                })
                        .pos(x - sideButtonWidth - SPACING, 0)
                        .size(sideButtonWidth, height)
                        .build());
            }
        }

        private static class ResponseToggleEntry extends Entry {
            ResponseToggleEntry(int x, int width, int height, Notification notif,
                                AdvancedOptionsList listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.responseEnabled)
                        .create(x, 0, width, height, localized("common", "status"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ResponseFieldEntry extends Entry {
            ResponseFieldEntry(int x, int width, int height, AdvancedOptionsList listWidget,
                               Notification notif, int index) {
                super();
                boolean regex = Config.get().allowRegex;
                int fieldSpacing = 1;
                int smallButtonWidth = 16;
                int sideButtonWidth = Math.max(16, height);
                int timeFieldWidth = Minecraft.getInstance().font.width("00000");
                int msgFieldWidth = width - timeFieldWidth - fieldSpacing
                        - (regex ? smallButtonWidth + fieldSpacing : 0);
                int msgFieldX = x + (regex ? smallButtonWidth + fieldSpacing : 0);
                ResponseMessage response = notif.responseMessages.get(index);

                // Response field
                MultiLineEditBox messageField = new MultiLineEditBox(Minecraft.getInstance().font,
                        msgFieldX, 0, msgFieldWidth, height * 2,
                        Component.empty(), Component.empty());
                messageField.setCharacterLimit(256);
                messageField.setValue(response.string);
                messageField.setValueListener((val) -> response.string = val.strip());
                elements.add(messageField);

                // Delay field
                EditBox timeField = new EditBox(Minecraft.getInstance().font,
                        x + width - timeFieldWidth, 0, timeFieldWidth, height, Component.empty());
                timeField.setTooltip(Tooltip.create(
                        localized("option", "advanced.response.time.tooltip")));
                timeField.setTooltipDelay(Duration.ofMillis(500));
                timeField.setMaxLength(5);
                timeField.setValue(String.valueOf(response.delayTicks));
                timeField.setResponder((val) -> {
                    try {
                        int delay = Integer.parseInt(val.strip());
                        if (delay < 0) throw new NumberFormatException();
                        response.delayTicks = delay;
                        timeField.setTextColor(16777215);
                    } catch (NumberFormatException ignored) {
                        timeField.setTextColor(16711680);
                    }
                });
                elements.add(timeField);

                // Regex button
                if (regex) {
                    String icon = ".*";
                    CycleButton<Boolean> regexButton = CycleButton.booleanBuilder(
                                    Component.literal(icon).withStyle(ChatFormatting.GREEN),
                                    Component.literal(icon).withStyle(ChatFormatting.RED))
                            .displayOnlyValue()
                            .withInitialValue(response.regexGroups)
                            .withTooltip((status) -> Tooltip.create(status
                                    ? localized("option", "advanced.response.regex.enabled")
                                    : localized("option", "advanced.response.regex.disabled")))
                            .create(x, 0, smallButtonWidth, height,
                                    Component.empty(), (button, status) -> response.regexGroups = status);
                    regexButton.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(regexButton);
                }

                // Delete button
                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    notif.responseMessages.remove(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(sideButtonWidth, height)
                        .build());

                // Drag reorder button
                elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                (button) -> {
                                    this.setDragging(true);
                                    listWidget.dragSourceSlot = listWidget.children().indexOf(this);
                                })
                        .pos(x - sideButtonWidth - SPACING, 0)
                        .size(sideButtonWidth, height)
                        .build());
            }
        }
    }
}
