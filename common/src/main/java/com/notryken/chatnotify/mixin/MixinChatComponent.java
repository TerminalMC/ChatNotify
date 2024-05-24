/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.mixin;

import com.notryken.chatnotify.ChatNotify;
import com.notryken.chatnotify.processor.MessageProcessor;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
@Mixin(value = ChatComponent.class, priority = 800)
public class MixinChatComponent {

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component replaceMessage(Component message) {
        if (ChatNotify.mixinEarly()) {
            return message;
        } else {
            return MessageProcessor.processMessage(message);
        }
    }
}
