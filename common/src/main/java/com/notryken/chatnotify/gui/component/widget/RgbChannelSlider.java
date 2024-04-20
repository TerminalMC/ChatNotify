/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.component.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/**
 * 0-255 slider for a channel (red, green or blue) of an RGB color. Slider message
 * color will track the slider value.
 */
public class RgbChannelSlider extends DoubleSlider {
    private final IntUnaryOperator toChannel;
    private final IntUnaryOperator fromChannel;

    /**
     * @param source rgb color int source.
     * @param dest rgb color int destination.
     * @param toChannel operator to get the value (0-255) of the slider's
     *                  channel from an RGB int.
     * @param fromChannel operator convert the value (0-255) of the slider's
     *                    channel to an RGB int.
     */
    public RgbChannelSlider(int x, int y, int width, int height,
                            @Nullable String messagePrefix, @Nullable String messageSuffix,
                            Supplier<Integer> source, Consumer<Integer> dest,
                            IntUnaryOperator toChannel, IntUnaryOperator fromChannel) {
        super(x, y, width, height, 0, 255, 0, messagePrefix, messageSuffix,
                null, null,
                () -> (double) toChannel.applyAsInt(source.get()),
                (value) -> dest.accept(fromChannel.applyAsInt(value.intValue())));
        this.toChannel = toChannel;
        this.fromChannel = fromChannel;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double messageValue = round(value * 255, 0);
        String valueStr = String.valueOf(messageValue);
        StringBuilder messageBuilder = new StringBuilder(valueStr);
        if (messagePrefix != null) {
            messageBuilder.insert(0, messagePrefix);
        }
        if (messageSuffix != null) {
            messageBuilder.append(messageSuffix);
        }
        MutableComponent message = Component.literal(messageBuilder.toString());
        if (fromChannel != null) message = message.withStyle(
                Style.EMPTY.withColor(fromChannel.applyAsInt((int) messageValue)));
        setMessage(message);
    }
}
