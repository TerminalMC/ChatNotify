package notryken.chatnotify.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import notryken.chatnotify.config.Notification;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
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
                int[] match = msgContainsStr(strMsg, username);
                if (match != null) {
                    // Avoid username matching recent message content.
                    strMsg = strMsg.substring(0, match[0]) +
                            strMsg.substring(match[1]);
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
        for (Notification notif : config.getNotifs()) {
            if (notif.enabled) {
                if (notif.triggerIsKey) {
                    if (message.getContent() instanceof
                            TranslatableTextContent ttc) {
                        if (ttc.getKey().contains(notif.getTrigger())) {
                            playSound(notif);
                            modifiedMessage = simpleRestyle(message, notif);
                            break;
                        }
                    }
                } else {
                    for (String trigger : notif.getTriggers())
                    {
                        if (msgContainsStr(strMsg, trigger) != null) {
                            playSound(notif);
                            modifiedMessage =
                                    complexRestyle(message, trigger, notif);
                            break;
                        }
                    }
                    if (modifiedMessage != null) {
                        break;
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
     * @return An integer array [start,end] of str in msg, or null if not found.
     */
    private static int[] msgContainsStr(String msg, String str)
    {
        Matcher matcher = Pattern.compile("(?<!\\w)(\\W?(?i)" +
                Pattern.quote(str) + "\\W?)(?!\\w)", Pattern.CASE_INSENSITIVE)
                .matcher(msg);
        if (matcher.find()) {
            return new int[]{matcher.start(), matcher.end()};
        }
        return null;
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

    private static Style getStyle(Notification notif)
    {
        return Style.of(
                Optional.of(notif.getColor()),
                Optional.of(notif.getFormatControl(0)),
                Optional.of(notif.getFormatControl(1)),
                Optional.of(notif.getFormatControl(2)),
                Optional.of(notif.getFormatControl(3)),
                Optional.of(notif.getFormatControl(4)),
                Optional.empty(),
                Optional.empty());
    }

    private static Text simpleRestyle(Text message, Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            return message.copy().setStyle(getStyle(notif));
        }
        return message;
    }


    private static Text complexRestyle(Text oldMessage,
                                       String trigger,
                                       Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            MutableText message = oldMessage.copy();
            return processText(message, trigger, getStyle(notif));
        }
        return oldMessage;
    }

    private static MutableText processText(MutableText message,
                                           String trigger, Style style)
    {
        if (message.getContent() instanceof LiteralTextContent)
        {
            message = processLiteralTc(message, trigger, style);
        }
        else if (message.getContent() instanceof TranslatableTextContent ttc)
        {
            Object[] args = ttc.getArgs();
            for (int i = 0; i < ttc.getArgs().length; i++) {
                if (args[i] instanceof Text argText) {
                    args[i] = processText(argText.copy(), trigger, style);
                }
            }
            message = MutableText.of(new TranslatableTextContent(
                    ttc.getKey(), ttc.getFallback(), args));
        }
        else
        {
            List<Text> siblings = message.getSiblings();
            for (int i = 0; i < siblings.size(); i++) {
                siblings.set(i,
                        processText(siblings.get(i).copy(), trigger, style));
            }
        }
        return message;
    }

    private static MutableText processLiteralTc(MutableText message,
                                                String trigger, Style style)
    {
        if (message.getContent() instanceof LiteralTextContent ltc)
        {
            List<Text> siblings = message.getSiblings();

            Style oldStyle = message.getStyle();
            siblings.replaceAll(text -> fixStyle((MutableText) text, oldStyle));

            String msgStr = removeFormatCodes(ltc.string());

            int[] match = msgContainsStr(msgStr, trigger);
            if (match != null) {
                if (match[1] != msgStr.length()) {
                    siblings.add(0, Text.literal(
                                    msgStr.substring(match[1]))
                            .setStyle(message.getStyle()));
                }

                siblings.add(0, Text.literal(msgStr.substring(
                        match[0], match[1])).setStyle(style
                        .withClickEvent(message.getStyle().getClickEvent())
                        .withHoverEvent(message.getStyle().getHoverEvent())
                        .withInsertion(message.getStyle().getInsertion())));

                if (match[0] != 0) {
                    siblings.add(0, Text.literal(
                                    msgStr.substring(0, match[0]))
                            .setStyle(message.getStyle()));
                }

                if (siblings.size() == 1) {
                    message = siblings.get(0).copy();
                }
                else {
                    MutableText newMessage = MutableText.of(TextContent.EMPTY);
                    newMessage.siblings.addAll(siblings);
                    message = newMessage;
                }
            }
        }
        return message;
    }


    private static String removeFormatCodes(String string)
    {
        char[] charArray = string.toCharArray();
        StringBuilder cleanString = new StringBuilder();

        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '§') {
                i++; // Skip
            }
            else {
                cleanString.append(charArray[i]);
            }
        }
        return cleanString.toString();
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

    // Message analysis methods

    private static void analyseMessage(Text message)
    {
        try (PrintWriter pw = new PrintWriter(
                String.valueOf(System.currentTimeMillis())))
        {
            pw.println("=======================================\n\n\n\n");

            analyseText(pw, message, 0);

            pw.println("\n\n\n\n=======================================");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void analyseText(PrintWriter pw, Text message, int depth)
    {
        depth++;
        StringBuilder indent = new StringBuilder();
        indent.append(">>  ".repeat(depth));

        pw.println(indent + "-------------------------------------------\n\n");

        pw.println(indent + "message: " + message);
        pw.println(indent + "getString(): " + message.getString());
        pw.println(indent + "getContent(): " + message.getContent());
        pw.println(indent + "getStyle(): " + message.getStyle());
        pw.println(indent + "sibling count: " + message.getSiblings().size());

        if (message.getContent() instanceof TranslatableTextContent ttc) {
            pw.println(indent + "is TTC");
            analyseTranslatableTc(pw, ttc, depth);
        }
        else if (message.getContent() instanceof LiteralTextContent ltc) {
            pw.println(indent + "is LTC");
            analyseLiteralTc(pw, ltc, depth);
        }
        if (message.getSiblings().size() != 0) {
            pw.println(indent + "analysing siblings");
            for (Text sibling : message.getSiblings()) {
                analyseText(pw, sibling, depth);
            }
        }

        pw.println(indent + "\n\n-------------------------------------------");
    }

    private static void analyseTranslatableTc(PrintWriter pw,
                                              TranslatableTextContent ttc,
                                              int depth)
    {
        depth++;
        StringBuilder indent = new StringBuilder();
        indent.append(">>  ".repeat(depth));

        pw.println(indent + "-------------------------------------------\n\n");

        pw.println(indent + "ttc: " + ttc);
        pw.println(indent + "toString(): " + ttc.toString());
        pw.println(indent + "getKey(): " + ttc.getKey());
        pw.println(indent + "args Count: " + ttc.getArgs().length);
        pw.println(indent + "getArgs(): " + Arrays.toString(ttc.getArgs()));

        for (int i = 0; i < ttc.getArgs().length; i++) {
            if (ttc.getArgs()[i] instanceof Text argText) {
                analyseText(pw, argText, depth);
            }
            else {
                pw.println(indent + "arg " + ttc.getArgs()[i] + " not" +
                        " Text");
            }
        }

        pw.println(indent + "\n\n-------------------------------------------");
    }

    private static void analyseLiteralTc(PrintWriter pw,
                                         LiteralTextContent ltc,
                                         int depth)
    {
        depth++;
        StringBuilder indent = new StringBuilder();
        indent.append(">>  ".repeat(depth));

        pw.println(indent + "-------------------------------------------\n\n");

        pw.println(indent + "ltc: " + ltc);
        pw.println(indent + "toString(): " + ltc.toString());
        pw.println(indent + "string(): " + ltc.string());

        pw.println(indent + "\n\n-------------------------------------------");
    }
}
