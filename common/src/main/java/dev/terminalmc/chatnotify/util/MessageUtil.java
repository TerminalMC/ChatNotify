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

import com.mojang.datafixers.util.Pair;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.compat.chatheads.ChatHeadsWrapper;
import dev.terminalmc.chatnotify.config.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.ChatNotify.recentMessages;
import static dev.terminalmc.chatnotify.config.Config.SenderDetectionMode.COMBINED;

public class MessageUtil {
    private static boolean debug = false;
    private static boolean ownMsg = false;
    
    /**
     * Initiates the message processing algorithm.
     * @param msg The original message.
     * @return A modified copy of the message, or the original if no modifying
     * was required.
     */
    public static @Nullable Component processMessage(Component msg) {
        debug = Config.get().debugMode.equals(Config.DebugMode.ALL);
        ownMsg = false;
        
        String str = msg.getString();
        if (str.isBlank()) return msg; // Ignore blank messages
        
        // Save message for trigger editor
        if (ChatNotify.unmodifiedChat.size() > 30) ChatNotify.unmodifiedChat.poll();
        ChatNotify.unmodifiedChat.add(msg);
        
        if (debug) {
            ChatNotify.LOG.warn("Processing new message");
            ChatNotify.LOG.warn("Original text:");
            ChatNotify.LOG.warn(msg.getString());
            ChatNotify.LOG.warn("Original tree:");
            ChatNotify.LOG.warn(msg.toString());
        }

        // Remove format codes from string before searching
        String cleanStr = FormatUtil.stripCodes(str);
        
        // Check owner
        String cleanOwnedStr = checkOwner(cleanStr);
        ownMsg = !cleanOwnedStr.equals(cleanStr);
        
        // Process notifications
        msg = tryNotify(msg.copy(), cleanStr, cleanOwnedStr);

        if (debug) {
            ChatNotify.LOG.warn("Finished processing message");
            if (msg == null) {
                ChatNotify.LOG.warn("null");
            } else {
                ChatNotify.LOG.warn("Final text:");
                ChatNotify.LOG.warn(msg.getString());
                ChatNotify.LOG.warn("Final tree:");
                ChatNotify.LOG.warn(msg.toString());
            }
        }
        
        return msg;
    }

    /**
     * Determines whether a message was sent by the user and modifies it if
     * necessary to prevent unwanted notifications.
     *
     * <p>If the global option {@link Config#senderDetectionMode} is set to 
     * {@link Config.SenderDetectionMode#COMBINED} and the ChatHeads mod is
     * available, it will be queried to determine the message owner.</p>
     * 
     * <p>Otherwise, the message will be compared to recently sent messages 
     * and checked for triggers of the username notification to determine
     * whether it was sent by the mod user.</p>
     *
     * <p>If the message is positively identified, it is set to {@code null} if
     * the global option {@link Config#checkOwnMessages} is false, else the part
     * of the prefix that matched a trigger is removed to prevent it being 
     * detected by trigger search.</p>
     * @param cleanStr the clean (no format codes) string to check.
     * @return the string, a modified copy, or {@code null} depending on the
     * result of the check.
     */
    private static String checkOwner(String cleanStr) {
        boolean checkSuccessful = false;
        String cleanOwnedStr = cleanStr;
        if (Config.get().senderDetectionMode == COMBINED) {
            // Ask chat heads who the message owner is
            Pair<PlayerInfo,Integer> info = ChatHeadsWrapper.getPlayerInfo();
            if (info != null) { // null indicates ChatHeads failure
                checkSuccessful = true;
                if (debug) ChatNotify.LOG.warn("Owner check using ChatHeads");
                if (info.getFirst() != null && Minecraft.getInstance().player != null) {
                    UUID id = info.getFirst().getProfile().getId();
                    if (id.equals(Minecraft.getInstance().player.getUUID())) {
                        if (debug) ChatNotify.LOG.warn("Matched user's UUID");
                        for (Trigger t : Config.get().getUserNotif().triggers) {
                            Matcher matcher = normalSearch(cleanStr, t.string);
                            if (matcher.find()) {
                                if (debug) ChatNotify.LOG.warn("Matched trigger '{}'", t.string);
                                // Modify message according to config
                                cleanOwnedStr = cleanStr.substring(0, matcher.start()) 
                                        + cleanStr.substring(matcher.end());
                                break;
                            }
                        }
                    }
                }
            }
        }
        // Default to sent-message-match heuristic
        if (!checkSuccessful) {
            if (debug) ChatNotify.LOG.warn("Owner check using heuristic");
            // Check for a matching stored message
            for (int i = 0; i < recentMessages.size(); i++) {
                Matcher matcher = Pattern.compile("(?iU)" + 
                                Pattern.quote(recentMessages.get(i).getSecond())).matcher(cleanStr);
                if (matcher.find()) {
                    if (debug) ChatNotify.LOG.warn("Matched a recent message");
                    // Matched against a stored message, check for a username trigger
                    String prefix = cleanStr.substring(0, matcher.start());
                    for (Trigger t : Config.get().getUserNotif().triggers) {
                        matcher = normalSearch(prefix, t.string);
                        if (matcher.find()) {
                            if (debug) ChatNotify.LOG.warn("Matched trigger '{}'", t.string);
                            recentMessages.remove(i); // Remove stored message
                            // Modify message according to config
                            cleanOwnedStr = cleanStr.substring(0, matcher.start()) 
                                    + cleanStr.substring(matcher.end());
                            break;
                        }
                    }
                }
            }
        }
        if (debug) ChatNotify.LOG.warn("Owner-checked string: '{}'", cleanOwnedStr);
        return cleanOwnedStr;
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
        boolean restyleAllInstances = Config.get().restyleMode.equals(Config.RestyleMode.ALL_INSTANCES);
        boolean anyActivated = false;
        boolean anySoundPlayed = false;
        
        // Check each notification, in order
        for (Notification notif : Config.get().getNotifs()) {
            if (!notif.canActivate(ownMsg)) continue;
            
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
                if (notif.exclusionEnabled) {
                    for (Trigger exTrig : notif.exclusionTriggers) {
                        if (trig.string.isBlank()) continue;
                        exHit = switch(exTrig.type) {
                            case NORMAL -> normalSearch(cleanOwnedStr, exTrig.string).find();
                            case REGEX -> exTrig.pattern != null && exTrig.pattern.matcher(cleanStr).find();
                            case KEY -> keySearch(msg, exTrig.string);
                        };
                        if (exHit) break;
                    }
                }
                if (exHit) continue;
                
                // Activate notification
                anyActivated = true;
                
                // Play sound
                if (!anySoundPlayed || Config.get().notifMode.equals(Config.NotifMode.ALL)) {
                    anySoundPlayed = playSound(notif);
                }
                
                // Send response messages
                Matcher subsMatcher = trig.type == Trigger.Type.REGEX ? matcher : null;
                sendResponses(notif, subsMatcher);
                
                // Restyle
                msg = restyle(msg, cleanStr, trig, matcher, notif.textStyle, restyleAllInstances);

                // Send custom messages
                showStatusBarMsg(notif, msg, subsMatcher);
                showTitleMsg(notif, msg, subsMatcher);

                // If replacement enabled, process
                if (notif.replacementMsgEnabled) {
                    msg = convertMsg(notif.replacementMsg, subsMatcher);
                    String str = msg.getString();
                    cleanStr = FormatUtil.stripCodes(str);
                    cleanOwnedStr = cleanStr;

                    // No other notifications can activate on a blank message
                    if (str.isBlank()) return null;
                }
                
                break;
            }
            // If only activating single, return early
            if (anyActivated && Config.get().notifMode.equals(Config.NotifMode.SINGLE)) return msg;
        }
        return msg;
    }
    
    public static Component restyle(Component msg, String cleanStr, Trigger trig, Matcher matcher, 
                                     TextStyle textStyle, boolean restyleAllInstances) {
        try {
            // Convert message into a format suitable for recursive processing
            msg = FormatUtil.convertToStyledLiteral(msg.copy());
            if (debug) {
                ChatNotify.LOG.warn("Converting message prior to initiating restyle");
                ChatNotify.LOG.warn("Converted text:");
                ChatNotify.LOG.warn(msg.getString());
                ChatNotify.LOG.warn("Converted tree:");
                ChatNotify.LOG.warn(msg.toString());
            }

            // Restyle, using style string if possible
            boolean restyled = false;
            if (trig.styleTarget.enabled && !trig.styleTarget.string.isBlank()) {
                switch(trig.styleTarget.type) {
                    case NORMAL -> {
                        Matcher m = styleSearch(cleanStr, trig.styleTarget.string);
                        if (m.find()) {
                            restyled = true;
                            do {
                                msg = restyleLeaves(msg, textStyle, m.start(), m.end());
                            } while (restyleAllInstances && m.find());
                        }
                    }
                    case REGEX -> {
                        if (trig.styleTarget.pattern != null) {
                            Matcher m = trig.styleTarget.pattern.matcher(cleanStr);
                            if (m.find()) {
                                restyled = true;
                                do {
                                    msg = restyleLeaves(msg, textStyle, m.start(), m.end());
                                } while (restyleAllInstances && m.find());
                            }
                        }
                    }
                }
            }
            // If style string not usable, attempt to restyle trigger
            if (!restyled) {
                if (debug) {
                    ChatNotify.LOG.warn("Style target '{}' (type {})", 
                            trig.styleTarget.string, trig.styleTarget.type);
                    ChatNotify.LOG.warn("Defaulting to trigger restyle");
                }
                switch(trig.type) {
                    case NORMAL -> {
                        do {
                            msg = restyleLeaves(msg, textStyle,
                                    matcher.start() + matcher.group(1).length(),
                                    matcher.end() - matcher.group(2).length());
                        } while (restyleAllInstances && matcher.find());
                    }
                    case REGEX -> {
                        do {
                            msg = restyleLeaves(msg, textStyle,
                                    matcher.start(), matcher.end());
                        } while (restyleAllInstances && matcher.find());
                    }
                    case KEY -> msg = restyleRoot(msg, textStyle);
                }
            }
        } catch (IllegalArgumentException ignored) {}
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
     * Plays the sound of the specified {@link Notification}, if enabled.
     * @param notif the {@link Notification}.
     */
    private static boolean playSound(Notification notif) {
        if (notif.sound.isEnabled() && notif.sound.getVolume() > 0) {
            ResourceLocation location = notif.sound.getResourceLocation();
            if (location != null) {
                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                        notif.sound.getResourceLocation(), Config.get().soundSource,
                        notif.sound.getVolume(), notif.sound.getPitch(),
                        SoundInstance.createUnseededRandom(), false, 0,
                        SoundInstance.Attenuation.NONE, 0, 0, 0, true));
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a custom message string into a {@link Component} for sending.
     * @param msg the custom message string.
     * @param matcher a regex matcher for capturing group substitution.
     * @return the message, converted and with all substitutions done.
     */
    private static Component convertMsg(String msg, @Nullable Matcher matcher) {
        // Replace $ with section sign
        msg = msg.replaceAll(Matcher.quoteReplacement("$"), "\u00A7");
        // Substitute capturing groups
        if (matcher != null) {
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String replacement = matcher.group(i) == null ? "" : matcher.group(i);
                msg = msg.replace("(" + i + ")", replacement);
            }
        }
        return Component.literal(msg);
    }

    /**
     * Displays the status bar message for the {@link Notification}, if enabled.
     * @param notif the {@link Notification}.
     * @param msg the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger
     *                was used, {@code null} otherwise.
     */
    private static void showStatusBarMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.statusBarMsgEnabled) {
            Component displayMsg = notif.statusBarMsg.isBlank()
                    ? msg
                    : convertMsg(notif.statusBarMsg, matcher);
            Minecraft.getInstance().gui.setOverlayMessage(displayMsg, false);
        }
    }

    /**
     * Displays the title message for the {@link Notification}, if enabled.
     * @param notif the {@link Notification}.
     * @param msg the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger
     *                was used, {@code null} otherwise.
     */
    private static void showTitleMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.titleMsgEnabled) {
            Component displayMsg = notif.titleMsg.isBlank()
                    ? msg
                    : convertMsg(notif.titleMsg, matcher);
            Minecraft.getInstance().gui.setTitle(displayMsg);
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
                        String replacement = matcher.group(i) == null ? "" : matcher.group(i);
                        msg.sendingString = msg.sendingString.replace("(" + i + ")", replacement);
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
        return style.isEnabled() ? recursiveRestyle(msg.copy(), style, start, end, 0) : msg;
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
    private static MutableComponent recursiveRestyle(MutableComponent msg, TextStyle style, 
                                                     int start, int end, int index) {
        if (debug) ChatNotify.LOG.warn("recursiveRestyle('{}', {}, {}, {})", 
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
                siblings.add(recursiveRestyle(sibling.copy(), style, start, end, index));
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
