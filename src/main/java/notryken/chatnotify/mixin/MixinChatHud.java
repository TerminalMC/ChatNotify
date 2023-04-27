package notryken.chatnotify.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import notryken.chatnotify.config.NotifyOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static notryken.chatnotify.client.ChatNotifyClient.*;

@Mixin(ChatHud.class)
public class MixinChatHud {

    private List<String> wordVariations = null;
    private Text modifiedMessage = null;
    private boolean messageIsModified = false;


    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"))
    private void addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci)
    {
        for(NotifyOption option : config.optionList.values())
        {
            if(!messageIsModified) { // Stop when one option matches (don't modify multiple times)
                modifiedMessage = notify(message, option);
            }
        }
    }


    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"))
    private void replaceMessage(Args args)
    {
        if(modifiedMessage != null)
        {
            args.set(0, modifiedMessage);
        }
        modifiedMessage = null;
    }

    private String msgContainsStr(String msg, String str)
    {
        if(wordVariations == null)
        {
            wordVariations = new ArrayList<>();
            wordVariations.add(str);
            wordVariations.add("@" + str);
            wordVariations.add("'" + str + "'");
            wordVariations.add('"' + str + '"');

        }
        String[] splitMessage = msg.split(" ");

        /* FIXME doesn't currently support checking the first part of the
        *   message, because that is ignored by the player name check. */
        for(int i = 1; i < splitMessage.length; i++) // Ignore first part;
        {
            for(String word : wordVariations)
            {
                if(splitMessage[i].equalsIgnoreCase(word))
                {
                    return word;
                }
            }
        }
        return null;
    }

    private Text notify(Text message, NotifyOption option)
    {
        String matchedStr = msgContainsStr(message.getString(), option.getWord());

        if (matchedStr != null) {
            MutableText newMessage = MutableText.of(message.getContent());
            Style style = Style.of(
                    Optional.of(TextColor.fromRgb(option.getColor())),
                    Optional.of(option.getBold()),
                    Optional.of(option.getItalic()),
                    Optional.of(option.getUnderlined()),
                    Optional.of(option.getStrikethrough()),
                    Optional.of(option.getObfuscated()),
                    Optional.empty(),
                    Optional.empty());
            newMessage.setStyle(style);

            // Required for correct display on servers.
            List<Text> siblings = message.getSiblings();
            newMessage.siblings.addAll(siblings);

            SoundManager soundManager = client.getSoundManager();
            soundManager.play(new PositionedSoundInstance(
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.getId(),
                    SoundCategory.PLAYERS, 1f, 1f, SoundInstance.createRandom(),
                    false, 0, SoundInstance.AttenuationType.NONE, 0, 0, 0,
                    true));

            messageIsModified = true;
            return newMessage;
        }
        return message;
    }
}
