/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.component.listwidget;

import com.notryken.chatnotify.gui.component.widget.RgbChannelSlider;
import com.notryken.chatnotify.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/**
 * Contains channel sliders and quick-select buttons for an RGB color.
 */
public class ColorOptionsListWidget extends OptionsListWidget {
    public final Supplier<Integer> src;
    public final Consumer<Integer> dest;

    public ColorOptionsListWidget(Minecraft mc, int width, int height, int top, int bottom,
                                  int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                                  int scrollWidth, Supplier<Integer> src, Consumer<Integer> dest) {
        super(mc, width, height, top, bottom, itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth);
        this.src = src;
        this.dest = dest;

        addEntry(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Example Text")
                        .setStyle(Style.EMPTY.withColor(src.get())),
                null, -1));

        addEntry(new Entry.RgbSliderEntry(entryX, entryWidth, entryHeight, "Red: ", src,
                (color) -> {
                    dest.accept(ColorUtil.withRed.applyAsInt(src.get(), color));
                    refreshColorIndicator();
                },
                ColorUtil.toRed, ColorUtil.fromRed));
        addEntry(new Entry.RgbSliderEntry(entryX, entryWidth, entryHeight, "Green: ", src,
                (color) -> {
                    dest.accept(ColorUtil.withGreen.applyAsInt(src.get(), color));
                    refreshColorIndicator();
                },
                ColorUtil.toGreen, ColorUtil.fromGreen));
        addEntry(new Entry.RgbSliderEntry(entryX, entryWidth, entryHeight, "Blue: ", src,
                (color) -> {
                    dest.accept(ColorUtil.withBlue.applyAsInt(src.get(), color));
                    refreshColorIndicator();
                },
                ColorUtil.toBlue, ColorUtil.fromBlue));

        addEntry(new Entry.ColorSelectionEntry(entryX, entryWidth, dest, this));
    }

    @Override
    public ColorOptionsListWidget resize(int width, int height, int top, int bottom,
                                         int itemHeight, double scrollAmount) {
        ColorOptionsListWidget newListWidget = new ColorOptionsListWidget(
                minecraft, width, height, top, bottom, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth, src, dest);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    public void refreshColorIndicator() {
        remove(0);
        addEntryToTop(new OptionsListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Text Color")
                        .setStyle(Style.EMPTY.withColor(src.get())),
                null, -1));
    }

    private abstract static class Entry extends OptionsListWidget.Entry {

        protected static class RgbSliderEntry extends Entry {
            public RgbSliderEntry(int x, int width, int height, @Nullable String message,
                                  Supplier<Integer> source, Consumer<Integer> dest,
                                  IntUnaryOperator toChannel, IntUnaryOperator fromChannel) {
                super();
                elements.add(new RgbChannelSlider(x, 0, width, height, message, null,
                        source, dest, toChannel, fromChannel));
            }
        }

        protected static class ColorSelectionEntry extends Entry {
            int[] colors = new int[] {
                    10027008,
                    16711680,
                    16753920,
                    16761856,
                    16776960,
                    65280,
                    32768,
                    19456,
                    2142890,
                    65535,
                    255,
                    8388736,
                    16711935,
                    16777215,
                    8421504,
                    0};

            public ColorSelectionEntry(int x, int width, Consumer<Integer> dest,
                                       ColorOptionsListWidget listWidget) {
                super();

                int buttonWidth = width / colors.length;
                for (int i = 0; i < colors.length; i++) {
                    int color = colors[i];
                    int setX = x + (width / 2) - (buttonWidth * colors.length / 2);
                    elements.add(Button.builder(Component.literal("\u2588")
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))), (button) ->
                            {
                                dest.accept(color);
                                listWidget.reload();
                            })
                            .pos(setX + (buttonWidth * i), 0)
                            .size(buttonWidth, buttonWidth)
                            .build());
                }
            }
        }
    }
}