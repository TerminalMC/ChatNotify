/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.util;

import dev.terminalmc.chatnotify.ChatNotify;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Localization {
    public static String translationKey(String domain, String path) {
        return domain + "." + ChatNotify.MOD_ID + "." + path;
    }

    public static MutableComponent localized(String domain, String path, Object... args) {
        return Component.translatable(translationKey(domain, path), args);
    }
}
