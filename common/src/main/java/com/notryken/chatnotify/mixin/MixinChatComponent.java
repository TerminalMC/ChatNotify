package com.notryken.chatnotify.mixin;

import com.notryken.chatnotify.ChatNotify;
import com.notryken.chatnotify.processor.MessageProcessor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/*
 * Minecraft handles different message packet types in different ways. The
 * mc1.20.1 call stack look something like this:
 * <p>
 * ClientPacketListener#handleDisguisedChat
 * -> ChatListener#handleDisguisedChatMessage
 * -> ChatComponent#addMessage(Component)
 * -> ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)
 * <p>
 * ClientPacketListener#handleSystemChat
 * -> ChatListener#handleSystemMessage
 * -> ChatComponent#addMessage(Component)
 * -> ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)
 * <p>
 * ClientPacketListener#handlePlayerChat
 * -> ChatListener#handlePlayerChatMessage
 * -> ChatListener#showMessageToPlayer
 * -> ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag)
 * <p>
 * ChatComponent#addMessage(Component, MessageSignature, GuiMessageTag) logs
 * the message and then passes it to ChatComponent#addMessage(Component,
 * MessageSignature, int, GuiMessageTag, boolean).
 */

@Mixin(value = ChatComponent.class, priority = 800)
public class MixinChatComponent {

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private Component replaceMessage(Component message) {
        if (!ChatNotify.config().mixinEarly) {
            return MessageProcessor.processMessage(message);
        }
        else {
            return message;
        }
    }
}
