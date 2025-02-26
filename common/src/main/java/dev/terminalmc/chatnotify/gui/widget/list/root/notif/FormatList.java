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

package dev.terminalmc.chatnotify.gui.widget.list.root.notif;

import dev.terminalmc.chatnotify.config.*;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.FastColor;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class FormatList extends OptionList {
    private final Notification notif;

    public FormatList(Minecraft mc, int width, int height, int top, int bottom, int entryWidth,
                      int entryHeight, int entrySpacing, Notification notif) {
        super(mc, width, height, top, bottom, entryWidth, entryHeight, entrySpacing);
        this.notif = notif;
    }

    @Override
    protected void addEntries() {
        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "notif.format.list", "\u2139"),
                Tooltip.create(localized("option", "notif.format.list.tooltip")), -1));

        addEntry(new Entry.ColorOptions(entryX, entryWidth, entryHeight, this,
                () -> notif.textStyle.color, (val) -> notif.textStyle.color = val,
                () -> notif.textStyle.doColor, (val) -> notif.textStyle.doColor = val,
                localized("option", "notif.format.color")));

        addEntry(new Entry.FormatOptions(entryX, entryWidth, entryHeight, notif, true));
        addEntry(new Entry.FormatOptions(entryX, entryWidth, entryHeight, notif, false));
    }

    // Utility

    private void initList() {
        this.init();
    }

    // Custom entries

    abstract static class Entry extends OptionList.Entry {

        private static class ColorOptions extends Entry {
            ColorOptions(int x, int width, int height, FormatList list,
                         Supplier<Integer> supplier, Consumer<Integer> consumer,
                         Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                         MutableComponent text) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int colorFieldWidth = Minecraft.getInstance().font.width("#FFAAFF+++");
                int mainButtonWidth = width - colorFieldWidth - statusButtonWidth - SPACE * 2;

                // Color GUI button
                Button mainButton = Button.builder(text.withStyle(Style.EMPTY.withColor(supplier.get())),
                                (button) -> {
                                    int cpHeight = HsvColorPicker.MIN_HEIGHT;
                                    int cpWidth = HsvColorPicker.MIN_WIDTH;
                                    list.getScreen().setOverlayWidget(new HsvColorPicker(
                                            x + width / 2 - cpWidth / 2,
                                            list.getScreen().height / 2 - cpHeight / 2,
                                            cpWidth, cpHeight,
                                            supplier, consumer,
                                            (widget) -> list.initList()));
                                })
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                // Hex code field
                TextField colorField = new TextField(x + mainButtonWidth + SPACE, 0,
                        colorFieldWidth, height);
                colorField.hexColorValidator().strict();
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = TextColor.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        consumer.accept(color);
                        // Update color of main button and field
                        mainButton.setMessage(mainButton.getMessage().copy().withStyle(Style.EMPTY.withColor(color)));
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                                FastColor.ARGB32.blue(color), hsv);
                        if (hsv[2] < 0.1) colorField.setTextColor(0xFFFFFF);
                        else colorField.setTextColor(color);
                    }
                });
                colorField.setValue(TextColor.fromRgb(supplier.get()).formatValue());
                elements.add(colorField);

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

        private static class FormatOptions extends Entry {
            private FormatOptions(int x, int width, int height, Notification notif, boolean first) {
                super();
                if (first) createFirst(x, width, height, notif);
                else createSecond(x, width, height, notif);
            }

            // Bold, italic, underline
            private void createFirst(int x, int width, int height, Notification notif) {
                int buttonWidth = (width - SPACE * 2) / 3;

                CycleButton<TextStyle.FormatMode> boldButton =
                        CycleButton.<TextStyle.FormatMode>builder(
                                        (state) -> getMessage(state, ChatFormatting.BOLD))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.bold)
                                .withTooltip(this::getTooltip)
                                .create(x, 0, buttonWidth, height,
                                        localized("option", "notif.format.bold"),
                                        (button, state) -> notif.textStyle.bold = state);
                boldButton.setTooltipDelay(500);
                elements.add(boldButton);

                CycleButton<TextStyle.FormatMode> italicButton =
                        CycleButton.<TextStyle.FormatMode>builder(
                                        (state) -> getMessage(state, ChatFormatting.ITALIC))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.italic)
                                .withTooltip(this::getTooltip)
                                .create(x + width / 2 - buttonWidth / 2, 0, buttonWidth, height,
                                        localized("option", "notif.format.italic"),
                                        (button, state) -> notif.textStyle.italic = state);
                italicButton.setTooltipDelay(500);
                elements.add(italicButton);

                CycleButton<TextStyle.FormatMode> underlineButton =
                        CycleButton.<TextStyle.FormatMode>builder(
                                        (state) -> getMessage(state, ChatFormatting.UNDERLINE))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.underlined)
                                .withTooltip(this::getTooltip)
                                .create(x + width - buttonWidth, 0, buttonWidth, height,
                                        localized("option", "notif.format.underline"),
                                        (button, state) -> notif.textStyle.underlined = state);
                underlineButton.setTooltipDelay(500);
                elements.add(underlineButton);
            }

            // Strikethrough, obfuscate
            private void createSecond(int x, int width, int height, Notification notif) {
                int buttonWidth = (width - SPACE) / 2;

                CycleButton<TextStyle.FormatMode> strikethroughButton =
                        CycleButton.<TextStyle.FormatMode>builder(
                                        (state) -> getMessage(state, ChatFormatting.STRIKETHROUGH))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.strikethrough)
                                .withTooltip(this::getTooltip)
                                .create(x, 0, buttonWidth, height,
                                        localized("option", "notif.format.strikethrough"),
                                        (button, state) -> notif.textStyle.strikethrough = state);
                strikethroughButton.setTooltipDelay(500);
                elements.add(strikethroughButton);

                CycleButton<TextStyle.FormatMode> obfuscateButton =
                        CycleButton.<TextStyle.FormatMode>builder(
                                        (state) -> getMessage(state, ChatFormatting.OBFUSCATED))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.obfuscated)
                                .withTooltip(this::getTooltip)
                                .create(x + width - buttonWidth, 0, buttonWidth, height,
                                        localized("option", "notif.format.obfuscate"),
                                        (button, state) -> notif.textStyle.obfuscated = state);
                obfuscateButton.setTooltipDelay(500);
                elements.add(obfuscateButton);
            }

            private Component getMessage(TextStyle.FormatMode mode, ChatFormatting format) {
                return switch(mode) {
                    case ON -> CommonComponents.OPTION_ON.copy().withStyle(format)
                            .withStyle(ChatFormatting.GREEN);
                    case OFF -> CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED);
                    default -> Component.literal("/").withStyle(ChatFormatting.GRAY);
                };
            }

            private Tooltip getTooltip(TextStyle.FormatMode mode) {
                return mode.equals(TextStyle.FormatMode.DISABLED)
                        ? Tooltip.create(localized("option", "notif.format.disabled.tooltip"))
                        : null;
            }
        }
    }
}
