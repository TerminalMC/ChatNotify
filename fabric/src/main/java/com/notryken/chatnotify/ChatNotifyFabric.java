/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public class ChatNotifyFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ChatNotify::onEndTick);
        boolean hasChatHistoryMod = FabricLoader.getInstance().isModLoaded("chatpatches");
        ChatNotify.init(hasChatHistoryMod);
    }
}
