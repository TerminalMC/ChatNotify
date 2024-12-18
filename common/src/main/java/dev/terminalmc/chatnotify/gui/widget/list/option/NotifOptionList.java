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

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.*;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for options of a {@link Notification}, and buttons linking
 * to other screens.
 */
public class NotifOptionList extends OptionList {
    public static final String[] KEYS = {
            ".",
            "chat.type.",
            "chat.type.admin",
            "chat.type.advancement",
            "chat.type.advancement.challenge",
            "chat.type.advancement.goal",
            "chat.type.advancement.task",
            "chat.type.announcement",
            "chat.type.emote",
            "chat.type.team.sent",
            "chat.type.team.text",
            "chat.type.text",

            "death.",

            "multiplayer.player.joined",
            "multiplayer.player.left",

            "commands.",
            "commands.message.display",
            "commands.message.display.incoming",
            "commands.message.display.outgoing",

            "commands.ban.success",
            "commands.pardon.success",
            "commands.banip.success",
            "commands.pardonip.success",
            "commands.op.success",
            "commands.deop.success",
            "commands.whitelist.add.success",
            "commands.whitelist.remove.success",

            "commands.difficulty.success",
            "commands.gamemode.success",
            "commands.kick.success",
            "commands.summon.success",
            "commands.teleport.success",
    };

    private final Notification notif;
    private int dragSourceSlot = -1;
    private boolean dragHasStyleField = false;

    public NotifOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                           int entryWidth, int entryHeight, Notification notif) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);
        this.notif = notif;
        notif.editing = true;

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif.triggers", "\u2139"),
                Tooltip.create(localized("option", "notif.triggers.tooltip")), -1));

        boolean isUser = notif.equals(Config.get().getNotifs().getFirst());
        for (int i = 0; i < notif.triggers.size(); i++) {
            Trigger trigger = notif.triggers.get(i);
            if (isUser && i <= 1) {
                addEntry(new Entry.TriggerDisplayFieldEntry(dynEntryX, dynEntryWidth, entryHeight,
                        trigger.string, i == 0));
            } else {
                addEntry(new Entry.TriggerFieldEntry(dynEntryX, dynEntryWidth, entryHeight,
                        this, notif, trigger, i));
                if (trigger.styleString != null) {
                    addEntry(new Entry.StyleStringFieldEntry(dynEntryX, dynEntryWidth, entryHeight,
                            this, trigger));
                }
            }
        }
        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    notif.triggers.add(new Trigger());
                    reload();
                }));


        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif.controls"), null, -1));

        addEntry(new Entry.SoundConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.ColorConfigEntry(entryX, entryWidth, entryHeight, this,
                () -> notif.textStyle.color, (val) -> notif.textStyle.color = val,
                () -> notif.textStyle.doColor, (val) -> notif.textStyle.doColor = val,
                localized("option", "notif.color")));
        addEntry(new Entry.FormatConfigEntry(entryX, entryWidth, entryHeight, notif, true));
        addEntry(new Entry.FormatConfigEntry(entryX, entryWidth, entryHeight, notif, false));

        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif.advanced"),
                Tooltip.create(localized("option", "notif.advanced.tooltip")), 500,
                (button) -> openAdvancedConfig()));
    }

    @Override
    public NotifOptionList reload(int width, int height, double scrollAmount) {
        NotifOptionList newList = new NotifOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight, notif);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    @Override
    public void onClose() {
        notif.editing = false;
        notif.autoDisable();
    }

    private void openTriggerConfig(Trigger trigger) {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "trigger"),
                new TriggerOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, trigger, notif.textStyle, "", "", false, true)));
    }

    private void openSoundConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "sound"),
                new SoundOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, notif.sound)));
    }

    private void openAdvancedConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "advanced"),
                new AdvancedOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, notif)));
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
        OptionList.Entry hoveredEntry = getEntryAtPosition(mouseX, mouseY);
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
     * {@link OptionList}.
     */
    private int triggerListOffset() {
        int i = 0;
        for (OptionList.Entry entry : children()) {
            if (entry instanceof Entry.TriggerFieldEntry) return i;
            i++;
        }
        throw new IllegalStateException("Trigger list not found");
    }

    /**
     * @return The number of non-{@link Entry.TriggerFieldEntry} or
     * {@link Entry.TriggerDisplayFieldEntry} entries in the
     * {@link OptionList} before (and including) the specified index.
     */
    private int triggerOffset(int index) {
        int i = 0;
        int offset = 0;
        for (OptionList.Entry entry : children()) {
            if (!(entry instanceof Entry.TriggerFieldEntry
                    || entry instanceof Entry.TriggerDisplayFieldEntry)) offset++;
            if (i++ == index) return offset;
        }
        throw new IllegalStateException("Trigger index out of range");
    }

    abstract static class Entry extends OptionList.Entry {

        private static class TriggerDisplayFieldEntry extends Entry {
            TriggerDisplayFieldEntry(int x, int width, int height, String value, boolean first) {
                super();

                TextField displayField = new TextField(x, 0, width, height);
                displayField.setValue(value);
                displayField.setTooltip(Tooltip.create(first
                        ? localized("option", "notif.trigger.profile_name.tooltip")
                        : localized("option", "notif.trigger.display_name.tooltip")));
                displayField.setTooltipDelay(Duration.ofMillis(500));
                displayField.setEditable(false);
                displayField.active = false;
                elements.add(displayField);
            }
        }

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, NotifOptionList list,
                              Notification notif, Trigger trigger, int index) {
                super();
                int fieldSpacing = 1;
                int triggerFieldWidth = width - (list.tinyWidgetWidth * 3) - (fieldSpacing * 2);
                TextField triggerField = trigger.type == Trigger.Type.KEY
                        ? new FakeTextField(0, 0, triggerFieldWidth, height, () -> {
                            int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                            int wWidth = Math.max(DropdownTextField.MIN_WIDTH, width);
                            int wX = x + (width / 2) - (wWidth / 2);
                            int wY = list.getY();
                            list.screen.setOverlayWidget(new DropdownTextField(
                                    wX, wY, wWidth, wHeight, Component.empty(),
                                    () -> trigger.string, (str) -> trigger.string = str,
                                    (widget) -> {
                                        list.screen.removeOverlayWidget();
                                        list.reload();
                                    }, List.of(KEYS)));
                        })
                        : new TextField(0, 0, triggerFieldWidth, height);
                int movingX = x;

                // Drag reorder button
                elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                (button) -> {
                                    this.setDragging(true);
                                    list.dragSourceSlot = list.children().indexOf(this);
                                    list.dragHasStyleField = trigger.styleString != null;
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
                triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "notif.trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);
                movingX = x + width - list.tinyWidgetWidth * 2;
                
                // Trigger editor button
                Button editorButton = Button.builder(Component.literal("\u270e"),
                                (button) -> list.openTriggerConfig(trigger))
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                editorButton.setTooltip(Tooltip.create(
                        localized("option", "notif.trigger_editor.tooltip")));
                editorButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(editorButton);
                movingX += list.tinyWidgetWidth;

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

                // Delete button
                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    notif.triggers.remove(index);
                                    list.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class StyleStringFieldEntry extends Entry {
            StyleStringFieldEntry(int x, int width, int height, NotifOptionList list,
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
                stringField.setResponder((string) -> trigger.styleString = string.strip());
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

        private static class SoundConfigEntry extends Entry {
            SoundConfigEntry(int x, int width, int height, Notification notif,
                             NotifOptionList list) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int mainButtonWidth = width - statusButtonWidth - SPACING;

                // Sound GUI button
                elements.add(Button.builder(localized("option", "notif.sound", notif.sound.getId()),
                                (button) -> list.openSoundConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                // Status button
                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.sound.isEnabled())
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) -> notif.sound.setEnabled(status)));
            }
        }

        private static class FormatConfigEntry extends Entry {
            private FormatConfigEntry(int x, int width, int height, Notification notif, boolean first) {
                super();
                if (first) createFirst(x, width, height, notif);
                else createSecond(x, width, height, notif);
            }

            // Bold, italic, underline
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

            // Strikethrough, obfuscate
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

        static class ColorConfigEntry extends Entry {
            ColorConfigEntry(int x, int width, int height, OptionList list,
                             Supplier<Integer> supplier, Consumer<Integer> consumer,
                             MutableComponent text) {
                this(x, width, height, list, supplier, consumer, () -> false, (val) -> {}, text, false);
            }

            ColorConfigEntry(int x, int width, int height, OptionList list,
                             Supplier<Integer> supplier, Consumer<Integer> consumer,
                             Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                             MutableComponent text) {
                this(x, width, height, list, supplier, consumer, statusSupplier, statusConsumer, text, true);
            }

            ColorConfigEntry(int x, int width, int height, OptionList list,
                             Supplier<Integer> supplier, Consumer<Integer> consumer,
                             Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                             MutableComponent text, boolean showStatusButton) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int colorFieldWidth = Minecraft.getInstance().font.width("#FFAAFF+++");
                int mainButtonWidth = width - colorFieldWidth - SPACING;
                if (showStatusButton) mainButtonWidth -= (statusButtonWidth + SPACING);

                // Color GUI button
                Button mainButton = Button.builder(text.withColor(supplier.get()),
                                (button) -> {
                                    int cpHeight = HsvColorPicker.MIN_HEIGHT;
                                    int cpWidth = Math.max(HsvColorPicker.MIN_WIDTH, width);
                                    list.screen.setOverlayWidget(new HsvColorPicker(
                                            x, list.screen.height / 2 - cpHeight / 2, cpWidth, cpHeight,
                                            Component.empty(), supplier, consumer,
                                            (widget) -> {
                                                list.screen.removeOverlayWidget();
                                                list.reload();
                                            }));
                                })
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                // Hex code field
                TextField colorField = new TextField(x + mainButtonWidth + SPACING, 0,
                        colorFieldWidth, height);
                colorField.hexColorValidator();
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = MiscUtil.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        consumer.accept(color);
                        // Update color of main button and field
                        mainButton.setMessage(mainButton.getMessage().copy().withColor(color));
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                                FastColor.ARGB32.blue(color), hsv);
                        if (hsv[2] < 0.1) colorField.setTextColor(16777215);
                        else colorField.setTextColor(color);
                    }
                });
                colorField.setValue(TextColor.fromRgb(supplier.get()).formatValue());
                elements.add(colorField);

                if (showStatusButton) {
                    // Status button
                    elements.add(CycleButton.booleanBuilder(
                                    CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                    CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                            .displayOnlyValue()
                            .withInitialValue(statusSupplier.get())
                            .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                    Component.empty(), (button, status) -> statusConsumer.accept(status)));
                }
            }
        }
    }
}
