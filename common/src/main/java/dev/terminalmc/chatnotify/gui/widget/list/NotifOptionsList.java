/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.TriState;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.LenientEditBox;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for options of a {@link Notification}, and buttons linking
 * to other screens.
 */
public class NotifOptionsList extends OptionsList {
    private final Notification notif;
    private final boolean isUsername;
    private int dragSourceSlot = -1;
    private boolean dragHasStyleField = false;

    public NotifOptionsList(Minecraft mc, int width, int height, int y, int rowWidth,
                            int itemHeight, int entryWidth, int entryHeight,
                            Notification notif, boolean isUsername) {
        super(mc, width, height, y, rowWidth, itemHeight, entryWidth, entryHeight);
        this.notif = notif;
        this.isUsername = isUsername;
        notif.editing = true;

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif.triggers", "\u2139"),
                Tooltip.create(localized("option", "notif.triggers.tooltip")), -1));

        for (int i = 0; i < notif.triggers.size(); i++) {
            Trigger trigger = notif.triggers.get(i);
            addEntry(new Entry.TriggerFieldEntry(entryX, entryWidth, entryHeight,
                    this, notif, trigger, i));
            if (trigger.styleString != null) {
                addEntry(new Entry.StyleStringFieldEntry(entryX, entryWidth, entryHeight,
                        this, trigger));
            }
        }
        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    notif.triggers.add(new Trigger());
                    reload();
                }));


        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif.controls"), null, -1));

        addEntry(new Entry.SoundConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.ColorConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.FormatConfigEntry(entryX, entryWidth, entryHeight, notif, true));
        addEntry(new Entry.FormatConfigEntry(entryX, entryWidth, entryHeight, notif, false));

        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif.advanced"),
                Tooltip.create(localized("option", "notif.advanced.tooltip")), 500,
                (button) -> openAdvancedConfig()));
    }

    @Override
    public NotifOptionsList reload(int width, int height, double scrollAmount) {
        NotifOptionsList newListWidget = new NotifOptionsList(minecraft, width, height,
                getY(), getRowWidth(), itemHeight, entryWidth, entryHeight, notif, isUsername);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    @Override
    public void onClose() {
        notif.editing = false;
        notif.autoDisable();
    }

    private void openKeyConfig(Trigger trigger) {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "trigger"),
                new TriggerOptionsList(minecraft, width, height, getY(), getRowWidth(),
                        itemHeight, entryWidth, entryHeight, trigger)));
    }

    private void openSoundConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "sound"),
                new SoundOptionsList(minecraft, width, height, getY(), getRowWidth(),
                        itemHeight, entryWidth, entryHeight, notif.sound)));
    }

    private void openAdvancedConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "advanced"),
                new AdvancedOptionsList(minecraft, width, height, getY(), getRowWidth(),
                        itemHeight, entryWidth, entryHeight, notif)));
    }

    // Trigger field dragging

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        if (dragSourceSlot != -1) {
            super.renderItem(graphics, mouseX, mouseY, delta, dragSourceSlot,
                    mouseX, mouseY, entryWidth, entryHeight);
            if (dragHasStyleField) {
                super.renderItem(graphics, mouseX, mouseY, delta, dragSourceSlot + 1,
                        mouseX, mouseY + itemHeight, entryWidth, entryHeight);
            }
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

    /**
     * A dragged entry, when dropped, will be placed below the hovered entry.
     * Therefore, the move operation will only be executed if the hovered entry
     * is below the dragged entry, or more than one slot above.
     */
    private void dropDragged(double mouseX, double mouseY) {
        OptionsList.Entry hoveredEntry = getEntryAtPosition(mouseX, mouseY);
        int hoveredSlot = children().indexOf(hoveredEntry);
        // Check whether the drop location is valid
        if (hoveredEntry instanceof Entry.TriggerFieldEntry || hoveredSlot == triggerListOffset() - 1) {
            // pass
        } else if (hoveredEntry instanceof Entry.StyleStringFieldEntry) {
            hoveredSlot -= 1; // Reference the 'parent' Entry
        } else {
            this.dragSourceSlot = -1;
            return;
        }
        // Check whether the move operation would actually change anything
        if (hoveredSlot > dragSourceSlot || hoveredSlot < dragSourceSlot - 1) {
            // Account for the list not starting at slot 0
            int sourceIndex = dragSourceSlot - triggerOffset(dragSourceSlot);
            int destIndex = hoveredSlot - triggerOffset(hoveredSlot);
            // I can't really explain why
            if (sourceIndex > destIndex) destIndex += 1;
            // Move
            notif.moveTrigger(sourceIndex, destIndex);
            reload();
        }
        this.dragSourceSlot = -1;
    }

    /**
     * @return The index of the first {@link Entry.TriggerFieldEntry} in the
     * {@link OptionsList}.
     */
    private int triggerListOffset() {
        int i = 0;
        for (OptionsList.Entry entry : children()) {
            if (entry instanceof Entry.TriggerFieldEntry) return i;
            i++;
        }
        throw new IllegalStateException("Trigger list not found");
    }

    /**
     * @return The number of non-{@link Entry.TriggerFieldEntry} entries in the
     * {@link OptionsList} before (and including) the specified index.
     */
    private int triggerOffset(int index) {
        int i = 0;
        int offset = 0;
        for (OptionsList.Entry entry : children()) {
            if (!(entry instanceof Entry.TriggerFieldEntry)) offset++;
            if (i++ == index) return offset;
        }
        throw new IllegalStateException("Trigger index out of range");
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, NotifOptionsList listWidget,
                              Notification notif, Trigger trigger, int index) {
                super();
                boolean regex = Config.get().allowRegex;
                int fieldSpacing = 1;
                int smallButtonWidth = 16;
                int sideButtonWidth = Math.max(16, height);
                int triggerFieldWidth = width - smallButtonWidth * 2 - fieldSpacing * 2
                        - (regex ? smallButtonWidth : 0);
                int triggerFieldX = x + smallButtonWidth + fieldSpacing + (regex ? smallButtonWidth : 0);

                // Trigger field
                EditBox triggerField = new LenientEditBox(Minecraft.getInstance().font,
                        triggerFieldX, 0, triggerFieldWidth, height, Component.empty());
                triggerField.setMaxLength(240);
                triggerField.setValue(trigger.string);
                triggerField.setResponder((string) -> trigger.string = string.strip());
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "notif.trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);

                if (listWidget.isUsername && index <= 1) {
                    triggerField.setEditable(false);
                    triggerField.active = false;
                    triggerField.setTooltip(Tooltip.create((index == 0
                            ? localized("option", "notif.trigger.profile_name.tooltip")
                            : localized("option", "notif.trigger.display_name.tooltip"))));
                }
                else {
                    int regexButtonX = x;
                    int keyButtonX = x + (regex ? smallButtonWidth : 0);

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
                                            listWidget.openKeyConfig(trigger);
                                        }});
                    keyButton.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(keyButton);

                    // Style string add button
                    Button styleButton = Button.builder(Component.literal("+"),
                                    (button) -> {
                                        trigger.styleString = "";
                                        listWidget.reload();
                                    })
                            .pos(x + width - smallButtonWidth, 0)
                            .size(smallButtonWidth, height)
                            .build();
                    if (trigger.styleString == null) {
                        styleButton.setTooltip(Tooltip.create(
                                localized("option", "notif.style_string.add.tooltip")));
                        styleButton.setTooltipDelay(Duration.ofMillis(500));
                    } else {
                        styleButton.active = false;
                    }
                    elements.add(styleButton);

                    // Delete button
                    elements.add(Button.builder(Component.literal("\u274C")
                                            .withStyle(ChatFormatting.RED),
                                    (button) -> {
                                        notif.triggers.remove(index);
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
                                        listWidget.dragHasStyleField = trigger.styleString != null;
                                    })
                            .pos(x - sideButtonWidth - SPACING, 0)
                            .size(sideButtonWidth, height)
                            .build());
                }
            }
        }

        private static class StyleStringFieldEntry extends Entry {
            StyleStringFieldEntry(int x, int width, int height, NotifOptionsList listWidget,
                                  Trigger trigger) {
                super();
                boolean regex = Config.get().allowRegex;
                int fieldSpacing = 1;
                int smallButtonWidth = 16;
                int stringFieldWidth = width - smallButtonWidth * 2 - fieldSpacing * 2
                        - (regex ? smallButtonWidth : 0);
                int stringFieldX = x + smallButtonWidth + fieldSpacing + (regex ? smallButtonWidth : 0);

                StringWidget infoIcon = new StringWidget(x + smallButtonWidth + fieldSpacing, 0,
                        smallButtonWidth, height, Component.literal("\u2139"),
                        Minecraft.getInstance().font);
                infoIcon.alignCenter();
                infoIcon.setTooltip(Tooltip.create(localized("option", "notif.style_string.tooltip")));
                infoIcon.setTooltipDelay(Duration.ofMillis(500));
                elements.add(infoIcon);

                EditBox stringField = new EditBox(Minecraft.getInstance().font,
                        stringFieldX, 0, stringFieldWidth, height, Component.empty());
                stringField.setMaxLength(240);
                stringField.setValue(trigger.styleString);
                stringField.setResponder((string) -> trigger.styleString = string.strip());
                stringField.setTooltip(Tooltip.create(
                        localized("option", "notif.style_string.field.tooltip")));
                stringField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(stringField);

                elements.add(Button.builder(Component.literal("\u274C"),
                                (button) -> {
                                    trigger.styleString = null;
                                    listWidget.reload();
                                })
                        .pos(x + width - smallButtonWidth, 0)
                        .size(smallButtonWidth, height)
                        .build());
            }
        }

        private static class SoundConfigEntry extends Entry {
            SoundConfigEntry(int x, int width, int height, Notification notif,
                             NotifOptionsList listWidget) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int mainButtonWidth = width - statusButtonWidth - SPACING;

                elements.add(Button.builder(localized("option", "notif.sound", notif.sound.getId()),
                                (button) -> listWidget.openSoundConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.sound.isEnabled())
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) -> notif.sound.setEnabled(status)));
            }
        }

        private static class ColorConfigEntry extends Entry {
            ColorConfigEntry(int x, int width, int height, Notification notif, NotifOptionsList listWidget) {
                super();
                Font font = Minecraft.getInstance().font;
                int statusButtonWidth = Math.max(24, height);
                int colorFieldWidth = font.width("#FFAAFF+++");
                int mainButtonWidth = width - colorFieldWidth - statusButtonWidth - SPACING * 2;

                Button mainButton = Button.builder(localized("option", "notif.color")
                                        .setStyle(Style.EMPTY.withColor(notif.textStyle.getTextColor())),
                                (button) -> {
                                    int cpHeight = HsvColorPicker.MIN_HEIGHT;
                                    int cpWidth = Math.max(HsvColorPicker.MIN_WIDTH, width);
                                    listWidget.screen.setOverlayWidget(new HsvColorPicker(
                                            x, listWidget.screen.height / 2 - cpHeight / 2, cpWidth, cpHeight,
                                            Component.empty(), () -> notif.textStyle.color,
                                            (val) -> notif.textStyle.color = val,
                                            (widget) -> {
                                                listWidget.screen.removeOverlayWidget();
                                                listWidget.reload();
                                            }));
                                })
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                EditBox colorField = new EditBox(font, x + mainButtonWidth + SPACING, 0,
                        colorFieldWidth, height, Component.empty());
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = ColorUtil.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        notif.textStyle.color = color;
                        // Update color of main button
                        mainButton.setMessage(localized("option", "notif.color")
                                .setStyle(Style.EMPTY.withColor(textColor)));
                        colorField.setTextColor(color);
                    } else {
                        colorField.setTextColor(16711680);
                    }
                });
                colorField.setValue(notif.textStyle.getTextColor().formatValue());
                elements.add(colorField);

                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.textStyle.doColor)
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) -> notif.textStyle.doColor = status));
            }
        }

        private static class FormatConfigEntry extends Entry {
            private FormatConfigEntry(int x, int width, int height, Notification notif, boolean first) {
                super();
                if (first) createFirst(x, width, height, notif);
                else createSecond(x, width, height, notif);
            }

            private void createFirst(int x, int width, int height, Notification notif) {
                int buttonWidth = (width - SPACING * 2) / 3;

                CycleButton<TriState.State> boldButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.BOLD))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.bold.getState())
                        .withTooltip(this::getTooltip)
                        .create(x, 0, buttonWidth, height,
                                localized("option", "notif.format.bold"),
                                (button, state) -> notif.textStyle.bold.state = state);
                boldButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(boldButton);

                CycleButton<TriState.State> italicButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.ITALIC))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.italic.getState())
                        .withTooltip(this::getTooltip)
                        .create(x + width / 2 - buttonWidth / 2, 0, buttonWidth, height,
                                localized("option", "notif.format.italic"),
                                (button, state) -> notif.textStyle.italic.state = state);
                italicButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(italicButton);

                CycleButton<TriState.State> underlineButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.UNDERLINE))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.underlined.getState())
                        .withTooltip(this::getTooltip)
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "notif.format.underline"),
                                (button, state) -> notif.textStyle.underlined.state = state);
                underlineButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(underlineButton);
            }

            private void createSecond(int x, int width, int height, Notification notif) {
                int buttonWidth = (width - SPACING) / 2;

                CycleButton<TriState.State> strikethroughButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.STRIKETHROUGH))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.strikethrough.getState())
                        .withTooltip(this::getTooltip)
                        .create(x, 0, buttonWidth, height,
                                localized("option", "notif.format.strikethrough"),
                                (button, state) -> notif.textStyle.strikethrough.state = state);
                strikethroughButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(strikethroughButton);

                CycleButton<TriState.State> obfuscateButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.OBFUSCATED))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.obfuscated.getState())
                        .withTooltip(this::getTooltip)
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "notif.format.obfuscate"),
                                (button, state) -> notif.textStyle.obfuscated.state = state);
                obfuscateButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(obfuscateButton);
            }

            private Component getMessage(TriState.State state, ChatFormatting format) {
                return switch(state) {
                    case ON -> CommonComponents.OPTION_ON.copy().withStyle(format)
                            .withStyle(ChatFormatting.GREEN);
                    case OFF -> CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED);
                    default -> Component.literal("/").withStyle(ChatFormatting.GRAY);
                };
            }

            private Tooltip getTooltip(TriState.State state) {
                if (state.equals(TriState.State.DISABLED)) return
                        Tooltip.create(localized("option", "notif.format.tooltip"));
                return null;
            }
        }
    }
}
