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

package dev.terminalmc.chatnotify;

import dev.terminalmc.chatnotify.command.Commands;
import dev.terminalmc.chatnotify.gui.screen.RootScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod(value = ChatNotify.MOD_ID)
public class ChatNotifyForge {

    public ChatNotifyForge() {
        // Register config screen
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new RootScreen(parent))
        );

        // Initialize client
        ChatNotify.init();
    }

    @EventBusSubscriber(
            modid = ChatNotify.MOD_ID,
            value = Dist.CLIENT
    )
    static class ClientEventHandler {

        /**
         * Registers client after-tick event.
         */
        @SubscribeEvent
        public static void clientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase.equals(TickEvent.Phase.END)) {
                ChatNotify.afterClientTick(Minecraft.getInstance());
            }
        }

        /**
         * Registers all commands.
         */
        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            Commands.register(event.getDispatcher(), event.getBuildContext());
        }
    }
}
