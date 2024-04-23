/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify;

import com.notryken.chatnotify.gui.screen.GlobalOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(ChatNotify.MOD_ID)
@Mod.EventBusSubscriber(modid = ChatNotify.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChatNotifyForge {
    public ChatNotifyForge() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new GlobalOptionsScreen(parent))
                );

        ChatNotify.init();
    }

    @Mod.EventBusSubscriber(modid = ChatNotify.MOD_ID, value = Dist.CLIENT)
    static class ClientEventHandler {
        @SubscribeEvent
        public static void clientTickEvent(TickEvent.ClientTickEvent event) {
            if(event.phase.equals(TickEvent.Phase.END)) {
                ChatNotify.onEndTick(Minecraft.getInstance());
            }
        }
    }
}
