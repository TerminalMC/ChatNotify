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

package dev.terminalmc.chatnotify.platform;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.command.ChatNotifyCommand;
import dev.terminalmc.chatnotify.platform.services.ICommandRegistrar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(
    modid = ChatNotify.MOD_ID,
    value = Dist.CLIENT
)
public class NeoForgeCommandRegistrar implements ICommandRegistrar {
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ChatNotifyCommand.register(event.getDispatcher());
    }

    // Keep this to satisfy the common call
    @Override
    public void registerAll() {
        // No-op: registration happens automatically
    }
}
