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
        Notification notif;
        for (Iterator<Notification> iter1 = config.getNotifs().iterator();
             iter1.hasNext() && modifiedMessage == null;)
        {
            notif = iter1.next();
            if (notif.enabled)
            {
                if (notif.triggerIsKey && message.getContent() instanceof
                        TranslatableTextContent ttc)
                {
                    String trigger = notif.getTrigger();
                    if (ttc.getKey().contains(trigger)) {
                        notify(message, trigger, true, notif, mute);
                    }
                }
                else {
                    for (Iterator<String> iter2 = notif.getTriggerIterator();
                         iter2.hasNext();)
                    {
                        String trigger = iter2.next();
                        if (msgContainsStr(strMsg, trigger)) {
                            notify(message, trigger, false, notif, mute);
                            break;
                        }
                    }
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
    private void notify(Text message, String trigger, boolean keyTrigger,
                        Notification notif, boolean mute)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            MutableText newMessage = message.copy();

            Style style = Style.of(
                    Optional.of(TextColor.fromRgb(notif.getColor())),
                    Optional.of(notif.getFormatControl(0)),
                    Optional.of(notif.getFormatControl(1)),
                    Optional.of(notif.getFormatControl(2)),
                    Optional.of(notif.getFormatControl(3)),
                    Optional.of(notif.getFormatControl(4)),
                    Optional.empty(),
                    Optional.empty());

            //System.out.println("# incoming content: " + message.getContent());
            System.out.println("# incoming siblings: " + message.getSiblings());

            System.out.println("#########");

            if (keyTrigger) {
                System.out.println("-A1");
                newMessage.setStyle(style);
            }
            else {
                System.out.println("-B1");
                List<Text> siblings = newMessage.getSiblings();

                if (siblings.isEmpty()) {
                    System.out.println("-A2");
                    TextContent msgContent = newMessage.getContent();
                    if (msgContent instanceof TranslatableTextContent ttc &&
                            ttc.getKey().equals("chat.type.text"))
                    {
                        System.out.println("-A3");

                        Object[] args = ttc.getArgs();

                        if (args.length == 2) {
                            System.out.println("-A4");

                            boolean done = false;

                            if (args[0] instanceof Text argText)
                            {
                                System.out.println("-A5");
                                if (argText.getString().equalsIgnoreCase(trigger))
                                {
                                    System.out.println("-A6A");
                                    args[0] = argText.copy().fillStyle(style);
                                    done = true;
                                    newMessage = MutableText.of(new TranslatableTextContent(ttc.getKey(), ttc.getFallback(), args));
                                }
                                else {
                                    System.out.println("-B6A");
                                }
                            }
                            if (!done && args[1] instanceof Text argText)
                            {
                                System.out.println("-B5");
                                if (argText.getString().equalsIgnoreCase(trigger))
                                {
                                    System.out.println("-A6B");
                                    args[1] = argText.copy().setStyle(style);
                                }
                                else
                                {
                                    System.out.println("-B6B");
                                    siblings.add(argText);
                                    partialHighlight(siblings, trigger, style);

                                    MutableText mt = Text.empty();
                                    mt.siblings.addAll(siblings);
                                    args[1] = mt;
                                }
                                newMessage = MutableText.of(new TranslatableTextContent(ttc.getKey(), ttc.getFallback(), args));
                                done = true;
                            }
                            if (!done) {
                                System.out.println("-C5");
                                newMessage.setStyle(style);
                            }
                        }
                        else {
                            System.out.println("-B4");
                            newMessage.setStyle(style);
                        }
                    }
                    else {
                        System.out.println("-B3");
                        if (msgContent instanceof LiteralTextContent) {
                            System.out.println("### message is class LiteralTextContent");
                        } else if (msgContent instanceof NbtTextContent) {
                            System.out.println("### message is class NbtTextContent");
                        } else if (msgContent instanceof ScoreTextContent) {
                            System.out.println("### message is class ScoreTextContent");
                        } else if (msgContent instanceof SelectorTextContent) {
                            System.out.println("### message is class SelectorTextContent");
                        } else if (msgContent instanceof KeybindTextContent) {
                            System.out.println("### message is class KeybindTextContent");
                        } else {
                            System.out.println("### message is class idkwth");
                        }
                        newMessage.setStyle(style);
                    }
                }
                else {
                    System.out.println("-B2");
                    partialHighlight(siblings, trigger, style);
                }
            }


            //System.out.println("# outgoing content: " + newMessage.getContent());
            System.out.println("# outgoing siblings: " + newMessage.getSiblings());

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

    private void partialHighlight(List<Text> siblings, String trigger, Style style)
    {
        for (int i = 0; i < siblings.size(); i++) {

            Text sibling = siblings.get(i);

            if (!sibling.getSiblings().isEmpty()) {
                partialHighlight(sibling.getSiblings(), trigger, style);
            }
            else {
                String str = sibling.getString();
                String lowerStr = str.toLowerCase();
                int start = lowerStr.indexOf(trigger.toLowerCase());

                if (start != -1) {
                    siblings.remove(i);
                    siblings.add(i, Text.literal(str.substring(start + trigger.length())).setStyle(sibling.getStyle()));
                    siblings.add(i, Text.literal(str.substring(start, start + trigger.length())).setStyle(style));
                    siblings.add(i, Text.literal(str.substring(0, start)).setStyle(sibling.getStyle()));
                    i = siblings.size(); // Exit condition
                }
            }

            /*
            FIXME this still doesn't work properly in that on some servers highly-formatted
             messages will not be partially recolored. I'm not entirely sure why that is, but
             honestly I don't really care at this point.
             */
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