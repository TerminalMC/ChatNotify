package notryken.chatnotify.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.util.MessageProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatComponent.class)
public class MixinChatComponent {
    @ModifyArg(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"),
            index = 0)
    private Component replaceMessage(Component message) {
        return MessageProcessor.processMessage(message);
    }
}
