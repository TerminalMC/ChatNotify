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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static notryken.chatnotify.client.ChatNotifyClient.*;

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
        String strMsg = TextVisitFactory.removeFormattingCodes(message);
        strMsg = preProcess(strMsg);

        if (strMsg != null) {
            checkNotifications(message, strMsg, refresh);
        }
    }

    /**
     * Modifies the message based on whether it matches the user's last sent
     * message, and whether the user's own messages are to be ignored.
     * @param strMsg The original message.
     * @return The processed message.
     */
    private String preProcess(String strMsg)
    {
        if (!recentMessages.isEmpty()) {
            /*
            This check is considered to adequately determine whether the message
            was sent by the user.
             */
            for (String username : config.getNotif(0).getTriggers()) {
                if (strMsg.contains(username)) { //  && !recentMessages.isEmpty() Second value prevents errors when spamming (or does it?)
                    for (int i = recentMessages.size() - 1; true; i--)
                    {
                        if (strMsg.contains(recentMessages.get(i))) {
                            recentMessages.remove(i);
                            recentMessageTimes.remove(i);

                            if (config.ignoreOwnMessages) {
                                strMsg = null;
                            }
                            else {
                                strMsg = strMsg.replaceFirst(username, "");
                            }

                            break;
                        }
                    }
                    break;
                }
            }

//            if (strMsg.contains(username) && strMsg.contains(lastSentMessage)) {
//                lastSentMessage = null;
//                if (config.ignoreOwnMessages) {
//                    strMsg = null;
//                }
//                else {
//                    strMsg = strMsg.replaceFirst(username, "");
//                }
//            }
        }
        return strMsg;
    }

    /**
     * Checks all triggers of all enabled notifications to determine whether
     * the message should trigger a notification, calling notify() if so.
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
                         iter2.hasNext() && modifiedMessage == null;)
                    {
                        String trigger = iter2.next();
                        if (msgContainsStr(strMsg, trigger)) {
                            notify(message, trigger, false, notif, mute);
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

            if (keyTrigger) {
                //System.out.println("-A1 (is keyTrigger)");
                newMessage.setStyle(style);
            }
            else {
                //System.out.println("-B1 (not keyTrigger)");

                List<Text> siblings = newMessage.getSiblings();

                if (siblings.isEmpty()) {
                    //System.out.println("-A2 (no siblings)");

                    TextContent msgContent = newMessage.getContent();

                    if (msgContent instanceof TranslatableTextContent ttc) {
                        //System.out.println("-A3 (is TTC)");

                        Object[] args = ttc.getArgs();
                        boolean done = false;

                        for (int i = 0; i < args.length && !done; i++)
                        {
                            if (args[i] instanceof Text argText)
                            {
                                //System.out.println("Initial " + i);
                                //printTree(argText, 0);

                                MutableText mt = Text.empty();
                                mt.siblings.add(argText);
                                done = styleTrigger(mt.siblings, trigger, style) == 0;
                                if (mt.siblings.size() == 1) {
                                    args[i] = mt.siblings.get(0);
                                }
                                else {
                                    args[i] = mt;
                                }

                                //System.out.println("Final " + i);
                                //printTree((Text) args[i], 0);
                            }
                        }
                        if (done) {
                            newMessage = MutableText.of(
                                    new TranslatableTextContent(
                                            ttc.getKey(),
                                            ttc.getFallback(),
                                            args))
                                    .setStyle(newMessage.getStyle());
                        }
                        else {
                            newMessage.setStyle(style);
                        }
                    }
                    else {
                        //System.out.println("-B3 (not TTC)");

                        siblings.add(newMessage);
                        styleTrigger(siblings, trigger, style);
                        if (siblings.size() == 1) {
                            newMessage = (MutableText) siblings.get(0);
                        }
                    }
                }
                else {
                    //System.out.println("-B2 (has siblings)");

                    //printTree(message, 0);

                    /* Possible error here, giving styleTrigger() the siblings
                    but not the text content. Not sure if it's a problem. */
                    styleTrigger(siblings, trigger, style);
                }
            }
            modifiedMessage = newMessage;
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
     * Attempts to format the first occurrence of the given trigger according
     * to the given style, without breaking everything (touch wood).
     * @param siblings List of Text objects representing siblings of a Text
     *                 message
     * @param trigger The trigger string
     * @param style The style to use
     * @return 0 if successful, -1 otherwise.
     */
    private int styleTrigger(List<Text> siblings, String trigger, Style style)
    {
        int retVal = -1;

        for (int i = 0; i < siblings.size(); i++) {

            Text sibling = siblings.get(i);
            String str = sibling.getString();

            int start;
            Matcher matcher = Pattern.compile("(?<!\\w)(\\W?(?i)" + trigger + "\\W?)(?!\\w)").matcher(str);
            if (matcher.find()) {
                start = matcher.start();
                if (start > 0) {
                    start = str.substring(matcher.start()).toLowerCase().indexOf(trigger.toLowerCase()) + matcher.start();
                }
            }
            else {
                start = -1;
            }

            if (start != -1)
            {
                if (!sibling.getSiblings().isEmpty()) {
                    retVal = styleTrigger(sibling.getSiblings(), trigger, style);
                }

                if (retVal == -1)
                {
                    if (sibling.getContent() instanceof LiteralTextContent ltc)
                    {
                        List<Text> subSiblings = sibling.getSiblings();
                        subSiblings.replaceAll(text ->
                                fixStyle((MutableText) text, sibling.getStyle()));

                        String tempStr = ltc.toString();
                        // Remove 'literal{}'
                        String subStr = tempStr.substring(8, tempStr.length() - 1);

                        if (start + trigger.length() != subStr.length()) {
                            subSiblings.add(0, Text.literal(
                                            subStr.substring(start + trigger.length()))
                                    .setStyle(sibling.getStyle()));
                        }

                        subSiblings.add(0, Text.literal(subStr.substring(
                                start, start + trigger.length())).setStyle(style
                                .withClickEvent(sibling.getStyle().getClickEvent())
                                .withHoverEvent(sibling.getStyle().getHoverEvent())
                                .withInsertion(sibling.getStyle().getInsertion())));

                        if (start != 0) {
                            subSiblings.add(0, Text.literal(
                                            subStr.substring(0, start))
                                    .setStyle(sibling.getStyle()));
                        }

                        if (subSiblings.size() == 1) {
                            siblings.set(i, subSiblings.get(0));
                        }
                        else {
                            MutableText mt = MutableText.of(TextContent.EMPTY);
                            mt.siblings.addAll(subSiblings);
                            siblings.set(i, mt);
                        }
                        retVal = 0;
                    }
                }

                // If all else fails.
                if (retVal == -1) {
                    //System.out.println("Recursion and LTC fix failed.");

                    siblings.remove(i);

                    if (start + trigger.length() != str.length()) {
                        siblings.add(i, Text.literal(
                                str.substring(start + trigger.length()))
                                .setStyle(sibling.getStyle()));
                    }

                    siblings.add(i, Text.literal(str.substring(
                            start, start + trigger.length())).setStyle(style
                            .withClickEvent(sibling.getStyle().getClickEvent())
                            .withHoverEvent(sibling.getStyle().getHoverEvent())
                            .withInsertion(sibling.getStyle().getInsertion())));

                    if (start != 0) {
                        siblings.add(i, Text.literal(str.substring(0, start))
                                .setStyle(sibling.getStyle()));
                    }

                    retVal = 0;
                }

                return retVal;
            }
        }
        return retVal;
    }


    /**
     * Replaces the null fields of the text's Style with the corresponding
     * fields of the specified Style, excluding text format (bold, italic etc).
     * @param text The Text to fix
     * @param style The Style to pull from
     * @return The given Text, with null Style values replaced with the given
     * Style's values.
     */
    private MutableText fixStyle(MutableText text, Style style)
    {
        Style newStyle = text.getStyle();
        if (text.getStyle().getColor() == null) {
            newStyle = newStyle.withColor(style.getColor());
        }
        if (text.getStyle().getClickEvent() == null) {
            newStyle = newStyle.withClickEvent(style.getClickEvent());
        }
        if (text.getStyle().getHoverEvent() == null) {
            newStyle = newStyle.withHoverEvent(style.getHoverEvent());
        }
        if (text.getStyle().getInsertion() == null) {
            newStyle = newStyle.withInsertion(style.getInsertion());
        }
        if (text.getStyle().getFont() == null) {
            newStyle = newStyle.withFont(style.getFont());
        }
        text.setStyle(newStyle);
        return text;
    }

//    private void printTree(Text message, int depth)
//    {
//        depth++;
//        StringBuilder indent = new StringBuilder();
//        indent.append(">   ".repeat(depth));
////        //System.out.println(indent + "Content: " + message.getContent());
////        //System.out.println(indent + "Style : " + message.getStyle());
////        //System.out.println(indent + "Siblings: ");
//        for (Text t : message.getSiblings())
//        {
//            printTree(t, depth);
//        }
//    }

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