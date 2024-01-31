package notryken.chatnotify.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.processor.MessageProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
 * MessageSignature, int, GuiMessageTag, boolean). The latter method is also
 * used for chat refresh, so the former method is the only suitable method
 * common to all incoming message routes.
 */
@Mixin(value = ChatComponent.class)
public class MixinChatComponent {
    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"),
            index = 0)
    private Component replaceMessage(Component message) {
        return MessageProcessor.processMessage(message);
    }
}
