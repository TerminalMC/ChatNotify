/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.util;

import net.minecraft.network.chat.TextColor;

import java.util.Optional;

/**
 * It's okay to dislike the idea of a miscellaneous utilities class, but for the
 * moment you'll have to deal with it.
 */
public class MiscUtil {
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
