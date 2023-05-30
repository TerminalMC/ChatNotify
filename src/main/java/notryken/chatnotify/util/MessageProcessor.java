package notryken.chatnotify.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import notryken.chatnotify.config.Notification;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static notryken.chatnotify.client.ChatNotifyClient.*;
import static notryken.chatnotify.client.ChatNotifyClient.config;

public class MessageProcessor
{
    private static Text modifiedMessage = null;

    /**
     * Processes the specified message, modifying it and playing a sound if it
     * matches a notification trigger.
     * @param message The original message.
     * @return The processed message, modified if required.
     */
    public static Text processMessage(Text message)
    {
        modifiedMessage = null;

        String strMsg =
                preProcess(TextVisitFactory.removeFormattingCodes(message));

        if (strMsg != null) {
            tryNotify(message, strMsg);
        }

        return (modifiedMessage == null ? message : modifiedMessage);
    }

    /**
     * Modifies the message based on whether it is identified as being sent by
     * the user, and whether such messages are to be ignored.
     * @param strMsg The original message.
     * @return The processed message.
     */
    private static String preProcess(String strMsg)
    {
        if (!recentMessages.isEmpty()) {
            // Check each username trigger to allow for nicknames.
            for (String username : config.getNotif(0).getTriggers()) {
                int nameStart = strContainsStr(strMsg, username);
                if (nameStart >= 0) {
                    // Avoid username matching recent message content.
                    strMsg = strMsg.substring(0, nameStart + 1) +
                            strMsg.substring(nameStart + 1 + username.length());
                    for (int i = 0; i < recentMessages.size(); i++) {
                        if (strMsg.contains(recentMessages.get(i))) {
                            recentMessages.remove(i);
                            recentMessageTimes.remove(i);
                            if (config.ignoreOwnMessages) {
                                strMsg = null;
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return strMsg;
    }

    /**
     * Checks each trigger of each notification, until successful, against the
     * specified message. If one matches, modifies the message according to
     * the corresponding notification and plays the notification's sound.
     * @param message The original message.
     * @param strMsg The processed message.
     */
    private static void tryNotify(Text message, String strMsg)
    {
        Notification notif;
        for (Iterator<Notification> iter1 = config.getNotifs().iterator();
             iter1.hasNext() && modifiedMessage == null;)
        {
            notif = iter1.next();
            if (notif.enabled) {
                if (notif.triggerIsKey) {
                    if (message.getContent() instanceof
                            TranslatableTextContent ttc)
                    {
                        if (ttc.getKey().contains(notif.getTrigger())) {
                            playSound(notif);
                            modifiedMessage = simpleRestyle(message, notif);
                        }
                    }
                }
                else {
                    for (Iterator<String> iter2 = notif.getTriggerIterator();
                         iter2.hasNext() && modifiedMessage == null;)
                    {
                        String trigger = iter2.next();
                        if (strContainsStr(strMsg, trigger) >= 0) {
                            modifiedMessage =
                                    complexRestyle(message, trigger, notif);
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
     * @return The index of the start of str in msg, or -1 if not found.
     */
    private static int strContainsStr(String msg, String str)
    {
        Matcher matcher = Pattern.compile("(?<!\\w)(\\W?(?i)" +
                Pattern.quote(str) + "\\W?)(?!\\w)", Pattern.CASE_INSENSITIVE)
                .matcher(msg);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    private static void playSound(Notification notif)
    {
        if (notif.getControl(2)) {
            MinecraftClient.getInstance().getSoundManager().play(
                    new PositionedSoundInstance(
                            notif.getSound(), SoundCategory.PLAYERS,
                            notif.soundVolume, notif.soundPitch,
                            SoundInstance.createRandom(), false, 0,
                            SoundInstance.AttenuationType.NONE, 0, 0, 0, true));
        }
    }

    private static Text simpleRestyle(Text message, Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            return message.copy().setStyle(Style.of(
                    Optional.of(notif.getColor()),
                    Optional.of(notif.getFormatControl(0)),
                    Optional.of(notif.getFormatControl(1)),
                    Optional.of(notif.getFormatControl(2)),
                    Optional.of(notif.getFormatControl(3)),
                    Optional.of(notif.getFormatControl(4)),
                    Optional.empty(),
                    Optional.empty()));
        }
        return null;
    }

    private static Text complexRestyle(Text message,
                                       String trigger,
                                       Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            MutableText newMessage = message.copy();

            Style newStyle = Style.of(
                    Optional.of(notif.getColor()),
                    Optional.of(notif.getFormatControl(0)),
                    Optional.of(notif.getFormatControl(1)),
                    Optional.of(notif.getFormatControl(2)),
                    Optional.of(notif.getFormatControl(3)),
                    Optional.of(notif.getFormatControl(4)),
                    Optional.empty(),
                    Optional.empty());

            System.out.println("-B1 (not keyTrigger)");

            List<Text> siblings = newMessage.getSiblings();

            if (siblings.isEmpty()) {
                System.out.println("-A2 (no siblings)");

                TextContent msgContent = newMessage.getContent();

                if (msgContent instanceof TranslatableTextContent ttc) {
                    System.out.println("-A3 (is TTC)");

                    Object[] args = ttc.getArgs();
                    boolean done = false;

                    for (int i = 0; i < args.length && !done; i++)
                    {
                        if (args[i] instanceof Text argText)
                        {
                            System.out.println("Initial " + i);
                            printTree(argText, 0);

                            MutableText mt = Text.empty();
                            mt.siblings.add(argText);
                            done = styleTrigger(mt.siblings, trigger,
                                    newStyle) == 0;
                            if (mt.siblings.size() == 1) {
                                args[i] = mt.siblings.get(0);
                            }
                            else {
                                args[i] = mt;
                            }

                            System.out.println("Final " + i);
                            printTree((Text) args[i], 0);
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
                        newMessage.setStyle(newStyle);
                    }
                }
                else {
                    System.out.println("-B3 (not TTC)");

                    siblings.add(newMessage);
                    styleTrigger(siblings, trigger, newStyle);
                    if (siblings.size() == 1) {
                        newMessage = (MutableText) siblings.get(0);
                    }
                }
            }
            else {
                System.out.println("-B2 (has siblings)");

                Text original = newMessage.copy();

                System.out.println("before:");
                printTree(message, 0);

                    /* Possible error here, giving styleTrigger() the
                    siblings but not the text content. Not sure if it's a
                    problem. */
                styleTrigger(siblings, trigger, newStyle);

                System.out.println("after:");
                printTree(message, 0);

                if (newMessage.equals(original)) {
                    System.out.println("original and modified are " +
                            "equal");
                }
                else {
                    System.out.println("original and modified are not" +
                            " equal");
                }
            }
            return newMessage;
        }
        return null;
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
    private static int styleTrigger(List<Text> siblings, String trigger, Style style)
    {
        int retVal = -1;

        Pattern pattern = Pattern.compile("(?<!\\w)(\\W?(?i)" +
                Pattern.quote(trigger) + "\\W?)(?!\\w)",
                Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < siblings.size(); i++) {

            Text sibling = siblings.get(i);
            String str = sibling.getString();

            int start;
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                start = matcher.start();
                if (start > 0) {
                    start = str.substring(matcher.start()).toLowerCase()
                            .indexOf(trigger.toLowerCase()) + matcher.start();
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
    private static MutableText fixStyle(MutableText text, Style style)
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

    private static void printTree(Text message, int depth)
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
}
