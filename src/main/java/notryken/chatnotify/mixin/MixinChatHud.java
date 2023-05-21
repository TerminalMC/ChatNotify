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

import java.util.ArrayList;
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

            String strMsg = TextVisitFactory.removeFormattingCodes(message);
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

            System.out.println("#########");

            if (keyTrigger)
            {
                System.out.println("-A1 (is keyTrigger)");

                newMessage.setStyle(style);
            }
            else
            {
                System.out.println("-B1 (not keyTrigger)");

                List<Text> siblings = newMessage.getSiblings();

                if (siblings.isEmpty())
                {
                    System.out.println("-A2 (no siblings)");

                    TextContent msgContent = newMessage.getContent();

                    if (msgContent instanceof TranslatableTextContent ttc)
                    {
                        System.out.println("-A3 (is TTC)");
                        Object[] args = ttc.getArgs();

                        if (ttc.getKey().equals("chat.type.text"))
                        {
                            System.out.println("-A4 (is chat.type.text)");

                            // All chat.type.text messages should have 2 args.
                            if (args.length == 2)
                            {
                                System.out.println("-A5 (2 args)");

                                boolean done = false;

                                for (int i = 0; i < args.length && !done; i++)
                                {
                                    if (args[i] instanceof Text argText)
                                    {
                                        System.out.println("#### Original tree of arg " + i);
                                        printTree(argText, 0);

                                        MutableText mt = Text.empty();
                                        mt.siblings.add(argText);
                                        done = partialHighlight(mt.siblings, trigger, style) == 0;
                                        if (mt.siblings.size() == 1) {
                                            args[i] = mt.siblings.get(0);
                                        }
                                        else {
                                            args[i] = mt;
                                        }

                                        System.out.println("#### Modified tree of arg " + i);
                                        printTree((Text) args[i], 0);
                                    }
                                }
                                newMessage = MutableText.of(
                                                new TranslatableTextContent(
                                                        ttc.getKey(), ttc.getFallback(), args))
                                        .setStyle(newMessage.getStyle());

                                /*boolean done = false;

                                if (args[0] instanceof Text argText &&
                                        argText.getString().equalsIgnoreCase(trigger))
                                {
                                    System.out.println("-A6 (arg 0 is trigger)");

                                    args[0] = argText.copy().fillStyle(style);
                                    done = true;
                                    newMessage = MutableText.of(new TranslatableTextContent(ttc.getKey(), ttc.getFallback(), args));
                                }
                                if (!done && args[1] instanceof Text argText)
                                {
                                    System.out.println("-B6 (arg 0 not trigger)");

                                    if (argText.getString().equalsIgnoreCase(trigger))
                                    {
                                        System.out.println("-A7 (arg 1 is trigger)");

                                        args[1] = argText.copy().setStyle(style);
                                    }
                                    else
                                    {
                                        System.out.println("-B7 (arg 1 not trigger, but may contain)");

                                        siblings.add(argText);
                                        partialHighlight(siblings, trigger, style);

                                        MutableText mt = Text.empty();
                                        mt.siblings.addAll(siblings);
                                        args[1] = mt;
                                    }
                                    newMessage = MutableText.of(new TranslatableTextContent(ttc.getKey(), ttc.getFallback(), args));
                                    done = true;
                                }
                                if (!done)
                                {
                                    System.out.println("-C6 (neither arg 1 was trigger nor arg 2 was or contained trigger");

                                    newMessage.setStyle(style);
                                }*/
                            }
                        }

                        else {
                            System.out.println("-B4 (not chat.type.text, actually " + ttc.getKey() + ")");

                            System.out.println("\n# original content: " + newMessage.getContent());
                            System.out.println("\n# original string: " + newMessage.getString());
                            System.out.println("\n# original style: " + newMessage.getStyle());

                            /*
                            So this is ok, there is a bug in that the formatting
                             applied to an arg extends to text past that, e.g.
                             if "player1" is a trigger, then
                             "player1 was slain by player2" would be fully
                             colored to the 'player1' notif, whereas for
                             "player2 was slain by player1", only the 'player1'
                             would be colored.
                             */

                            System.out.println("# args count: " + args.length);

                            boolean done = false;

                            for (int i = 0; i < args.length && !done; i++)
                            {
                                if (args[i] instanceof Text argText)
                                {
                                    System.out.println("#### Original tree of arg " + i);
                                    printTree(argText, 0);

                                    MutableText mt = Text.empty();
                                    mt.siblings.add(argText);
                                    done = partialHighlight(mt.siblings, trigger, style) == 0;
                                    if (mt.siblings.size() == 1) {
                                        args[i] = mt.siblings.get(0);
                                    }
                                    else {
                                        args[i] = mt;
                                    }

                                    System.out.println("#### Modified tree of arg " + i);
                                    printTree((Text) args[i], 0);
                                }
                            }
                            newMessage = MutableText.of(
                                    new TranslatableTextContent(
                                            ttc.getKey(), ttc.getFallback(), args))
                                    .setStyle(newMessage.getStyle());


                            System.out.println("\n# final content: " + newMessage.getContent());
                            System.out.println("\n# final string: " + newMessage.getString());
                            System.out.println("\n# final style: " + newMessage.getStyle());

                            /*if (!done) {
                                newMessage.setStyle(style);
                            }*/
                        }
                    }
                    else {
                        System.out.println("-B3 (not TTC)");

                        siblings.add(newMessage);
                        partialHighlight(siblings, trigger, style);

                        //newMessage.setStyle(style);
                    }
                }
                else {
                    System.out.println("-B2 (has siblings)");

                    //printTree(message, 0);

                    partialHighlight(siblings, trigger, style);
                }
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

    private void printTree(Text message, int depth)
    {
        depth++;
        StringBuilder indent = new StringBuilder();
        indent.append(">   ".repeat(depth));
        System.out.println(indent + "Content: " + message.getContent());
        System.out.println(indent + "Style : " + message.getStyle());
        System.out.println(indent + "Siblings: ");
        for (Text t : message.getSiblings())
        {
            printTree(t, depth);
        }
    }

    private int partialHighlight(List<Text> siblings, String trigger, Style style)
    {
        int retVal = -1;

        for (int i = 0; i < siblings.size(); i++) {

            Text sibling = siblings.get(i);
            String str = sibling.getString();
            String lowerStr = str.toLowerCase();
            int start = lowerStr.indexOf(trigger.toLowerCase());

            if (start != -1)
            {
                // if the sibling has sub-siblings, recurse through them.
                if (!sibling.getSiblings().isEmpty()) {
                    retVal = partialHighlight(sibling.getSiblings(), trigger, style);
                }


                // If the style has not been changed (if no sub-siblings, or they
                // do not contain the match).
                if (retVal == -1 &&
                        sibling.getContent() instanceof LiteralTextContent ltc)
                {
                    /*
                    This uses 0-insertion to deal with the case of the trigger
                    being contained in the sibling somehow without being
                    contained in it's sub-siblings (assumes that the render
                    order is content then siblings).
                     */

                    // FIXME we're losing the siblings after the added ones duh

                    /*
                    subSibling.setStyle(sibling.getStyle()) overrides sibling format

                     */

                    List<Text> subSiblings = sibling.getSiblings();
                    subSiblings.replaceAll(text -> fixFormat((MutableText) text, sibling.getStyle()));

                    String tempStr = ltc.toString();
                    String str1 = tempStr.substring(8, tempStr.length() - 1); // Remove 'literal{}'

                    //List<Text> subTexts = new ArrayList<>();

                    // do the substring thingy on the basis of the ints rather than the string results

                    String subStr3 = str1.substring(start + trigger.length());
                    if (!subStr3.equals("")) {
                        subSiblings.add(0, Text.literal(subStr3).setStyle(sibling.getStyle()));
                    }

                    String subStr2 = str1.substring(start, start + trigger.length());
                    subSiblings.add(0, Text.literal(subStr2).setStyle(style
                            .withClickEvent(sibling.getStyle().getClickEvent())
                            .withHoverEvent(sibling.getStyle().getHoverEvent())
                            .withInsertion(sibling.getStyle().getInsertion()))); // originally setStyle(style)

                    String subStr1 = str1.substring(0, start);
                    if (!subStr1.equals("")) {
                        subSiblings.add(0, Text.literal(subStr1).setStyle(sibling.getStyle()));
                    }


                    /*subSiblings.add(0, Text.literal(subStr3).setStyle(sibling.getStyle()));
                    subSiblings.add(0, Text.literal(subStr2).setStyle(style));
                    subSiblings.add(0, Text.literal(subStr1).setStyle(sibling.getStyle()));*/

                    if (subSiblings.size() == 1) {
                        siblings.set(i, subSiblings.get(0));
                    }
                    else {
                        //MutableText mt = MutableText.of(TextContent.EMPTY).setStyle(sibling.getStyle());
                        MutableText mt = MutableText.of(TextContent.EMPTY);

                        mt.siblings.addAll(subSiblings);
                        //mt.siblings.addAll(sibling.getSiblings());

                        siblings.set(i, mt);
                    }

                    retVal = 0;
                }

                // If both the above fail.
                if (retVal == -1) {
                    System.out.println(">>>>> aaaaaaahhhhhhhhh <<<<<<");
                    siblings.remove(i);
                    siblings.add(i, Text.literal(str.substring(start + trigger.length())).setStyle(sibling.getStyle()));
                    siblings.add(i, Text.literal(str.substring(start, start + trigger.length())).setStyle(style));
                    siblings.add(i, Text.literal(str.substring(0, start)).setStyle(sibling.getStyle()));
                    retVal = 0;
                }

                return retVal;
            }
        }
        return retVal;
    }


    /**
     * Replaces the null fields of the text's format with the corresponding
     * fields of its parent.
     */
    private MutableText fixFormat(MutableText text, Style parentStyle)
    {
        Style style = text.getStyle();
        if (text.getStyle().getColor() == null) {
            style = style.withColor(parentStyle.getColor());
        }
        if (text.getStyle().getClickEvent() == null) {
            style = style.withClickEvent(parentStyle.getClickEvent());
        }
        if (text.getStyle().getHoverEvent() == null) {
            style = style.withHoverEvent(parentStyle.getHoverEvent());
        }
        if (text.getStyle().getInsertion() == null) {
            style = style.withInsertion(parentStyle.getInsertion());
        }
        if (text.getStyle().getFont() == null) {
            style = style.withFont(parentStyle.getFont());
        }
        text.setStyle(style);
        return text;
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