package notryken.chatnotify.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import notryken.chatnotify.client.ChatNotifyClient;
import notryken.chatnotify.config.Notification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The backbone of ChatNotify. Intercepts chat messages as they are sent to
 * the list to be displayed, checks whether they contain any words matching
 * any of the loaded Notification objects, and if so, modifies the color and
 * formatting as specified by the relevant Notification, and plays the
 * specified notification sound.
 */
@Mixin(ChatHud.class)
public class MixinChatHud {

    SoundManager soundManager = ChatNotifyClient.client.getSoundManager();
    private Text modifiedMessage = null;
    private boolean messageIsModified;

    /**
     * Intercepts chat messages as they are sent to the list to be displayed.
     */
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;" +
            "Lnet/minecraft/network/message/MessageSignatureData;" +
            "ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            at = @At("HEAD"))
    private void addMessage(Text message, MessageSignatureData signature,
                            int ticks, MessageIndicator indicator,
                            boolean refresh, CallbackInfo ci)
    {
        if (!refresh) // Only process new chat messages.
        {
            messageIsModified = false; // Reset.
            for (Notification option :
                    ChatNotifyClient.config.getNotifications().values())
            {
                // Don't modify the message multiple times.
                if (!messageIsModified) {
                    modifiedMessage = notify(message, option);
                }
            }
        }
    }


    /**
     * Calls msgContainsStr() with the message and the Notification's word field
     * to check whether the user should be notified. If so, makes a modified
     * copy of the message using the Notification data and plays the relevant
     * notification sound.
     * @param message The chat message.
     * @param option The Notification to use.
     * @return The modified message.
     */
    private Text notify(Text message, Notification option)
    {
        if (msgContainsStr(message.getString(), option.getTrigger())) {
            MutableText newMessage = MutableText.of(message.getContent());

            Style style = Style.of(
                    Optional.of(TextColor.fromRgb(
                            Integer.parseInt(option.getColor()))),
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

            if (option.getPlaySound()) {
                soundManager.play(new PositionedSoundInstance(
                        option.getSound().getId(), SoundCategory.PLAYERS,
                        1f, 1f, SoundInstance.createRandom(), false, 0,
                        SoundInstance.AttenuationType.NONE, 0, 0, 0, true));
            }

            messageIsModified = true;
            return newMessage;
        }
        return message;
    }

    /**
     * Uses regex pattern matching to check whether msg contains str.
     * Specifically, matches "(?<\!\w)(\W?(?i)" + str + "\W?)(?!\w)",
     * with two exceptions, if the message was sent by the user (starts with
     * their in-game name). If the global config is to ignore those messages,
     * it will simply return false. Otherwise, the subject "str" in the regex
     * will be modified with the first occurrence of the user's in-game name
     * removed. This is done to avoid name-matching every message sent by the
     * user, as the 'notify when someone says my name' option defaults to true.
     * @param msg The message to search in.
     * @param str The string to search for.
     * @return Whether the string was found in the message, according to the
     * regex matching.
     */
    private boolean msgContainsStr(String msg, String str)
    {
        String username = ChatNotifyClient.username;

        Pattern pattern = Pattern.compile(
                "(?<!\\w)(\\W?(?i)" + str + "\\W?)(?!\\w)");

        /* Identifies a message as being sent by the user if their name appears
        as the first word of the message.
         */
        boolean ownMessage =
                Pattern.compile("(?<!\\w)(\\W?(?i)" + username +
                        "\\W?)(?!\\w)").matcher(msg.split(" ")[0]).find();

        if (ChatNotifyClient.config.ignoreOwnMessages) {
            // Only process the message if it is not from the user.
            if (!ownMessage) {
                return pattern.matcher(msg).find();
            }
        }
        else {
            if (ownMessage) {
                /* Don't check the first word of the message (avoids
                name-matching every message sent by the user).
                */
                return pattern.matcher(msg.replaceFirst(username, "")).find();
            }
            else {
                /* Otherwise, check the whole message (allows notification
                when another specified player says something).
                 */
                return pattern.matcher(msg).find();
            }
        }
        return false;
    }

    /**
     * If the current chat message has been modified, replaces the existing
     * version with the modified one.
     */
    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;" +
            "Lnet/minecraft/network/message/MessageSignatureData;I" +
            "Lnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/util/ChatMessages;" +
                            "breakRenderedChatMessageLines(" +
                            "Lnet/minecraft/text/StringVisitable;I" +
                            "Lnet/minecraft/client/font/TextRenderer;)" +
                            "Ljava/util/List;"))
    private void replaceMessage(Args args)
    {
        if (modifiedMessage != null)
        {
            args.set(0, modifiedMessage);
        }
        modifiedMessage = null;
    }
}