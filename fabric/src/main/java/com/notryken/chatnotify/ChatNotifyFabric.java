/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify;

import net.fabricmc.api.ClientModInitializer;

public class ChatNotifyFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ChatNotify.init();
    }
}