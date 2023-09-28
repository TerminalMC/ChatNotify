package notryken.chatnotify.mixin;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import notryken.chatnotify.util.MessageProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MessageHandler.class)
public class MixinMessageHandler
{
    @ModifyArg(method = "processChatMessageInternal", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(" +
                    "Lnet/minecraft/text/Text;" +
                    "Lnet/minecraft/network/message/MessageSignatureData;" +
                    "Lnet/minecraft/client/gui/hud/MessageIndicator;)V"),
            index = 0)
    private Text replaceMessage(Text message)
    {
        return MessageProcessor.processMessage(message);
    }
}
