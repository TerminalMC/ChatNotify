/*
 * Copyright 2025 TerminalMC
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.ChatNotify.recentMessages;

public class MessageUtil {
    private static boolean debug = false;
    
    /**
     * Initiates the message processing algorithm.
     * @param msg The original message.
     * @return A modified copy of the message, or the original if no modifying
     * was required.
     */
    public static @Nullable Component processMessage(Component msg) {
        debug = Config.get().debugMode.equals(Config.DebugMode.ALL);
        
        String str = msg.getString();
        if (str.isBlank()) return msg; // Ignore blank messages
        
        // Save message for trigger editor
        if (ChatNotify.unmodifiedChat.size() > 30) ChatNotify.unmodifiedChat.poll();
        ChatNotify.unmodifiedChat.add(msg);
        
        if (debug) {
            ChatNotify.LOG.warn("Original Message");
            ChatNotify.LOG.warn(msg.toString());
        }

        // Remove format codes from string before searching
        String cleanStr = FormatUtil.stripCodes(str);
        
        // Check owner
        String cleanOwnedStr = checkOwner(cleanStr);
        if (cleanOwnedStr == null) {
            // Message was identified as being sent by the user and config
            // is set to ignore such messages, so return the original
            return msg;
        }
        
        // Process notifications
        msg = tryNotify(msg.copy(), cleanStr, cleanOwnedStr);

        if (debug) {
            ChatNotify.LOG.warn("Modified Message");
            ChatNotify.LOG.warn(msg == null ? "null" : msg.toString());
        }
        
        return msg;
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
        String msgLow = msg.toLowerCase(Locale.ROOT);
        // Check for a matching stored message
        for (int i = 0; i < recentMessages.size(); i++) {
            int lastMatchIdx = msgLow.lastIndexOf(recentMessages.get(i).getSecond());
            if (lastMatchIdx > 0) {
                // Matched against a stored message
                // Check for a username trigger in the part before the match
                String prefix = msg.substring(0, lastMatchIdx);
                for (Trigger trigger : Config.get().getUserNotif().triggers) {
                    Matcher matcher = normalSearch(prefix, trigger.string);
                    if (matcher.find()) { 
                        // Matched against a username trigger
                        // Remove stored message
                        recentMessages.remove(i);
                        // Modify message according to config
                        return Config.get().checkOwnMessages 
                                ? msg.substring(0, matcher.start()) + msg.substring(matcher.end()) 
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
     * @param msg the message.
     * @param cleanStr the message string, with all format codes removed.
     * @param cleanOwnedStr cleanStr, with the sender removed if applicable.
     * @return a re-styled copy of the message, or the original message if
     * restyling was not possible.
     */
    private static @Nullable Component tryNotify(Component msg, String cleanStr, 
                                                 String cleanOwnedStr) {
        boolean activated = false;
        boolean soundPlayed = false;
        
        // Check each notification, in order
        for (Notification notif : Config.get().getNotifs()) {
            if (!notif.isEnabled()) continue;
            
            // Trigger search
            for (Trigger trig : notif.triggers) {
                if (trig.string.isBlank()) continue;
                Matcher matcher = null;
                boolean hit = switch(trig.type) {
                    case NORMAL -> {
                        if (normalSearch(cleanOwnedStr, trig.string).find()) {
                            matcher = normalSearch(cleanStr, trig.string);
                            yield matcher.find();
                        }
                        yield false;
                    }
                    case REGEX -> {
                        if (trig.pattern == null) yield false;
                        matcher = trig.pattern.matcher(cleanStr);
                        yield matcher.find();
                    }
                    case KEY -> keySearch(msg, trig.string);
                };
                if (!hit) continue;

                // Exclusion search
                boolean exHit = false;
                for (Trigger exTrig : notif.exclusionTriggers) {
                    if (trig.string.isBlank()) continue;
                    exHit = switch(exTrig.type) {
                        case NORMAL -> normalSearch(cleanOwnedStr, exTrig.string).find();
                        case REGEX -> exTrig.pattern != null && exTrig.pattern.matcher(cleanStr).find();
                        case KEY -> keySearch(msg, exTrig.string);
                    };
                    if (exHit) break;
                }
                if (exHit) continue;
                
                // Activate notification
                
                if (notif.blockMessage) {
                    ChatNotify.LOG.info("Message '{}' blocked by trigger '{}'", 
                            msg.getString(), trig.string);
                    if (!activated) return null;
                }
                
                activated = true;
                if (!soundPlayed || Config.get().notifMode.equals(Config.NotifMode.MULTIPLE)) {
                    soundPlayed = playSound(notif);
                }
                showTitle(notif);
                sendResponses(notif, trig.type == Trigger.Type.REGEX ? matcher : null);
                
                try {
                    // Convert message into a format suitable for recursive processing
                    msg = FormatUtil.convertToStyledLiteral(msg.copy());
                    if (debug) {
                        ChatNotify.LOG.warn("Converted Message");
                        ChatNotify.LOG.warn(msg.toString());
                    }

                    // Restyle, using style string if possible
                    boolean restyled = false;
                    if (trig.styleString != null) {
                        Matcher m = styleSearch(cleanStr, trig.styleString);
                        if (m.find()) {
                            restyled = true;
                            do {
                                msg = restyleLeaves(msg, notif.textStyle, m.start(), m.end());
                            } while (Config.get().restyleMode.equals(
                                    Config.RestyleMode.ALL_INSTANCES) && m.find());
                        }
                    }
                    // If style string not usable, attempt to restyle trigger
                    if (!restyled) {
                        switch(trig.type) {
                            case NORMAL -> {
                                do {
                                    msg = restyleLeaves(msg, notif.textStyle,
                                            matcher.start() + matcher.group(1).length(),
                                            matcher.end() - matcher.group(2).length());
                                } while (Config.get().restyleMode.equals(
                                        Config.RestyleMode.ALL_INSTANCES) && matcher.find());
                            }
                            case REGEX -> {
                                do {
                                    msg = restyleLeaves(msg, notif.textStyle,
                                            matcher.start(), matcher.end());
                                } while (Config.get().restyleMode.equals(
                                        Config.RestyleMode.ALL_INSTANCES) && matcher.find());
                            }
                            case KEY -> msg = restyleRoot(msg, notif.textStyle);
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
                
                if (!Config.get().restyleMode.equals(Config.RestyleMode.ALL_TRIGGERS)) break;
            }
            if (activated && Config.get().notifMode.equals(Config.NotifMode.SINGLE)) return msg;
        }
        return msg;
    }

    /**
     * Checks whether the key matches the message;
     * @param msg the message to search.
     * @param key the key (or partial key) to search for.
     * @return {@code true} if the key matches the message, {@code false}
     * otherwise.
     */
    public static boolean keySearch(Component msg, String key) {
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
    public static Matcher normalSearch(String msg, String str) {
        /*
        U flag for full Unicode comparison, performance using randomly-generated
        100-character msg and 10-character str is approx 1.18 microseconds
        per check without flag, 1.31 microseconds with.
         */
        return Pattern.compile(
                "(?iU)(?<!\\w)(\\W?)" + Pattern.quote(str) + "(\\W?)(?!\\w)")
                .matcher(msg);
    }

    /**
     * Performs a case-insensitive search for the string within the message.
     * @param msg the message to search.
     * @param str the string to search for.
     * @return the {@link Matcher} for the search.
     */
    public static Matcher styleSearch(String msg, String str) {
        return Pattern.compile("(?iU)" + Pattern.quote(str)).matcher(msg);
    }

    /**
     * Plays the sound of the specified notification, if the relevant control is
     * enabled.
     * @param notif the Notification.
     */
    private static boolean playSound(Notification notif) {
        if (notif.sound.isEnabled() && notif.sound.getVolume() > 0) {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                    notif.sound.getResourceLocation(), Config.get().soundSource,
                    notif.sound.getVolume(), notif.sound.getPitch(),
                    SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true));
            return true;
        }
        return false;
    }

    /**
     * Displays the title text for the notification, if enabled.
     * @param notif the Notification.
     */
    private static void showTitle(Notification notif) {
        if (notif.titleText.canDisplay()) {
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
     * Overwrites the existing root style of the message with the specified
     * style.
     * @param msg the message to restyle.
     * @param style the {@link TextStyle} to apply.
     * @return the restyled message.
     */
    public static Component restyleRoot(Component msg, TextStyle style) {
        return style.isEnabled() ? msg.copy().setStyle(applyStyle(msg.getStyle(), style)) : msg;
    }

    /**
     * Uses a recursive traversal algorithm to apply the specified style to 
     * only the specified part of the message.
     * @param msg the message to restyle.
     * @param style the {@link TextStyle} to apply.
     * @param start the starting index of the string to restyle.
     * @param end the index after the end of the string to restyle.
     * @return the restyled message.
     */
    public static Component restyleLeaves(Component msg, TextStyle style, int start, int end) {
        return style.isEnabled() ? restyle(msg.copy(), style, start, end, 0) : msg;
    }

    /**
     * Recursive traversal restyling algorithm.
     * 
     * <p><b>Note:</b> Unable to process format codes or translatable 
     * components, use {@link FormatUtil#convertToStyledLiteral} prior to 
     * invoking this method.</p>
     * 
     * @param msg the message to restyle.
     * @param style the style to apply.
     * @param start the root string index of the first character in the target substring.
     * @param end the root string index of the last character in the target substring, plus one.
     * @param index the index of the start of {@code msg} in the root string.
     * @return the message, restyled if applicable.
     */
    private static MutableComponent restyle(MutableComponent msg, TextStyle style, int start, int end, int index) {
        if (debug) ChatNotify.LOG.warn("restyle('{}', {}, {}, {})", 
                msg.getString(), start, end, index);
        
        // Detach siblings
        List<Component> oldSiblings = new ArrayList<>(msg.getSiblings());
        msg.getSiblings().clear();
        
        // Restyle contents
        if (msg.getContents() instanceof PlainTextContents contents) {
            if (debug) ChatNotify.LOG.warn("PlainTextContents");
            String str = contents.text();
            if (index + str.length() >= start && index < end) {
                // Target string overlaps with current substring, so restyle
                // by splitting into 3 components; before, target, and after
                Style oldStyle = msg.getStyle();
                msg = Component.empty().withStyle(oldStyle);
                
                int localStart = Math.max(0, start - index);
                int localEnd = Math.min(str.length(), end - index);

                String part1 = str.substring(0, localStart);
                if (!part1.isEmpty()) msg.append(Component.literal(part1));
                
                String part2 = str.substring(localStart, localEnd);
                if (!part2.isEmpty()) msg.append(
                        Component.literal(part2).withStyle(style.getStyle()));
                
                String part3 = str.substring(localEnd);
                if (!part3.isEmpty()) msg.append(Component.literal(part3));
            }
            index += str.length();
        }

        // Recurse for original siblings and re-attach
        List<Component> siblings = msg.getSiblings();
        for (Component sibling : oldSiblings) {
            String str = sibling.getString();
            if (index + str.length() >= start && index < end) {
                siblings.add(restyle(sibling.copy(), style, start, end, index));
            } else {
                siblings.add(sibling);
            }
            index += str.length();
        }
        
        return msg;
    }

    /**
     * For each enabled field of the specified {@link TextStyle}, overrides the
     * corresponding {@link Style} field.
     * @param style the {@link Style} to apply to.
     * @param textStyle the {@link TextStyle} to apply.
     * @return the {@link Style}, with the {@link TextStyle} applied.
     */
    private static Style applyStyle(Style style, TextStyle textStyle) {
        if (!textStyle.bold.equals(TextStyle.FormatMode.DISABLED)) 
            style = style.withBold(textStyle.bold.equals(TextStyle.FormatMode.ON));
        if (!textStyle.italic.equals(TextStyle.FormatMode.DISABLED)) 
            style = style.withItalic(textStyle.italic.equals(TextStyle.FormatMode.ON));
        if (!textStyle.underlined.equals(TextStyle.FormatMode.DISABLED)) 
            style = style.withUnderlined(textStyle.underlined.equals(TextStyle.FormatMode.ON));
        if (!textStyle.strikethrough.equals(TextStyle.FormatMode.DISABLED)) 
            style = style.withStrikethrough(textStyle.strikethrough.equals(TextStyle.FormatMode.ON));
        if (!textStyle.obfuscated.equals(TextStyle.FormatMode.DISABLED)) 
            style = style.withObfuscated(textStyle.obfuscated.equals(TextStyle.FormatMode.ON));
        if (textStyle.doColor) style = style.withColor(textStyle.getTextColor());
        return style;
    }
}
