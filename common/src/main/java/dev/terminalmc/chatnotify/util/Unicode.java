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

@SuppressWarnings("UnnecessaryUnicodeEscape")
public enum Unicode {
    CHECK("\u2714"), // ✔
    CROSS("\u274C"), // ❌
    DOWN("\u2193"), // ↓
    EDIT("\u270E"), // ✎
    INFO("\u2139"), // ℹ
    KEY("\uD83D\uDD11"), // 🔑
    PAINT("\uD83C\uDF22"), // 🌢
    RESET("\u267B"), // ♻
    SECTION("\u00A7"), // §
    SOUND("\uD83D\uDD0A"), // 🔊
    UP("\u2191"), // ↑
    UP_DOWN("\u2191\u2193"); // ↑↓

    public final Character chr;
    public final String str;

    Unicode(String str) {
        this.chr = str.isEmpty() ? null : str.charAt(0);
        this.str = str;
    }
}
