package notryken.chatnotify.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import notryken.chatnotify.config.Notification;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static notryken.chatnotify.client.ChatNotifyClient.*;

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

        String plainMsg = TextVisitFactory.removeFormattingCodes(message);
        String processedMsg = preProcess(plainMsg);

        if (processedMsg != null) {
            tryNotify(message, plainMsg, processedMsg);
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
                int[] match = msgContainsStr(strMsg, username, false);
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
     * @param plainMsg The message string, stripped of format codes.
     * @param processedMsg The processed version of plainMsg.
     */
    private static void tryNotify(Text message, String plainMsg,
                                  String processedMsg)
    {
        for (Notification notif : config.getNotifs()) {
            if (notif.enabled) {
                if (notif.triggerIsKey) {
                    if (message.getContent() instanceof
                            TranslatableTextContent ttc) {
                        if (ttc.getKey().contains(notif.getTrigger())) {
                            playSound(notif);
                            modifiedMessage = simpleRestyle(message, notif);
                            sendResponseMessages(notif);
                            break;
                        }
                    }
                } else {
                    for (String trig : notif.getTriggers()) {
                        if (msgContainsStr(
                                (notif.regexEnabled ? plainMsg : processedMsg),
                                trig, notif.regexEnabled) != null)
                        {
                            boolean exclude = false;
                            for (String exTrig : notif.getExclusionTriggers()) {
                                if (msgContainsStr((notif.regexEnabled ?
                                        plainMsg : processedMsg), exTrig,
                                        notif.regexEnabled) != null)
                                {
                                    exclude = true;
                                    break;
                                }
                            }
                            if (!exclude) {
                                playSound(notif);
                                modifiedMessage = (notif.regexEnabled ?
                                        simpleRestyle(message, notif) :
                                        complexRestyle(message, trig, notif));
                                sendResponseMessages(notif);
                                break;
                            }
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
     * If strIsRegex is true, considers str to be a complete regular expression.
     * @param msg The message to search in.
     * @param str The string to search for.
     * @param strIsRegex Whether to wrap str in the regex
     *                   {@code "(?<!\w)(\W?(?i)" + str + "\W?)(?!\w)"} when
     *                   compiling.
     * @return An integer array [start,end] of str in msg, or null if not found.
     */
    private static int[] msgContainsStr(String msg, String str,
                                        boolean strIsRegex)
    {
        Matcher matcher;
        if (strIsRegex) {
            matcher = Pattern.compile(str).matcher(msg);
        }
        else {
            matcher = Pattern.compile("(?<!\\w)(\\W?(?i)" + Pattern.quote(str) +
                    "\\W?)(?!\\w)", Pattern.CASE_INSENSITIVE).matcher(msg);
        }
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

    private static void sendResponseMessages(Notification notif)
    {
        for (String response : notif.getResponseMessages()) {
            Screen oldScreen = MinecraftClient.getInstance().currentScreen;
            MinecraftClient.getInstance().setScreen(new ChatScreen(response));
            if (MinecraftClient.getInstance().currentScreen
                    instanceof ChatScreen cs)
            {
                cs.sendMessage(response, true);
            }
            MinecraftClient.getInstance().setScreen(oldScreen);
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
            return message.copy().setStyle(applyStyle(message.getStyle(),
                    getStyle(notif)));
        }
        return message;
    }

    private static Text complexRestyle(Text message, String trigger,
                                       Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            return processText(message.copy(), trigger, getStyle(notif));
        }
        return message;
    }

    private static MutableText processText(MutableText message, String trigger,
                                           Style style)
    {
        if (message.getContent() instanceof LiteralTextContent) {
            message = processLiteralTc(message, trigger, style);
        }
        else if (message.getContent() instanceof TranslatableTextContent ttc) {
            Object[] args = ttc.getArgs();
            for (int i = 0; i < ttc.getArgs().length; i++) {
                if (args[i] instanceof Text argText) {
                    args[i] = processText(argText.copy(), trigger, style);
                }
            }
            message = MutableText.of(new TranslatableTextContent(
                    ttc.getKey(), ttc.getFallback(), args));
        }
        else {
            message.getSiblings().replaceAll(text ->
                    processText(text.copy(), trigger, style));
        }
        return message;
    }

    private static MutableText processLiteralTc(MutableText message,
                                                String trigger, Style style)
    {
        if (message.getContent() instanceof LiteralTextContent ltc) {
            List<Text> siblings = message.getSiblings();

            Style oldStyle = message.getStyle();
            siblings.replaceAll(text ->
                    ((MutableText) text).setStyle(fixStyle(oldStyle, style)));

            String msgStr = removeFormatCodes(ltc.string());

            int[] match = msgContainsStr(msgStr, trigger, false);
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
     * For each non-null (or true) field of newStyle, overrides the
     * corresponding oldStyle field, returning the result. Note: if the color
     * of newStyle is 16777215 (white) it is considered null.
     * @param oldStyle The style to apply to.
     * @param newStyle The style to apply.
     * @return oldStyle, with newStyle applied.
     */
    private static Style applyStyle(Style oldStyle, Style newStyle)
    {
        Style result = oldStyle
                .withBold((newStyle.isBold() ||
                        oldStyle.isBold()))
                .withItalic((newStyle.isItalic() ||
                        oldStyle.isItalic()))
                .withUnderline((newStyle.isUnderlined() ||
                        oldStyle.isUnderlined()))
                .withStrikethrough((newStyle.isStrikethrough() ||
                        oldStyle.isStrikethrough()))
                .withObfuscated((newStyle.isObfuscated() ||
                        oldStyle.isObfuscated()));
        if (newStyle.getColor() != null &&
                !newStyle.getColor().equals(TextColor.fromRgb(16777215)))
        {
            result = result.withColor(newStyle.getColor());
        }
        if (newStyle.getClickEvent() != null) {
            result = result.withClickEvent(newStyle.getClickEvent());
        }
        if (newStyle.getHoverEvent() != null) {
            result = result.withHoverEvent(newStyle.getHoverEvent());
        }
        if (newStyle.getHoverEvent() != null) {
            result = result.withInsertion(newStyle.getInsertion());
        }
        if (newStyle.getFont() != null) {
            result = result.withFont(newStyle.getFont());
        }
        return result;
    }
    
    /**
     * Replaces the null (or false) fields of oldStyle with the corresponding
     * fields of newStyle.
     * @param oldStyle The style to fix.
     * @param newStyle The style to pull from.
     * @return oldStyle, with null/false fields replaced with the corresponding
     * newStyle field.
     */
    private static Style fixStyle(Style oldStyle, Style newStyle)
    {
        Style result = oldStyle
                .withBold((oldStyle.isBold() ||
                        newStyle.isBold()))
                .withItalic((oldStyle.isItalic() ||
                        newStyle.isItalic()))
                .withUnderline((oldStyle.isUnderlined() ||
                        newStyle.isUnderlined()))
                .withStrikethrough((oldStyle.isStrikethrough() ||
                        newStyle.isStrikethrough()))
                .withObfuscated((oldStyle.isObfuscated() ||
                        newStyle.isObfuscated()));
        if (oldStyle.getColor() == null) {
            result = result.withColor(newStyle.getColor());
        }
        if (oldStyle.getClickEvent() == null) {
            result = result.withClickEvent(newStyle.getClickEvent());
        }
        if (oldStyle.getHoverEvent() == null) {
            result = result.withHoverEvent(newStyle.getHoverEvent());
        }
        if (oldStyle.getInsertion() == null) {
            result = result.withInsertion(newStyle.getInsertion());
        }
        if (oldStyle.getFont() == null) {
            result = result.withFont(newStyle.getFont());
        }
        return result;
    }

    // Message analysis methods

//    private static void analyseMessage(Text message)
//    {
//        try (PrintWriter pw = new PrintWriter("message:" + 
//                System.currentTimeMillis()))
//        {
//            pw.println("=======================================\n\n\n\n");
//
//            analyseText(pw, message, 0);
//
//            pw.println("\n\n\n\n=======================================");
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void analyseText(PrintWriter pw, Text message, int depth)
//    {
//        depth++;
//        StringBuilder indent = new StringBuilder();
//        indent.append(">>  ".repeat(depth));
//
//        pw.println(indent + "------------------------------------------\n\n");
//
//        pw.println(indent + "message: " + message);
//        pw.println(indent + "getString(): " + message.getString());
//        pw.println(indent + "getContent(): " + message.getContent());
//        pw.println(indent + "getStyle(): " + message.getStyle());
//        pw.println(indent + "sibling count: " + message.getSiblings().size());
//
//        if (message.getContent() instanceof TranslatableTextContent ttc) {
//            pw.println(indent + "is TTC");
//            analyseTranslatableTc(pw, ttc, depth);
//        }
//        else if (message.getContent() instanceof LiteralTextContent ltc) {
//            pw.println(indent + "is LTC");
//            analyseLiteralTc(pw, ltc, depth);
//        }
//        if (message.getSiblings().size() != 0) {
//            pw.println(indent + "analysing siblings");
//            for (Text sibling : message.getSiblings()) {
//                analyseText(pw, sibling, depth);
//            }
//        }
//
//        pw.println(indent + "\n\n------------------------------------------");
//    }
//
//    private static void analyseTranslatableTc(PrintWriter pw,
//                                              TranslatableTextContent ttc,
//                                              int depth)
//    {
//        depth++;
//        StringBuilder indent = new StringBuilder();
//        indent.append(">>  ".repeat(depth));
//
//        pw.println(indent + "------------------------------------------\n\n");
//
//        pw.println(indent + "ttc: " + ttc);
//        pw.println(indent + "toString(): " + ttc.toString());
//        pw.println(indent + "getKey(): " + ttc.getKey());
//        pw.println(indent + "args Count: " + ttc.getArgs().length);
//        pw.println(indent + "getArgs(): " + Arrays.toString(ttc.getArgs()));
//
//        for (int i = 0; i < ttc.getArgs().length; i++) {
//            if (ttc.getArgs()[i] instanceof Text argText) {
//                analyseText(pw, argText, depth);
//            }
//            else {
//                pw.println(indent + "arg " + ttc.getArgs()[i] + " not" +
//                        " Text");
//            }
//        }
//
//        pw.println(indent + "\n\n------------------------------------------");
//    }
//
//    private static void analyseLiteralTc(PrintWriter pw,
//                                         LiteralTextContent ltc,
//                                         int depth)
//    {
//        depth++;
//        StringBuilder indent = new StringBuilder();
//        indent.append(">>  ".repeat(depth));
//
//        pw.println(indent + "------------------------------------------\n\n");
//
//        pw.println(indent + "ltc: " + ltc);
//        pw.println(indent + "toString(): " + ltc.toString());
//        pw.println(indent + "string(): " + ltc.string());
//
//        pw.println(indent + "\n\n------------------------------------------");
//    }
}
