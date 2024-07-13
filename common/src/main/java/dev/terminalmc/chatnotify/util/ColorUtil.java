/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.util;

import net.minecraft.network.chat.TextColor;

import java.util.Optional;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class ColorUtil {
    // RGB color channel operators
    public static final IntUnaryOperator toRed = (value) -> (value >> 16 & 255);
    public static final IntUnaryOperator toGreen = (value) -> (value >> 8 & 255);
    public static final IntUnaryOperator toBlue = (value) -> (value & 255);
    public static final IntUnaryOperator fromRed = (value) -> (value * 65536);
    public static final IntUnaryOperator fromGreen = (value) -> (value * 256);
    public static final IntUnaryOperator fromBlue = (value) -> (value);
    public static final IntBinaryOperator withRed = (value, red) ->
            (red + value - (value >> 16 & 255) * 65536);
    public static final IntBinaryOperator withGreen = (value, green) ->
            (green + value - (value >> 8 & 255) * 256);
    public static final IntBinaryOperator withBlue = (value, blue) ->
            (blue + value - (value & 255));


    /**
     * Converts a hex color string into a {@link TextColor}.
     * @param str a full-length RGB hex string with leading #
     *                 (7 chars total).
     * @return the resulting {@link TextColor} if the string is a valid color,
     * {@code null} otherwise.
     */
    public static TextColor parseColor(String str) {
        if (str.startsWith("#") && str.length() == 7) {
            Optional<TextColor> result = TextColor.parseColor(str).result();
            if (result.isPresent()) return result.get();
        }
        return null;
    }
}
