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

package dev.terminalmc.chatnotify.util;

import net.minecraft.network.chat.TextColor;

import java.util.Optional;

public class ColorUtil {
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
