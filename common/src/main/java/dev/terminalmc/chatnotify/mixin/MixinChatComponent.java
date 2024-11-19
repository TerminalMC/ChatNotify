/*
 * Copyright 2024 TerminalMC
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

package dev.terminalmc.chatnotify.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.util.MessageProcessor;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Minecraft handles different message packet types in different ways. The
 * mc1.20.1 call stacks look something like this:
 *
 * <p>{@link ClientPacketListener#handleDisguisedChat}
 * <p>-> {@link ChatListener#handleDisguisedChatMessage}
 * <p>-> {@link ChatComponent#addMessage(Component)}
 * <p>-> {@link ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)}
 *
 * <p>{@link ClientPacketListener#handleSystemChat}
 * <p>-> {@link ChatListener#handleSystemMessage}
 * <p>-> {@link ChatComponent#addMessage(Component)}
 * <p>-> {@link ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)}
 *
 * <p>{@link ClientPacketListener#handlePlayerChat}
 * <p>-> {@link ChatListener#handlePlayerChatMessage}
 * <p>-> {@link ChatListener#showMessageToPlayer}
 * <p>-> {@link ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)}
 *
 * <p>{@link ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)}
 * logs the message and adds it to the message queues.
 */
@Mixin(value = ChatComponent.class, priority = 792)
public class MixinChatComponent {

    @WrapMethod(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    private void replaceMessage(Component message, MessageSignature headerSignature, 
                                GuiMessageTag tag, Operation<Void> original) {
        message = chatNotify$replaceMessage(message);
        if (message != null) original.call(message, headerSignature, tag);
    }

    @Unique
    private static @Nullable Component chatNotify$replaceMessage(Component message) {
        if (ChatNotify.mixinEarly()) {
            return message;
        } else {
            return MessageProcessor.processMessage(message);
        }
    }
}
