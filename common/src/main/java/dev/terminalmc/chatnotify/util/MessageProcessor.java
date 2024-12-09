/*
 * Copyright 2024 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.util;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.*;
import dev.terminalmc.chatnotify.mixin.accessor.MutableComponentAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

import static dev.terminalmc.chatnotify.ChatNotify.recentMessages;
import static dev.terminalmc.chatnotify.util.Localization.localized;

public class MessageProcessor {

    /**
     * Initiates the message processing algorithm.
     * @param msg The original message.
     * @return A modified copy of the message, or the original if no modifying
     * was required.
     */
    public static @Nullable Component processMessage(Component msg) {
        switch(Config.get().debugMode) {
            case KEY -> msg = addKeyInfo(msg);
            case TEXT -> msg = addTextInfo(msg);
            case RAW -> msg = addRawInfo(msg);
        }

        String str = msg.getString();
        if (str.isBlank()) return msg; // Ignore blank messages
        String checkedStr = checkOwner(str); // Null if ignoring message

        return (checkedStr == null ? msg : tryNotify(msg.copy(), str, checkedStr));
    }

    /**
     * Determines whether a message was sent by the user and modifies it if
     * necessary to prevent unwanted notifications.
     *
     * <p>The message is identified as sent by the user if it contains a stored
     * message (or command) sent by the user, and the part preceding the stored
     * message contains a trigger of the username notification.</p>
     *
     * <p>If the message is positively identified, it is set to {@code null} if
     * ChatNotify is configured to ignore such messages, else the part of the
     * prefix that matched a trigger is removed to prevent it being detected by
     * trigger search.</p>
     * @param msg the message to check.
     * @return the message, a modified copy, or {@code null} depending on the
     * result of the check.
     */
    private static @Nullable String checkOwner(String msg) {
        // Stored messages are always converted to lowercase, convert to match
        String msgStrLow = msg.toLowerCase(Locale.ROOT);
        // Check for a matching stored message
        for (int i = 0; i < recentMessages.size(); i++) {
            int lastMatchIdx = msgStrLow.lastIndexOf(recentMessages.get(i).getSecond());
            if (lastMatchIdx > 0) { // Matched against a stored message
                // Check for a username trigger in the part before the match
                String prefix = msg.substring(0, lastMatchIdx);
                for (Trigger trigger : Config.get().getUserNotif().triggers) {
                    Matcher matcher = normalSearch(prefix, trigger.string);
                    if (matcher.find()) { // Matched against a username trigger
                        recentMessages.remove(i);
                        return Config.get().checkOwnMessages
                                ? msg.substring(0, matcher.start())
                                        + msg.substring(matcher.end())
                                : null;
                    }
                }
            }
        }
        return msg;
    }

    /**
     * For each trigger of each enabled notification, checks whether the
     * trigger matches the message.
     *
     * <p>When a trigger matches, checks the exclusion triggers of the
     * notification to determine whether to activate the notification.</p>
     *
     * <p>If the notification should be activated, completes the relevant
     * notification actions.</p>
     *
     * <p><b>Note:</b> For performance and simplicity reasons, this method only
     * allows one notification to be triggered by a given message.</p>
     * @param msg the original message.
     * @param str the original message string.
     * @param checkedStr the owner-checked message string.
     * @return a re-styled copy of the message, or the original message if
     * restyling was not possible.
     */
    private static Component tryNotify(Component msg, String str, String checkedStr) {
        for (Notification notif : Config.get().getNotifs()) {
            if (!notif.isEnabled() || notif.editing) continue;
            for (Trigger trig : notif.triggers) {
                if (trig.string.isBlank()) continue;

                // Trigger search
                boolean hit = false;
                Matcher matcher = null;
                switch(trig.type) {
                    case NORMAL -> hit = normalSearch(checkedStr, trig.string).find();
                    case REGEX -> {
                        if (trig.pattern != null) {
                            matcher = trig.pattern.matcher(str);
                            hit = matcher.find();
                        }
                    }
                    case KEY -> hit = keySearch(msg, trig.string);
                }
                if (!hit) continue;

                // Exclusion search
                boolean exHit = false;
                for (Trigger exTrig : notif.exclusionTriggers) {
                    // Search exclusions
                    switch(exTrig.type) {
                        case NORMAL -> exHit = normalSearch(checkedStr, exTrig.string).find();
                        case REGEX -> {
                            if (exTrig.pattern != null) {
                                matcher = exTrig.pattern.matcher(str);
                                exHit = matcher.find();
                            }
                        }
                        case KEY -> exHit = keySearch(msg, exTrig.string);
                    }
                    if (exHit) break;
                }
                if (exHit) continue;
                
                // Activate notification
                
                if (notif.blockMessage) {
                    ChatNotify.LOG.info("Blocked message '{}' due to trigger '{}'", 
                            msg.getString(), trig.string);
                    return null;
                }
                
                playSound(notif);
                showTitle(notif);
                sendResponses(notif, matcher);

                String cleanStr = StringUtil.stripColor(str);
                if (trig.type != Trigger.Type.NORMAL) {
                    if (trig.styleString != null && styleSearch(cleanStr, trig.styleString).find()) {
                        return complexRestyle(msg, trig.styleString, notif.textStyle);
                    } else {
                        return simpleRestyle(msg, notif.textStyle);
                    }
                } else {
                    if (trig.styleString != null && styleSearch(cleanStr, trig.styleString).find()) {
                        return complexRestyle(msg, trig.styleString, notif.textStyle);
                    } else if (styleSearch(cleanStr, trig.string).find()) {
                        return complexRestyle(msg, trig.string, notif.textStyle);
                    } else {
                        return simpleRestyle(msg, notif.textStyle);
                    }
                }
            }
        }
        return msg;
    }

    /**
     * @param msg the message to search.
     * @param key the key (or partial key) to search for.
     * @return {@code true} if the message matches the key, {@code false}
     * otherwise.
     */
    private static boolean keySearch(Component msg, String key) {
        if (key.equals(".")) {
            return true;
        } else if (msg.getContents() instanceof TranslatableContents tc) {
            return tc.getKey().contains(key);
        }
        return false;
    }

    /**
     * Performs a slightly 'fuzzy' search for the string within the message.
     * @param msg the message to search.
     * @param str the string to search for.
     * @return the {@link Matcher} for the search.
     */
    private static Matcher normalSearch(String msg, String str) {
        /*
        U flag for full unicode comparison, performance using randomly-generated
        100-character msg and 10-character str is approx 1.18 microseconds
        per check without flag, 1.31 microseconds with.
         */
        return Pattern.compile(
                "(?iU)(?<!\\w)((\\W?|(§[a-z0-9])+)" + Pattern.quote(str) + "\\W?)(?!\\w)")
                .matcher(msg);
    }

    /**
     * Performs a case-insensitive search for the string within the message.
     * @param msg the message to search.
     * @param str the string to search for.
     * @return the {@link Matcher} for the search.
     */
    private static Matcher styleSearch(String msg, String str) {
        return Pattern.compile("(?iU)" + Pattern.quote(str)).matcher(msg);
    }

    /**
     * Plays the sound of the specified notification, if the relevant control is
     * enabled.
     * @param notif the Notification.
     */
    private static void playSound(Notification notif) {
        if (notif.sound.isEnabled() && notif.sound.getVolume() > 0) {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                    notif.sound.getResourceLocation(), Config.get().soundSource,
                    notif.sound.getVolume(), notif.sound.getPitch(),
                    SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
    }

    /**
     * Displays the title text for the notification, if enabled.
     * @param notif the Notification.
     */
    private static void showTitle(Notification notif) {
        if (notif.titleText.isEnabled()) {
            Minecraft.getInstance().gui.setTitle(
                    Component.literal(notif.titleText.text).withColor(notif.titleText.color));
        }
    }

    /**
     * Sends all response messages of the specified notification, if the
     * relevant control is enabled.
     * @param notif the Notification.
     */
    private static void sendResponses(Notification notif, @Nullable Matcher matcher) {
        if (notif.responseEnabled) {
            int totalDelay = 0;
            for (ResponseMessage msg : notif.responseMessages) {
                msg.sendingString = msg.string;
                if (matcher != null && msg.type.equals(ResponseMessage.Type.REGEX)) {
                    // Capturing group substitution
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                        msg.sendingString = msg.sendingString.replace("(" + i + ")", matcher.group(i));
                    }
                }
                totalDelay += msg.delayTicks;
                msg.countdown = totalDelay;
                ChatNotify.responseMessages.add(msg);
            }
        }
    }

    /**
     * Destructively fills the style of the message with the specified
     * {@link TextStyle}.
     * @param msg the message to restyle.
     * @param style the {@link TextStyle} to apply.
     * @return the restyled message.
     */
    private static Component simpleRestyle(Component msg, TextStyle style) {
        if (style.isEnabled()) {
            msg = msg.copy().setStyle(applyStyle(msg.getStyle(), style));
        }
        return msg;
    }

    /**
     * Uses a recursive break-down algorithm to apply the specified
     * {@link TextStyle} to only the part of the message that matches the
     * trigger.
     * @param msg the message to restyle.
     * @param str the string to restyle within the message.
     * @param style the {@link TextStyle} to apply.
     * @return the restyled message.
     */
    private static Component complexRestyle(Component msg, String str, TextStyle style) {
        if (style.isEnabled()) {
            msg = restyleComponent(msg.copy(), str, style);
        }
        return msg;
    }

    /**
     * Recursively deconstructs the message to find the string and apply the
     * specified style to it.
     * @param msg the message to restyle.
     * @param str the string to restyle within the message.
     * @param style the {@link TextStyle} to apply.
     * @return the restyled message.
     */
    private static MutableComponent restyleComponent(
            MutableComponent msg, String str, TextStyle style) {
        if (msg.getContents() instanceof PlainTextContents) {
            // PlainTextContents is typically the lowest level
            msg = restyleContents(msg, str, style);
        }
        else if (msg.getContents() instanceof TranslatableContents contents) {
            // Recurse for all args
            Object[] args = contents.getArgs();
            for (int i = 0; i < contents.getArgs().length; i++) {
                if (args[i] instanceof Component argComponent) {
                    args[i] = restyleComponent(argComponent.copy(), str, style);
                }
                else if (args[i] instanceof String argString) {
                    args[i] = restyleComponent(Component.literal(argString), str, style);
                }
            }
            // Reconstruct
            msg = MutableComponent.create(new TranslatableContents(
                    contents.getKey(), contents.getFallback(), args))
                    .setStyle(msg.getStyle());
        }
        else {
            // Recurse for all siblings
            msg.getSiblings().replaceAll(text -> restyleComponent(text.copy(), str, style));
        }
        return msg;
    }

    /**
     * If the message contents is {@link PlainTextContents}, deconstructs,
     * restyles and reconstructs the message with the objective of applying
     * the specified style only to occurrences of the string.
     * @param msg the message to restyle.
     * @param str the string to restyle within the message.
     * @param style the {@link TextStyle} to apply.
     * @return the restyled message.
     */
    private static MutableComponent restyleContents(MutableComponent msg, String str,
                                                    TextStyle style) {
        if (!(msg.getContents() instanceof PlainTextContents contents)) return msg;

        String msgStr = contents.text();
        Matcher matcher = styleSearch(msgStr, str);
        if (matcher.find()) {
            // Trigger found, restyle
            List<Component> siblings = msg.getSiblings();

            if (siblings.isEmpty()) {
                // No siblings means that the trigger must exist at this level.

                int start = matcher.start();
                int end = matcher.end();

                // Some magic to deal with format codes
                if (msgStr.contains("§")) {
                    String activeCodes = activeFormatCodes(msgStr.substring(0, end-str.length()));

                    String msgTriggerFull = msgStr.substring(start,end);
                    int realStart = startIgnoreCodes(msgTriggerFull,
                            msgTriggerFull.length() - str.length());

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
                    ((MutableComponentAccessor)newMessage).getSiblings().addAll(siblings);
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
                ((MutableComponentAccessor)replacement).getSiblings().addAll(siblings);

                msg = restyleComponent(replacement, str, style);
            }
        }
        else {
            // Trigger not found, try siblings
            msg.getSiblings().replaceAll(text -> restyleComponent(text.copy(), str, style));
        }
        return msg;
    }

    /**
     * Scans the specified string and identifies any format codes that are
     * active at the end.
     * @param str the string to scan.
     * @return the active format codes as a string.
     */
    private static String activeFormatCodes(String str) {
        List<ChatFormatting> activeCodes = new ArrayList<>();
        for (int i = 0; i < str.length() - 1; i++) {
            char c = str.charAt(i);
            if ((int)c == 167) {
                char d = str.charAt(i+1);
                ChatFormatting format = ChatFormatting.getByCode(d);
                if (format != null) {
                    if (format == ChatFormatting.RESET) {
                        activeCodes.clear();
                    } else {
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
     * Workaround method; if a string passed to
     * {@link MessageProcessor#normalSearch} contains format codes immediately
     * preceding a match, the range returned will include the format codes.
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
     * For each enabled field of the specified {@link TextStyle}, overrides the
     * corresponding {@link Style} field.
     * @param style the {@link Style} to apply to.
     * @param textStyle the {@link TextStyle} to apply.
     * @return the {@link Style}, with the {@link TextStyle} applied.
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

    public static Component addTextInfo(Component msg) {
        Style newStyle;
        // Create new Hover and Click events
        newStyle = Style.EMPTY.
                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        localized("common", "click_copy.text").withStyle(ChatFormatting.GOLD)))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                        msg.getString()));

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
