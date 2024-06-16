/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.mixin;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.processor.MessageProcessor;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Refer to {@link MixinChatComponent} for an overview of Minecraft's message
 * handling call stacks.
 */
@Mixin(value = ChatListener.class, priority = 800)
public class MixinChatListener {

    @ModifyVariable(
            method = "handleDisguisedChatMessage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component replaceDisguisedChatMessage(Component message) {
        return chatNotify$replaceMessage(message);
    }

    @ModifyVariable(
            method = "handleSystemMessage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component replaceSystemMessage(Component message) {
        return chatNotify$replaceMessage(message);
    }

    // Unable to use handlePlayerChatMessage as that takes a PlayerChatMessage.
    @ModifyVariable(
            method = "showMessageToPlayer",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component replaceMessageToPlayer(Component message) {
        return chatNotify$replaceMessage(message);
    }


    @Unique
    private static Component chatNotify$replaceMessage(Component message) {
        if (ChatNotify.mixinEarly()) {
            return MessageProcessor.processMessage(message);
        } else {
            return message;
        }
    }
}
