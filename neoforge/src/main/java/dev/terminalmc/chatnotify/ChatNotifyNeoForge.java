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

import dev.terminalmc.chatnotify.gui.screen.MainOptionsScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ChatNotify.MOD_ID, dist = Dist.CLIENT)
public class ChatNotifyNeoForge {
    public ChatNotifyNeoForge() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (minecraft, parent) -> new MainOptionsScreen(parent));
        
        ChatNotify.init();
    }

    @EventBusSubscriber(modid = ChatNotify.MOD_ID, value = Dist.CLIENT)
    static class ClientEventHandler {
        @SubscribeEvent
        public static void clientTickEvent(ClientTickEvent.Post event) {
            ChatNotify.onEndTick(Minecraft.getInstance());
        }
    }
}
