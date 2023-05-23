package notryken.chatnotify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import notryken.chatnotify.config.Notification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static notryken.chatnotify.client.ChatNotifyClient.config;
import static notryken.chatnotify.client.ChatNotifyClient.lastSentMessage;

/**
 * The backbone of ChatNotify. Intercepts chat messages as they are sent to
 * the list to be displayed, checks whether they contain any words matching
 * any of the loaded Notification objects' triggers, and if so, modifies the
 * color and formatting as specified by the relevant Notification, and plays the
 * specified notification sound.
 */
@Mixin(ChatHud.class)
public class MixinChatHud {

    private final SoundManager soundManager =
            MinecraftClient.getInstance().getSoundManager();
    private Text modifiedMessage = null;


    /**
     * Intercepts chat messages as they are sent to the list to be displayed,
     * and starts the notification process.
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
            String strMsg = message.getString();

            strMsg = preProcess(strMsg, username);

            if (strMsg != null) {
                checkNotifications(message, strMsg, refresh);
            }
        }
    }

    /**
     * Modifies the message based on whether it matches the user's last sent
     * message, and whether the user's own messages are to be ignored.
     * @param strMsg The original message.
     * @param username The user's in-game name.
     * @return The processed message.
     */
    private String preProcess(String strMsg, String username)
    {
        if (lastSentMessage != null) {
            /*
            This check is considered to adequately determine whether the message
            was sent by the user.
             */
            if (strMsg.contains(username) && strMsg.contains(lastSentMessage)) {
                lastSentMessage = null;
                if (config.ignoreOwnMessages) {
                    strMsg = null;
                }
                else {
                    strMsg = strMsg.replaceFirst(username, "");
                }
            }
        }
        return strMsg;
    }

    /**
     * Checks all triggers of all enabled notifications to determine whether
     * the message should trigger a notification, calling doNotify() if so.
     * @param message The original message.
     * @param strMsg The processed message.
     * @param mute Whether the notification sound should be ignored.
     */
    private void checkNotifications(Text message, String strMsg, boolean mute)
    {
        modifiedMessage = null; // Reset.
        for (Notification notif : config.getNotifs()) {
            if (modifiedMessage == null && notif.enabled) {
                boolean doNotify = false;
                String trigger = "";

                if (notif.triggerIsKey) {
                    if (message.getContent() instanceof
                            TranslatableTextContent ttc) {
                        //System.out.println(ttc.getKey());
                        trigger = notif.getTrigger();
                        doNotify = ttc.getKey().contains(trigger);
                    }
                }
                else {
                    Iterator<String> iter = notif.getTriggerIterator();
                    while (iter.hasNext() && !doNotify) {
                        trigger = iter.next();
                        doNotify = msgContainsStr(strMsg, trigger);
                    }
                }

                if (doNotify) {
                    notify(message, trigger, notif, mute);
                }
            }
        }
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
     * Notifies the user based on the specified notification's configuration.
     * @param message The original message.
     * @param trigger The String that triggered the notification.
     * @param notif The notification.
     * @param mute Whether the notification sound should be ignored.
     */
    private void notify(Text message, String trigger, Notification notif,
                        boolean mute)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            MutableText newMessage = MutableText.of(message.getContent());

            Style style = Style.of(
                    Optional.of(TextColor.fromRgb(notif.getColor())),
                    Optional.of(notif.getFormatControl(0)),
                    Optional.of(notif.getFormatControl(1)),
                    Optional.of(notif.getFormatControl(2)),
                    Optional.of(notif.getFormatControl(3)),
                    Optional.of(notif.getFormatControl(4)),
                    Optional.empty(),
                    Optional.empty());

            /*List<Text> siblings = message.getSiblings();
            int i = 0;
            boolean done = false;
            while (i < siblings.size() && !done) {
                String str = siblings.get(i).getString();
                if (str.contains(trigger)) {
                    done = true;
                    siblings.set(i,
                            Text.of(TextVisitFactory.removeFormattingCodes(
                                    StringVisitable.plain(str))));
                }
                i++;
            }*/

            /*
            FIXME this is late-night proof-of-concept code, so very messy and
             inefficient.
             */

            /*
            TODO ayy it works!
             */

            boolean siblingsModified = false;

            if (message.getSiblings().isEmpty())
            {
                System.out.println("## no-sibling message");

                System.out.println("## content: " + message.getContent());

                /*
                FIXME so basically here on servers that don't use siblings,
                 there's still a way to set style of sub-parts of the message
                 but I have no idea how to access it.
                 */

                //System.out.println("## 2ndSiblings: " + message.getContent());

                String str = message.getString();
                Style originalStyle = message.getStyle();
                System.out.println("## original style: " + originalStyle);

                newMessage = Text.empty();
                List<Text> siblings = newMessage.getSiblings();

                int start = str.indexOf(trigger);

                siblingsModified = true;
                System.out.println("## original str: " + str);

                String subStr1 = str.substring(0, start);
                String subStr2 = str.substring(start, start + trigger.length());
                String subStr3 = str.substring(start + trigger.length());

                MutableText subText1 = Text.literal(subStr1);
                //subText1.setStyle(originalStyle);
                MutableText subText2 = Text.literal(subStr2);
                subText2.setStyle(style);
                MutableText subText3 = Text.literal(subStr3);
                //subText3.setStyle(originalStyle);

                System.out.println("## sub1: " + subText1);
                System.out.println("## sub2: " + subText2);
                System.out.println("## sub3: " + subText3);

                siblings.add(0, subText3);
                siblings.add(0, subText2);
                siblings.add(0, subText1);
            }
            else
            {
                /*
                This block only works on servers like Hypixel, MineHut etc. that
                use message siblings.
                 */
                List<Text> siblings = message.getSiblings();
                System.out.println("## style: " + message.getStyle());
                for (Text sibling : siblings) {
                    System.out.println("## sibling x: " + sibling);
                }
                for (int i = 0; i < siblings.size(); i++) {
                    System.out.println("## sibling " + i + ": " + siblings.get(i));
                    Text sibling = siblings.get(i);
                    String str = sibling.getString();
                    int start = str.indexOf(trigger);
                    if (start != -1) {
                        siblingsModified = true;
                        System.out.println("## original str: " + str);

                        String subStr1 = str.substring(0, start);
                        String subStr2 = str.substring(start, start + trigger.length());
                        String subStr3 = str.substring(start + trigger.length());

                        MutableText subText1 = Text.literal(subStr1);
                        subText1.setStyle(sibling.getStyle());
                        MutableText subText2 = Text.literal(subStr2);
                        subText2.setStyle(style);
                        MutableText subText3 = Text.literal(subStr3);
                        subText3.setStyle(sibling.getStyle());

                        System.out.println("## sub1: " + subText1);
                        System.out.println("## sub2: " + subText2);
                        System.out.println("## sub3: " + subText3);

                        siblings.remove(i);
                        siblings.add(i, subText3);
                        siblings.add(i, subText2);
                        siblings.add(i, subText1);

                        /*StringBuilder formatCodes = new StringBuilder();
                        char[] strArr = str.toCharArray();
                        for (int j = 0; j < start; j++) {
                            if (strArr[j] == '§') {
                                j++;
                                if (strArr[j] == 'r') {
                                    formatCodes = new StringBuilder();
                                }
                                else {
                                    formatCodes.append('§');
                                    formatCodes.append(strArr[j]);
                                }
                            }
                        }
                        System.out.println("## formatCodes: " + formatCodes);
                        if (!formatCodes.isEmpty()) {
                            StringBuilder modStr = new StringBuilder(str);
                            modStr.insert(start, "§r");
                            modStr.insert(start + 2 + str.length(), formatCodes);
                            str = modStr.toString();
                        }
                        System.out.println("## final str: " + str);
                        MutableText text = Text.literal(str);
                        text.setStyle(style);
                        System.out.println("## final text: " + text);
                        siblings.set(i, text);*/

                        i = siblings.size(); // Exit condition
                    }
                }
                newMessage.siblings.addAll(siblings);
            }

            if (!siblingsModified) {
                newMessage.setStyle(style);
            }

            modifiedMessage = newMessage;
        }
        else {
            modifiedMessage = message;
        }

        if (!mute && notif.getControl(2)) {
            soundManager.play(new PositionedSoundInstance(
                    notif.getSound(), SoundCategory.PLAYERS,
                    notif.soundVolume, notif.soundPitch,
                    SoundInstance.createRandom(), false, 0,
                    SoundInstance.AttenuationType.NONE, 0, 0, 0, true));
        }

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