/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify;

import com.mojang.datafixers.util.Pair;
import com.notryken.chatnotify.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatNotify {
    // Constants
    public static final String MOD_ID = "chatnotify";
    public static final String MOD_NAME = "ChatNotify";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final List<Pair<Long, String>> recentMessages = new ArrayList<>();

    // Config management
    private static Config CONFIG;

    public static void init() {
        CONFIG = Config.load();
    }

    public static Config config() {
        if (CONFIG == null) {
            throw new IllegalStateException("ChatNotify: Config not yet available");
        }
        return CONFIG;
    }

    public static void restoreDefaultConfig() {
        CONFIG = new Config();
        CONFIG.writeToFile();
    }
}