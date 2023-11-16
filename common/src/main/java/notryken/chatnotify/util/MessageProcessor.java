package notryken.chatnotify.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.Constants;
import notryken.chatnotify.config.Config;
import notryken.chatnotify.config.Notification;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static notryken.chatnotify.ChatNotify.config;
import static notryken.chatnotify.ChatNotify.recentMessages;

public class MessageProcessor
{
    /**
     * Initiates the message processing algorithm.
     * @param message The original message.
     * @return A modified copy of the message, or the original if no modifying
     * was required.
     */
    public static Component processMessage(Component message)
    {
        Component modifiedMessage = null;

        //String plainMsg = StringDecomposer.getPlainText(message); // Removes formatting codes
        String plainMsg = message.getString();
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
        String strMsgLower = strMsg.toLowerCase(Locale.ROOT);
        Config config = ChatNotify.config();

        if (!recentMessages.isEmpty()) {
            for (int i = 0; i < recentMessages.size(); i++) {
                int match1 = strMsgLower.lastIndexOf(recentMessages.get(i).getSecond());
                if (match1 > 0) {
                    String prefix = strMsg.substring(0, match1);
                    for (String username : config.getNotif(0).getTriggers()) {
                        int[] match2 = msgContainsStr(prefix, username, false);
                        if (match2 != null) {
                            recentMessages.remove(i);
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
    private static Component tryNotify(Component message, String plainMsg,
                                  String processedMsg)
    {
        Constants.LOG.info("tryNotify message: " + message.getString());

        for (Notification notif : ChatNotify.config().getNotifs()) {
            if (notif.enabled) {
                if (notif.triggerIsKey) {
                    if (message.getContents() instanceof
                            TranslatableContents ttc) {
                        if (ttc.getKey().contains(notif.getTrigger())) {
                            playSound(notif);
                            sendResponseMessages(notif);
                            return simpleRestyle(message, notif);
                        }
                    }
                } else {
                    for (String trig : notif.getTriggers()) {

                        Constants.LOG.info("Checking trigger: " + trig);
                        if (!trig.isEmpty()) {
                            int[] match = msgContainsStr((notif.regexEnabled ? plainMsg : processedMsg), trig, notif.regexEnabled);
                            if (match == null) {
                                Constants.LOG.info("Match null");
                            }
                            else {
                                Constants.LOG.info("Match: [" + match[0] + "," + match[1] + "]");
                            }
                        }

                        if (!trig.isEmpty() && msgContainsStr(
                                (notif.regexEnabled ? plainMsg : processedMsg),
                                trig, notif.regexEnabled) != null)
                        {
                            boolean exclude = false;
                            if (notif.exclusionEnabled) {
                                for (String exTrig :
                                        notif.getExclusionTriggers())
                                {
                                    if (msgContainsStr((notif.regexEnabled ?
                                                    plainMsg : processedMsg),
                                            exTrig, notif.regexEnabled) != null)
                                    {
                                        exclude = true;
                                        break;
                                    }
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
//                matcher = Pattern.compile("(?<!\\w)(\\W?(?i)" +
//                        Pattern.quote(str) + "\\W?)(?!\\w)",
//                        Pattern.CASE_INSENSITIVE).matcher(msg);
//                matcher = Pattern.compile("(?<!\\w)((\\W|§[a-z0-9])?(?i)" +
//                                Pattern.quote(str) + "\\W?)(?!\\w)").matcher(msg);
                matcher = Pattern.compile("(?<!\\w)((\\W?|(§[a-z0-9])+)(?i)" +
                        Pattern.quote(str) + "\\W?)(?!\\w)").matcher(msg);
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
            Minecraft.getInstance().getSoundManager().play(
                    new SimpleSoundInstance(
                            notif.getSound(), config().notifSoundSource,
                            notif.soundVolume, notif.soundPitch,
                            SoundInstance.createUnseededRandom(), false, 0,
                            SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
    }

    /**
     * Sends all response messages of the specified notification, if enabled.
     * @param notif The notification.
     */
    private static void sendResponseMessages(Notification notif)
    {
        if (notif.responseEnabled) {
            for (String response : notif.getResponseMessages()) {
                Minecraft client = Minecraft.getInstance();
                Screen oldScreen = client.screen;
                client.setScreen(new ChatScreen(response));
                if (client.screen instanceof ChatScreen cs) {
                    cs.handleChatInput(response, true);
                }
                client.setScreen(oldScreen);
            }
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
        return Style.create(
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
    private static Component simpleRestyle(Component message, Notification notif)
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
    private static Component complexRestyle(Component message, String trigger,
                                       Notification notif)
    {
        if (notif.getControl(0) || notif.getControl(1)) {
            return processText(message.copy(), trigger, getStyle(notif));
        }
        return message;
    }

    private static MutableComponent processText(MutableComponent message, String trigger,
                                                Style style)
    {
        if (message.getContents() instanceof LiteralContents) {
            // LiteralContents is typically the lowest level.
            message = processLiteralTc(message, trigger, style);
        }
        else if (message.getContents() instanceof TranslatableContents ttc) {
            // Recurse for all args of the TranslatableContents.
            Object[] args = ttc.getArgs();
            for (int i = 0; i < ttc.getArgs().length; i++) {
                if (args[i] instanceof Component argText) {
                    args[i] = processText(argText.copy(), trigger, style);
                }
            }
            message = MutableComponent.create(new TranslatableContents(
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

    private static MutableComponent processLiteralTc(MutableComponent message,
                                                String trigger, Style style)
    {
        if (message.getContents() instanceof LiteralContents ltc) {

            // Only process if the message or its siblings contain the match.

            //String msgStr = removeFormatCodes(ltc.text()); // TODO stringdecomposer.getplaintext()?
            String msgStr = ltc.text();
            int[] match = msgContainsStr(msgStr, trigger, false);

            if (match != null)
            {
                List<Component> siblings = message.getSiblings();

                if (siblings.isEmpty())
                {
                    /*
                    If no siblings, the match must exist in the
                    LiteralContents (ltc) of the message, so it is split
                    down and the parts individually re-styled before being
                    re-built into a new Component object, which is returned.
                     */

                    Constants.LOG.info("processLiteralTc final stage");
                    Constants.LOG.info("msgStr: " + msgStr);
                    Constants.LOG.info("trigger: " + trigger);
                    Constants.LOG.info("match: " + match[0] + " (" + msgStr.charAt(match[0]) + "), " + (match[1]-1) + " (" + msgStr.charAt(match[1]-1) + ")");

                    List<ChatFormatting> appliedCodes = new ArrayList<>();

                    int len = match[1] - trigger.length();

                    Constants.LOG.info("Scanning for codes over length " + len + " (string: " + msgStr.substring(0, len) + ")");

                    for (int i = 0; i < len; i++) {
                        char c = msgStr.charAt(i);
                        if ((int)c == 167) {
                            char d = msgStr.charAt(i+1);
                            ChatFormatting format = ChatFormatting.getByCode(d);
                            if (format != null) {
                                if (format == ChatFormatting.RESET) {
                                    appliedCodes.clear();
                                }
                                else {
                                    if (format.isColor()) {
                                        appliedCodes.removeIf(ChatFormatting::isColor);
                                    }
                                    appliedCodes.add(format);
                                }
                            }
                        }
                    }

                    StringBuilder builder = new StringBuilder();
                    for (ChatFormatting cf : appliedCodes) {
                        builder.append('\u00a7');
                        builder.append(cf.getChar());
                    }

                    Constants.LOG.info("codes applied at trigger: " + builder);

                    String msgTriggerFull = msgStr.substring(match[0],match[1]);
                    int realStart = formatCodesEnd(trigger, msgTriggerFull);

                    String msgStart = msgStr.substring(0, match[0]);
                    String msgTrigger = realStart == -1 ? msgTriggerFull : msgTriggerFull.substring(realStart);
                    String msgEnd = msgStr.substring(match[1]);

                    Constants.LOG.info("actual trigger: " + msgTrigger);

                    msgStr = msgStart + '\u00a7' + 'r' + msgTrigger + builder + msgEnd;

                    Constants.LOG.info("edited string: " + msgStr);

                    match = new int[]{match[0],match[1]};

                    if (match[1] != msgStr.length()) {
                        siblings.add(0, Component.literal(
                                        msgStr.substring(match[1]))
                                .setStyle(message.getStyle()));
                    }

                    siblings.add(0, Component.literal(msgStr.substring(
                            match[0], match[1])).setStyle(
                                    applyStyle(message.getStyle(), style)));

                    if (match[0] != 0) {
                        siblings.add(0, Component.literal(
                                        msgStr.substring(0, match[0]))
                                .setStyle(message.getStyle()));
                    }

                    if (siblings.size() == 1) {
                        message = siblings.get(0).copy();
                    }
                    else {
                        MutableComponent newMessage = MutableComponent.create(ComponentContents.EMPTY);
                        newMessage.siblings.addAll(siblings);
                        message = newMessage;
                    }

                    for (Component c : message.getSiblings()) {
                        Constants.LOG.info("sibling: " + c.getString());
                    }

                    Constants.LOG.info("final string: " + message.getString());

                }
                else {
                    /*
                    If the message has siblings, it cannot be directly
                    re-styled. Instead, it is replaced by a new, empty Text
                    object, with the original LiteralContents added as
                    the first sibling, and all other siblings subsequently
                    added in their original order. The new message is then
                    itself processed, and the result is returned.
                     */

                    Style oldStyle = message.getStyle();

                    MutableComponent demoted = MutableComponent.create(message.getContents());
                    siblings.add(0, demoted);

                    MutableComponent replacement = MutableComponent.create(ComponentContents.EMPTY);
                    replacement.setStyle(oldStyle);
                    replacement.siblings.addAll(siblings);

                    message = processText(replacement, trigger, style);
                }
            }
        }
        return message;
    }

    private static int formatCodesEnd(String trigger, String msgTriggerFull) {
        Constants.LOG.info("msgTriggerFull: '" + msgTriggerFull + "'");
        char[] arr1 = msgTriggerFull.toCharArray();
        int realStart = -1;
        for (int i = 0; i < arr1.length-trigger.length(); i++) {
            if ((int)arr1[i] == 167 && (((int)arr1[i+1] > 47 && (int)arr1[i+1] < 58) ||
                    ((int)arr1[i+1] > 96 && (int)arr1[i+1] < 123))) {
                realStart = i+2;
                Constants.LOG.info("realStart: " + arr1[i+2] + " at " + (i+2));
            }
        }
        return realStart;
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
            if (charArray[i] == '\u00a7') {
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
                .withUnderlined((newStyle.isUnderlined() ||
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
        if (newStyle.getInsertion() != null) {
            result = result.withInsertion(newStyle.getInsertion());
        }
        if (newStyle.getFont() != Style.DEFAULT_FONT) {
            result = result.withFont(newStyle.getFont());
        }
        return result;
    }

    // Message analysis methods

//    private static void analyseMessage(String filePrefix, Component message)
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
//    private static void analyseText(PrintWriter pw, Component message, int depth)
//    {
//        depth++;
//        StringBuilder indent = new StringBuilder();
//        indent.append(">>  ".repeat(depth));
//
//        pw.println(indent + "------------------------------------------\n\n");
//
//        pw.println(indent + "message: " + message);
//        pw.println(indent + "getString(): " + message.getString());
//        pw.println(indent + "getContents(): " + message.getContents());
//        pw.println(indent + "getStyle(): " + message.getStyle());
//        pw.println(indent + "sibling count: " + message.getSiblings().size());
//
//        if (message.getContents() instanceof TranslatableContents ttc) {
//            pw.println(indent + "is TTC");
//            analyseTranslatableTc(pw, ttc, depth);
//        }
//        else if (message.getContents() instanceof LiteralContents ltc) {
//            pw.println(indent + "is LTC");
//            analyseLiteralTc(pw, ltc, depth);
//        }
//        if (message.getSiblings().size() != 0) {
//            pw.println(indent + "analysing siblings");
//            for (Component sibling : message.getSiblings()) {
//                analyseText(pw, sibling, depth);
//            }
//        }
//
//        pw.println(indent + "\n\n------------------------------------------");
//    }
//
//    private static void analyseTranslatableTc(PrintWriter pw,
//                                              TranslatableContents ttc,
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
//            if (ttc.getArgs()[i] instanceof Component argText) {
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
//                                         LiteralContents ltc,
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
