package notryken.chatnotify.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.*;
import notryken.chatnotify.config.Notification;

//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static notryken.chatnotify.client.ChatNotifyClient.*;

public class MessageProcessor
{
    /**
     * Initiates the message processing algorithm.
     * @param message The original message.
     * @return A modified copy of the message, or the original if no modifying
     * was required.
     */
    public static Text processMessage(Text message)
    {
        Text modifiedMessage = null;

        String plainMsg = TextVisitFactory.removeFormattingCodes(message);
        String processedMsg = preProcess(plainMsg);

        if (processedMsg != null) {
            modifiedMessage = tryNotify(message.copy(), plainMsg, processedMsg);
        }

//        if (modifiedMessage != null) {
//            analyseMessage("original", message);
//            analyseMessage("final", modifiedMessage);
//        }

        return (modifiedMessage == null ? message : modifiedMessage);
    }

    /**
     * strMsg is identified as being sent by the user if it contains one of the
     * stored recently-sent messages, and has a prefix containing (according to
     * msgContainsStr) one of the username-notification triggers.
     * If the message is identified as such, it is either set to null, if the
     * configuration ignoreOwnMessages is true, or the username-matched
     * component of the prefix is removed, if ignoreOwnMessages is false.
     * @param strMsg The message string to process.
     * @return The processed string (or null).
     */
    private static String preProcess(String strMsg)
    {
        if (!recentMessages.isEmpty()) {
            for (int i = 0; i < recentMessages.size(); i++) {
                int match1 = strMsg.lastIndexOf(recentMessages.get(i));
                if (match1 > 0) {
                    String prefix = strMsg.substring(0, match1);
                    for (String username : config.getNotif(0).getTriggers()) {
                        int[] match2 = msgContainsStr(prefix, username, false);
                        if (match2 != null) {
                            recentMessages.remove(i);
                            recentMessageTimes.remove(i);
                            if (config.ignoreOwnMessages) {
                                strMsg = null;
                            }
                            else {
                                strMsg = strMsg.substring(0, match2[0]) +
                                        strMsg.substring(match2[1]);
                            }
                            return strMsg;
                        }
                    }
                }
            }
        }
        return strMsg;
    }

    /**
     * Checks each trigger of each notification against one of the specified
     * forms of the message, depending on the notification's settings. When a
     * match is successful, completes the notification procedure by playing
     * the corresponding sound, sending the corresponding response messages,
     * and returning the re-styled version of the message.
     * @param message The original message.
     * @param plainMsg The message string, stripped of format codes.
     * @param processedMsg The pre-processed version of plainMsg.
     * @return The re-styled message, or null if no notification was triggered.
     */
    private static Text tryNotify(Text message, String plainMsg,
                                  String processedMsg)
    {
        for (Notification notif : config.getNotifs()) {
            if (notif.enabled) {
                if (notif.triggerIsKey) {
                    if (message.getContent() instanceof
                            TranslatableTextContent ttc) {
                        if (ttc.getKey().contains(notif.getTrigger())) {
                            playSound(notif);
                            sendResponseMessages(notif);
                            return simpleRestyle(message, notif);
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
                                sendResponseMessages(notif);
                                return (notif.regexEnabled ?
                                        simpleRestyle(message, notif) :
                                        complexRestyle(message, trig, notif));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * If strIsRegex is true, attempts to compile str as regex. Else, compiles
     * {@code "(?<!\w)(\W?(?i)" + Pattern.quote(str) + "\W?)(?!\w)"}
     * case-insensitive. Uses the compiled pattern to search msg.
     * @param msg The message to search in.
     * @param str The string or regex to search with.
     * @param strIsRegex Whether to consider str as a complete regex.
     * @return An integer array [start,end] of the match, or null if not found
     * or if strIsRegex is true and str does not represent a valid regex.
     */
    private static int[] msgContainsStr(String msg, String str,
                                        boolean strIsRegex)
    {
        try {
            Matcher matcher;
            if (strIsRegex) {
                matcher = Pattern.compile(str).matcher(msg);
            }
            else {
                matcher = Pattern.compile("(?<!\\w)(\\W?(?i)" +
                        Pattern.quote(str) + "\\W?)(?!\\w)",
                        Pattern.CASE_INSENSITIVE).matcher(msg);
            }
            if (matcher.find()) {
                return new int[]{matcher.start(), matcher.end()};
            }
        } catch (PatternSyntaxException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * Plays the sound of the specified notification.
     * @param notif The notification.
     */
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

    /**
     * Sends all response messages of the specified notification.
     * @param notif The notification.
     */
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

    /**
     * Constructs a Style from the specified notification's text color
     * and format fields.
     * @param notif The notification.
     * @return The resulting Style.
     */
    private static Style getStyle(Notification notif)
    {
        return Style.of(
                (notif.getColor() == null ? Optional.empty() :
                        Optional.of(notif.getColor())),
                Optional.of(notif.getFormatControl(0)),
                Optional.of(notif.getFormatControl(1)),
                Optional.of(notif.getFormatControl(2)),
                Optional.of(notif.getFormatControl(3)),
                Optional.of(notif.getFormatControl(4)),
                Optional.empty(),
                Optional.empty());
    }

    /**
     * If the specified notification's color or format controls are turned on,
     * uses applyStyle to destructively fill the style of the specified message
     * with the notification's style.
     * @param message The original message.
     * @param notif The notification to draw the style from.
     * @return The re-styled message.
     */
    private static Text simpleRestyle(Text message, Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            return message.copy().setStyle(applyStyle(message.getStyle(),
                    getStyle(notif)));
        }
        return message;
    }

    /**
     * If the specified notification's color or format controls are turned on,
     * initiates a recursive message break-down algorithm with the objective of
     * restyling only the part of the specified message that matches the
     * specified trigger.
     * @param message The original message.
     * @param trigger The string to re-style.
     * @param notif The notification to draw the style from.
     * @return The re-styled message.
     */
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
            // LiteralTextContent is typically the lowest level.
            message = processLiteralTc(message, trigger, style);
        }
        else if (message.getContent() instanceof TranslatableTextContent ttc) {
            // Recurse for all args of the TranslatableTextContent.
            Object[] args = ttc.getArgs();
            for (int i = 0; i < ttc.getArgs().length; i++) {
                if (args[i] instanceof Text argText) {
                    args[i] = processText(argText.copy(), trigger, style);
                }
            }
            message = MutableText.of(new TranslatableTextContent(
                    ttc.getKey(), ttc.getFallback(), args))
                    .setStyle(message.getStyle());
        }
        else {
            // Recurse for all siblings.
            message.getSiblings().replaceAll(text ->
                    processText(text.copy(), trigger, style));
        }
        return message;
    }

    private static MutableText processLiteralTc(MutableText message,
                                                String trigger, Style style)
    {
        if (message.getContent() instanceof LiteralTextContent ltc) {
            // Only process if the message or its siblings contain the match.
            if (msgContainsStr(removeFormatCodes(
                    message.getString()), trigger, false) != null)
            {
                List<Text> siblings = message.getSiblings();

                if (siblings.isEmpty())
                {
                    /*
                    If no siblings, the match must exist in the
                    LiteralTextContent (ltc) of the message, so it is split
                    down and the parts individually re-styled before being
                    re-built into a new Text object, which is returned.
                     */

                    String msgStr = removeFormatCodes(ltc.string());

                    int[] match = msgContainsStr(msgStr, trigger, false);
                    if (match != null) {
                        if (match[1] != msgStr.length()) {
                            siblings.add(0, Text.literal(
                                            msgStr.substring(match[1]))
                                    .setStyle(message.getStyle()));
                        }

                        siblings.add(0, Text.literal(msgStr.substring(
                                match[0], match[1])).setStyle(
                                        applyStyle(message.getStyle(), style)));

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
                else {
                    /*
                    If the message has siblings, it cannot be directly
                    re-styled. Instead, it is replaced by a new, empty Text
                    object, with the original LiteralTextContent added as
                    the first sibling, and all other siblings subsequently
                    added in their original order. The new message is then
                    itself processed, and the result is returned.
                     */

                    Style oldStyle = message.getStyle();

                    MutableText demoted = MutableText.of(message.getContent());
                    siblings.add(0, demoted);

                    MutableText replacement = MutableText.of(TextContent.EMPTY);
                    replacement.setStyle(oldStyle);
                    replacement.siblings.addAll(siblings);

                    message = processText(replacement, trigger, style);
                }
            }
        }
        return message;
    }

    /**
     * Uses basic iteration to remove all format codes (denoted by §) from
     * the specified string.
     * @param string The string to clean.
     * @return The specified string, with format codes removed.
     */
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
     * corresponding oldStyle field, returning the result.
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
        if (newStyle.getColor() != null) {
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

    // Message analysis methods

//    private static void analyseMessage(String filePrefix, Text message)
//    {
//        try (PrintWriter pw = new PrintWriter(filePrefix +
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
