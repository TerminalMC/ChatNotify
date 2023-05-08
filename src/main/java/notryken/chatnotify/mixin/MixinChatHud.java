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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static notryken.chatnotify.client.ChatNotifyClient.config;
import static notryken.chatnotify.client.ChatNotifyClient.lastSentMessage;

/**
 * The backbone of ChatNotify. Intercepts chat messages as they are sent to
 * the list to be displayed, checks whether they contain any words matching
 * any of the loaded Notification objects, and if so, modifies the color and
 * formatting as specified by the relevant Notification, and plays the
 * specified notification sound.
 */
@Mixin(ChatHud.class)
public class MixinChatHud {

    private final SoundManager soundManager =
            ChatNotifyClient.client.getSoundManager();
    private Text modifiedMessage = null;


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
        String username = config.getUsername();
        if (username != null) {
            boolean mute = false;
            String strMsg = message.getString();

            if (refresh) {
                // Avoid repeat pinging, but maintain message coloring.
                mute = true;
            }

            strMsg = preProcess(strMsg, username);

            if (strMsg != null) {
                checkNotifications(message, strMsg, mute);
            }
        }
    }


    private String preProcess(String strMsg, String username)
    {
        if (lastSentMessage != null) {
            if (strMsg.contains(username) && strMsg.contains(lastSentMessage)) {
                lastSentMessage = null;
                if (config.getIgnoreOwnMsg()) {
                    strMsg = null;
                }
                else {
                    strMsg = strMsg.replaceFirst(username, "");
                }
            }
        }
        return strMsg;
    }


    private void checkNotifications(Text message, String strMsg, boolean mute)
    {
        modifiedMessage = null; // Reset.
        for (Notification notif : config.getNotifications()) {
            if (modifiedMessage == null) { // Don't modify multiple times.
                boolean doNotify;

                if (notif.isKeyTrigger()) {
                    if (message.getContent() instanceof TranslatableTextContent ttc) {
                        doNotify = ttc.getKey().contains(notif.getTrigger());
                    }
                    else {
                        doNotify = false;
                    }
                }
                else {
                    doNotify = msgContainsStr(strMsg, notif.getTrigger());
                }

                if (doNotify) {
                    notify(message, notif, mute);
                }
            }
        }
    }



    private void notify(Text message, Notification notif, boolean mute)
    {
        MutableText newMessage = MutableText.of(message.getContent());

        Style style = Style.of(
                Optional.of(TextColor.fromRgb(notif.getColor())),
                Optional.of(notif.isBold()),
                Optional.of(notif.isItalic()),
                Optional.of(notif.isUnderlined()),
                Optional.of(notif.isStrikethrough()),
                Optional.of(notif.isObfuscated()),
                Optional.empty(),
                Optional.empty());

        // Remove server-enforced formatting codes.
        // Could possibly target the sibling that contains the trigger,
        // and only remove the format code for that one.
        // FIXME?
        List<Text> siblings = message.getSiblings();
        List<Text> newSiblings = new ArrayList<>();
        for (Text t : siblings)
        {
            newSiblings.add(Text.of(TextVisitFactory.removeFormattingCodes(StringVisitable.plain(t.getString()))));
        }
        newMessage.siblings.addAll(newSiblings);

        newMessage.setStyle(style);

        if (!mute) {
            soundManager.play(new PositionedSoundInstance(
                    notif.getSound(), SoundCategory.PLAYERS,
                    notif.getSoundVolume(),
                    notif.getSoundPitch(),
                    SoundInstance.createRandom(), false, 0,
                    SoundInstance.AttenuationType.NONE, 0, 0, 0, true));
        }
        modifiedMessage = newMessage;
    }

    /**
     * Uses regex pattern matching to check whether msg contains str.
     * Specifically, matches {@code "(?<!\w)(\W?(?i)" + str + "\W?)(?!\w)"}.
     * @param msg The message to search in.
     * @param str The string to search for.
     * @return Whether the string was found in the message, according to the
     * regex matching.
     */
    private boolean msgContainsStr(String msg, String str)
    {
        Pattern pattern = Pattern.compile(
                "(?<!\\w)(\\W?(?i)" + str + "\\W?)(?!\\w)");
        return pattern.matcher(msg).find();
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
        if (modifiedMessage != null) {
            args.set(0, modifiedMessage);
            modifiedMessage = null;
        }
    }
}