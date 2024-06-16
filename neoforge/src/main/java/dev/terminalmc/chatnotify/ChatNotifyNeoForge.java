/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify;

import dev.terminalmc.chatnotify.gui.screen.GlobalOptionsScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ChatNotify.MOD_ID)
public class ChatNotifyNeoForge {
    public ChatNotifyNeoForge() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (minecraft, parent) -> new GlobalOptionsScreen(parent));

//        boolean hasChatHistoryMod = ModList.get().isLoaded("");
        ChatNotify.init(false);
    }

    @EventBusSubscriber(modid = ChatNotify.MOD_ID, value = Dist.CLIENT)
    static class ClientEventHandler {
        @SubscribeEvent
        public static void clientTickEvent(ClientTickEvent.Post event) {
            ChatNotify.onEndTick(Minecraft.getInstance());
        }
    }
}
