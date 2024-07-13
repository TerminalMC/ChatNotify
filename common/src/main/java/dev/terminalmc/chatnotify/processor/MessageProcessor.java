/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.processor;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.terminalmc.chatnotify.ChatNotify.recentMessages;
import static dev.terminalmc.chatnotify.util.Localization.localized;

/*
 * Message processing algorithm, starting at processMessage.
 */
public class MessageProcessor {

    /**
     * Initiates the message processing algorithm.
     * @param msg The original message.
     * @return A modified copy of the message, or the original if no modifying
     * was required.
     */
    public static Component processMessage(Component msg) {
        switch(Config.get().debugShowKey.state) {
            case ON -> msg = addKeyInfo(msg);
            case OFF -> msg = addRawInfo(msg);
        }

        String msgStr = msg.getString();
        if (msgStr.isBlank()) return msg; // Ignore blank messages
        String checkedMsgStr = checkOwner(msgStr); // Null if ignoring message

        Component modifiedMsg = null;
        if (checkedMsgStr != null) {
            modifiedMsg = tryNotify(msg.copy(), msgStr, checkedMsgStr);
        }

        return (modifiedMsg == null ? msg : modifiedMsg);
    }

    /**
     * Determines whether a message was sent by the user and modifies it if
     * necessary to prevent unwanted notifications.
     *
     * <p>The message is identified as sent by the user if it contains a stored
     * message (or command) sent by the user, and the part preceding the stored
     * message contains a trigger of the username notification.
     *
     * <p>If the message is positively identified, it is set to {@code null} if
     * ChatNotify is configured to ignore such messages, else the part of the
     * prefix that matched a trigger is removed to prevent it being detected by
     * trigger search.
     * @param msgStr the message to check.
     * @return the message, modified message, or {@code null} depending on the
     * result of the check.
     */
    private static @Nullable String checkOwner(String msgStr) {
        // Stored messages are always converted to lowercase, convert to match.
        String msgStrLow = msgStr.toLowerCase(Locale.ROOT);
        // Check for a matching stored message
        for (int i = 0; i < recentMessages.size(); i++) {
            int lastMatchIdx = msgStrLow.lastIndexOf(recentMessages.get(i).getSecond());
            if (lastMatchIdx > 0) { // First condition satisfied
                // Check for a username trigger in the part before the match
                String prefix = msgStr.substring(0, lastMatchIdx);
                for (Trigger trigger : Config.get().getUserNotif().triggers) {
                    Matcher matcher = triggerSearch(prefix, trigger.string);
                    if (matcher.find()) { // Second condition satisfied
                        recentMessages.remove(i);
                        // Modify the message string
                        if (Config.get().checkOwnMessages) {
                            msgStr = msgStr.substring(0, matcher.start()) +
                                    msgStr.substring(matcher.end());
                        }
                        else {
                            msgStr = null;
                        }
                        return msgStr;
                    }
                }
            }
        }
        return msgStr;
    }

    /**
     * For each trigger of each enabled notification, checks whether the
     * trigger matches the message.
     *
     * <p>When a trigger matches, checks the exclusion triggers of the
     * notification to determine whether to activate the notification.
     *
     * <p>If the notification should be activated, completes the relevant
     * notification actions.
     *
     * <p><b>Note:</b> For performance and simplicity reasons, this method only
     * allows one notification to be triggered by a given message.
     * @param msg the original message.
     * @param msgStr the original message string.
     * @param checkedMsgStr the owner-checked message string.
     * @return a re-styled copy of the message, or null if no trigger matched.
     */
    private static Component tryNotify(Component msg, String msgStr, String checkedMsgStr) {
        boolean allowRegex = Config.get().allowRegex;
        for (Notification notif : Config.get().getNotifs()) {
            if (notif.isEnabled() && !notif.editing) {
                for (Trigger trigger : notif.triggers) {
                    if (!trigger.string.isBlank()) { // Guard
                        boolean hit;
                        Matcher matcher = null;
                        if (trigger.isKey) {
                            hit = keySearch(msg, trigger.string);
                        }
                        else if (allowRegex && trigger.isRegex) {
                            matcher = regexSearch(msgStr, trigger.string);
                            hit = matcher != null && matcher.find();
                        }
                        else {
                            hit = triggerSearch(checkedMsgStr, trigger.string).find();
                        }
                        if (hit) {
                            boolean exclHit = false;
                            for (Trigger exclTrigger : notif.exclusionTriggers) {
                                if (exclTrigger.isKey) {
                                    exclHit = keySearch(msg, exclTrigger.string);
                                }
                                else if (allowRegex && exclTrigger.isRegex) {
                                    Matcher exclMatcher = regexSearch(msgStr, exclTrigger.string);
                                    exclHit = exclMatcher != null && exclMatcher.find();
                                }
                                else {
                                    exclHit = triggerSearch(checkedMsgStr, exclTrigger.string).find();
                                }
                                if (exclHit) break;
                            }

                            if (!exclHit) {
                                playSound(notif);
                                sendResponses(notif, matcher);
                                String cleanMsgStr = StringUtil.stripColor(msgStr);
                                if (trigger.isKey || (allowRegex && trigger.isRegex)) {
                                    if (trigger.styleString != null && styleSearch(cleanMsgStr, trigger.styleString).find()) {
                                        return complexRestyle(msg, trigger.styleString, notif.textStyle);
                                    } else {
                                        return simpleRestyle(msg, notif.textStyle);
                                    }
                                } else {
                                    if (trigger.styleString != null && styleSearch(cleanMsgStr, trigger.styleString).find()) {
                                        return complexRestyle(msg, trigger.styleString, notif.textStyle);
                                    } else if (styleSearch(cleanMsgStr, trigger.string).find()) {
                                        return complexRestyle(msg, trigger.string, notif.textStyle);
                                    } else {
                                        return simpleRestyle(msg, notif.textStyle);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param msg the message to search.
     * @param key the key (or partial key) to search for.
     * @return {@code true} if the message matches the key, {@code false}
     * otherwise.
     */
    private static boolean keySearch(Component msg, String key) {
        boolean hit = false;
        if (key.equals(".")) {
            hit = true;
        }
        else if (msg.getContents() instanceof TranslatableContents tc) {
            if (tc.getKey().contains(key)) {
                hit = true;
            }
        }
        return hit;
    }

    /**
     * @param msgStr the message to search.
     * @param str the string to search for.
     * @return the {@link Matcher} for the search.
     */
    private static Matcher triggerSearch(String msgStr, String str) {
        /*
        U flag for full unicode comparison, performance using randomly-generated
        100-character msgStr and 10-character str is approx 1.18 microseconds
        per check without flag, 1.31 microseconds with.
         */
        return Pattern.compile(
                "(?iU)(?<!\\w)((\\W?|(§[a-z0-9])+)" + Pattern.quote(str) + "\\W?)(?!\\w)")
                .matcher(msgStr);
    }

    private static Matcher styleSearch(String msgStr, String str) {
        return Pattern.compile("(?iU)" + Pattern.quote(str)).matcher(msgStr);
    }

    /**
     * @param msgStr the message to search.
     * @param patternStr the pattern string to search for.
     * @return  the {@link Matcher} for the search, if the pattern string was
     * compilable, {@code null} otherwise.
     */
    private static @Nullable Matcher regexSearch(String msgStr, String patternStr) {
        try {
            return Pattern.compile(patternStr).matcher(msgStr);
        } catch (PatternSyntaxException e) {
            ChatNotify.LOG.warn("ChatNotify: Error processing regex: " + e);
        }
        return null;
    }

    /**
     * Plays the sound of the specified notification, if the relevant control is
     * enabled.
     * @param notif the Notification.
     */
    private static void playSound(Notification notif) {
        if (notif.sound.isEnabled()) {
            Minecraft.getInstance().getSoundManager().play(
                    new SimpleSoundInstance(
                            notif.sound.getResourceLocation(), Config.get().soundSource,
                            notif.sound.getVolume(), notif.sound.getPitch(),
                            SoundInstance.createUnseededRandom(), false, 0,
                            SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
    }

    /**
     * Sends all response messages of the specified notification, if the
     * relevant control is enabled.
     * @param notif the Notification.
     */
    private static void sendResponses(Notification notif, @Nullable Matcher matcher) {
        if (notif.responseEnabled) {
            boolean allowRegex = Config.get().allowRegex;
            Minecraft minecraft = Minecraft.getInstance();
            Screen oldScreen = minecraft.screen;
            minecraft.setScreen(new ChatScreen(""));
            for (ResponseMessage msg : notif.responseMessages) {
                msg.sendingString = msg.string;
                if (matcher != null && allowRegex && msg.regexGroups) {
                    // Capturing group substitution
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                        msg.sendingString = msg.sendingString.replace("(" + i + ")", matcher.group(i));
                    }
                }
                msg.countdown = msg.delayTicks;
                ChatNotify.responseMessages.add(msg);
            }
            minecraft.setScreen(oldScreen);
        }
    }

    /**
     * Destructively fills the style of the message with the specified
     * TextStyle.
     * @param msg the message to restyle.
     * @param style the TextStyle to apply.
     * @return the restyled message.
     */
    private static Component simpleRestyle(Component msg, TextStyle style) {
        if (style.isEnabled()) {
            msg = msg.copy().setStyle(applyStyle(msg.getStyle(), style));
        }
        return msg;
    }

    /**
     * Uses a recursive break-down algorithm to apply the specified TextStyle to
     * only the part of the message that matches the trigger.
     * @param msg the message to restyle.
     * @param trigger the string to restyle within the message.
     * @param style the TextStyle to apply.
     * @return the restyled message.
     */
    private static Component complexRestyle(Component msg, String trigger, TextStyle style) {
        if (style.isEnabled()) {
            msg = restyleComponent(msg.copy(), trigger, style);
        }
        return msg;
    }

    /**
     * Recursively deconstructs the message to find the trigger and apply the
     * specified style to it.
     * @param msg the message to restyle.
     * @param trigger the string to restyle within the message.
     * @param style the TextStyle to apply.
     * @return the restyled message.
     */
    private static MutableComponent restyleComponent(MutableComponent msg, String trigger,
                                                     TextStyle style) {

        if (msg.getContents() instanceof PlainTextContents) {
            // PlainTextContents is typically the lowest level
            msg = restyleContents(msg, trigger, style);
        }
        else if (msg.getContents() instanceof TranslatableContents contents) {
            // Recurse for all args
            Object[] args = contents.getArgs();
            for (int i = 0; i < contents.getArgs().length; i++) {
                if (args[i] instanceof Component argComponent) {
                    args[i] = restyleComponent(argComponent.copy(), trigger, style);
                }
                else if (args[i] instanceof String argString) {
                    args[i] = restyleComponent(Component.literal(argString), trigger, style);
                }
            }
            // Reconstruct
            msg = MutableComponent.create(new TranslatableContents(
                    contents.getKey(), contents.getFallback(), args))
                    .setStyle(msg.getStyle());
        }
        else {
            // Recurse for all siblings
            msg.getSiblings().replaceAll(text -> restyleComponent(text.copy(), trigger, style));
        }
        return msg;
    }

    /**
     * If the message contents is {@link PlainTextContents}, deconstructs,
     * restyles and reconstructs the message with the objective of applying
     * the specified style only to occurrences of the trigger.
     * @param msg the message to restyle.
     * @param trigger the string to restyle within the message.
     * @param style the TextStyle to apply.
     * @return the restyled message.
     */
    private static MutableComponent restyleContents(MutableComponent msg, String trigger,
                                                    TextStyle style) {
        if (!(msg.getContents() instanceof PlainTextContents contents)) return msg;

        String msgStr = contents.text();
        Matcher matcher = styleSearch(msgStr, trigger);
        if (matcher.find()) {
            // Trigger found, restyle
            List<Component> siblings = msg.getSiblings();

            if (siblings.isEmpty()) {
                // No siblings means that the trigger must exist at this level.

                int start = matcher.start();
                int end = matcher.end();

                // Some magic to deal with format codes
                if (msgStr.contains("§")) {
                    String activeCodes = activeFormatCodes(msgStr.substring(0, end-trigger.length()));

                    String msgTriggerFull = msgStr.substring(start,end);
                    int realStart = startIgnoreCodes(msgTriggerFull,
                            msgTriggerFull.length() - trigger.length());

                    String msgStart = msgStr.substring(0, start);
                    String msgTrigger = msgTriggerFull.substring(realStart);
                    String msgEnd = msgStr.substring(end);

                    msgStr = msgStart + '\u00a7' + 'r' + msgTrigger + activeCodes + msgEnd;

                    end = end-realStart+2;
                }

                // msgStr before match
                if (start != 0) {
                    siblings.add(Component.literal(msgStr.substring(0, start))
                            .setStyle(msg.getStyle()));
                }

                // Match
                siblings.add(Component.literal(msgStr.substring(start, end))
                        .setStyle(applyStyle(msg.getStyle(), style)));

                // msgStr after match
                if (end != msgStr.length()) {
                    siblings.add(Component.literal(msgStr.substring(end))
                            .setStyle(msg.getStyle()));
                }

                if (siblings.size() == 1) {
                    msg = siblings.getFirst().copy();
                }
                else {
                    MutableComponent newMessage = MutableComponent.create(PlainTextContents.EMPTY);
                    newMessage.siblings.addAll(siblings);
                    msg = newMessage;
                }
            }
            else {
                // Unable to restyle without affecting siblings, so add contents
                // as first sibling of a new MutableComponent, followed by other
                // siblings in original order, then restyle that.

                MutableComponent replacement = MutableComponent.create(PlainTextContents.EMPTY);
                replacement.setStyle(msg.getStyle());

                siblings.addFirst(MutableComponent.create(msg.getContents()));
                replacement.siblings.addAll(siblings);

                msg = restyleComponent(replacement, trigger, style);
            }
        }
        else {
            // Trigger not found, try siblings
            msg.getSiblings().replaceAll(text -> restyleComponent(text.copy(), trigger, style));
        }
        return msg;
    }

    /**
     * Scans the specified string and identifies any format codes that are
     * active at the end.
     * @param msgStr the string to scan.
     * @return the active format codes as a string.
     */
    private static String activeFormatCodes(String msgStr) {
        List<ChatFormatting> activeCodes = new ArrayList<>();
        for (int i = 0; i < msgStr.length(); i++) {
            char c = msgStr.charAt(i);
            if ((int)c == 167) {
                char d = msgStr.charAt(i+1);
                ChatFormatting format = ChatFormatting.getByCode(d);
                if (format != null) {
                    if (format == ChatFormatting.RESET) {
                        activeCodes.clear();
                    }
                    else {
                        if (format.isColor()) {
                            activeCodes.removeIf(ChatFormatting::isColor);
                        }
                        activeCodes.add(format);
                    }
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for (ChatFormatting cf : activeCodes) {
            builder.append('\u00a7');
            builder.append(cf.getChar());
        }
        return builder.toString();
    }

    /**
     * Workaround method; if a string passed to strSearch() contains format
     * codes immediately preceding a match, the range returned will include the
     * format codes.
     *
     * <p>This method scans the specified string to determine the start
     * of the actual match, defined as being the first character after the
     * last format code.<p>
     * @param str the string with possible format codes.
     * @param maxStart the maximum possible value of the match start.
     * @return the index of the first character after the last format code.
     */
    private static int startIgnoreCodes(String str, int maxStart) {
        char[] arr1 = str.toCharArray();
        int realStart = 0;
        for (int i = 0; i <= maxStart; i++) {
            if ((int)arr1[i] == 167 && (((int)arr1[i+1] > 47 && (int)arr1[i+1] < 58) ||
                    ((int)arr1[i+1] > 96 && (int)arr1[i+1] < 123))) {
                realStart = i+2;
            }
        }
        return realStart;
    }

    /**
     * For each enabled field of the specified TextStyle, overrides the
     * corresponding Style field.
     * @param style the Style to apply to.
     * @param textStyle the TextStyle to apply.
     * @return the Style, with the TextStyle applied.
     */
    private static Style applyStyle(Style style, TextStyle textStyle) {
        if (textStyle.bold.isEnabled()) style = style.withBold(textStyle.bold.isOn());
        if (textStyle.italic.isEnabled()) style = style.withItalic(textStyle.italic.isOn());
        if (textStyle.underlined.isEnabled()) style = style.withUnderlined(textStyle.underlined.isOn());
        if (textStyle.strikethrough.isEnabled()) style = style.withStrikethrough(textStyle.strikethrough.isOn());
        if (textStyle.obfuscated.isEnabled()) style = style.withObfuscated(textStyle.obfuscated.isOn());
        if (textStyle.doColor) style = style.withColor(textStyle.getTextColor());
        return style;
    }

    // Debug utils

    public static Component addKeyInfo(Component msg) {
        Style newStyle;
        // Create new Hover and Click events
        if (msg.getContents() instanceof TranslatableContents tc) {
            newStyle = Style.EMPTY
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            localized("chat", "message.translation_key.value", tc.getKey())
                                    .append("\n").append(localized("common", "click_copy")
                                            .withStyle(ChatFormatting.GOLD))))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                            tc.getKey()));
        } else {
            newStyle = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    localized("chat", "message.translation_key.missing")
                            .withStyle(ChatFormatting.GRAY)));
        }
        // Overwrite existing events
        return overwriteStyle(newStyle, msg.copy());
    }

    public static Component addRawInfo(Component msg) {
        Style newStyle;
        // Create new Hover and Click events
        newStyle = Style.EMPTY.
                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        localized("common", "click_copy.raw").withStyle(ChatFormatting.GOLD)))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                        msg.toString()));

        // Overwrite existing events
        return overwriteStyle(newStyle, msg.copy());
    }

    public static MutableComponent overwriteStyle(Style style, MutableComponent msg) {
        if (msg.getContents() instanceof TranslatableContents tc) {
            Object[] args = tc.getArgs();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component c) {
                    args[i] = overwriteStyle(style, c.copy());
                }
//                else if (args[i] instanceof String s) {
//                    args[i] = Component.literal(s).setStyle(style);
//                }
                msg = MutableComponent.create(
                        new TranslatableContents(tc.getKey(), tc.getFallback(), args))
                        .setStyle(style.applyTo(msg.getStyle()));
            }
        } else {
            msg.setStyle(style.applyTo(msg.getStyle()));
            msg.getSiblings().replaceAll((sibling) -> overwriteStyle(style, sibling.copy()));
        }
        return msg;
    }
}
