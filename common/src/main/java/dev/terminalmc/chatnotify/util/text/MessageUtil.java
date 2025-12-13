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

package dev.terminalmc.chatnotify.util.text;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.compat.chatheads.ChatHeadsWrapper;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.Response;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.toast.NotificationToast;
import dev.terminalmc.chatnotify.mixin.accessor.GuiAccessor;
import dev.terminalmc.chatnotify.util.ResponseUtil;
import dev.terminalmc.chatnotify.util.TimingUtil;
import dev.terminalmc.chatnotify.util.Unicode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.ChatNotify.recentMessages;
import static dev.terminalmc.chatnotify.config.Config.SenderDetectionMode.COMBINED;

public class MessageUtil {

    private static boolean debug = false;
    private static boolean ownMsg = false;

    private MessageUtil() {
    }

    /**
     * Initiates the message processing algorithm.
     *
     * @param msg The original message.
     * @return A modified copy of the message, or the original if no modifying was required.
     */
    public static @Nullable Component processMessage(Component msg) {
        debug = Config.get().debugMode.equals(Config.DebugMode.ALL);
        ownMsg = false;

        String str = msg.getString();
        if (str.isBlank())
            return msg; // Ignore blank messages

        // Save message for trigger editor
        if (ChatNotify.unmodifiedChat.size() > 30)
            ChatNotify.unmodifiedChat.poll();
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
     * Determines whether a message was sent by the user and modifies it if necessary to prevent
     * unwanted notifications.
     * <p>
     * If the global option {@link Config#senderDetectionMode} is set to
     * {@link Config.SenderDetectionMode#COMBINED} and the ChatHeads mod is available, it will be
     * queried to determine the message owner.
     * <p>
     * Otherwise, the message will be compared to recently sent messages and checked for triggers of
     * the username notification to determine whether it was sent by the mod user.
     * <p>
     * If the message is positively identified, it is set to {@code null} if the global option
     * {@link Config#checkOwnMessages} is false, else the part of the prefix that matched a trigger
     * is removed to prevent it being detected by trigger search.
     *
     * @param cleanStr the clean (no format codes) string to check.
     * @return the string, a modified copy, or {@code null} depending on the result of the check.
     */
    private static String checkOwner(String cleanStr) {
        PlayerInfo ownerInfo = null;
        String cleanOwnedStr = cleanStr;
        if (Config.get().senderDetectionMode == COMBINED) {
            // Ask ChatHeads who the message owner is
            ownerInfo = ChatHeadsWrapper.getPlayerInfo();
        }
        if (ownerInfo != null) {
            // Use info from ChatHeads
            if (debug)
                ChatNotify.LOG.warn("Owner check using ChatHeads");
            if (Minecraft.getInstance().player != null) {
                UUID id = ownerInfo.getProfile().getId();
                if (id.equals(Minecraft.getInstance().player.getUUID())) {
                    if (debug)
                        ChatNotify.LOG.warn("Matched user's UUID");
                    for (Trigger t : Config.get().getUserNotif().triggers) {
                        Matcher matcher = normalSearch(cleanStr, t.string);
                        if (matcher.find()) {
                            if (debug)
                                ChatNotify.LOG.warn("Matched trigger '{}'", t.string);
                            // Modify message according to config
                            cleanOwnedStr = cleanStr.substring(0, matcher.start())
                                    + cleanStr.substring(matcher.end());
                            break;
                        }
                    }
                }
            }
        } else {
            // Default to sent-message-match heuristic
            if (debug)
                ChatNotify.LOG.warn("Owner check using heuristic");
            // Check for a matching stored message
            for (int i = 0; i < recentMessages.size(); i++) {
                // Find last occurrence of recent message
                // Case-insensitive to allow for servers with all-caps prevention
                Matcher recentMatcher =
                        Pattern.compile("(?iU)" + Pattern.quote(recentMessages.get(i).getSecond()))
                                .matcher(cleanStr);
                int recentStart = -1;
                while (recentMatcher.find()) {
                    recentStart = recentMatcher.start();
                }
                if (recentStart != -1) {
                    if (debug)
                        ChatNotify.LOG.warn(
                                "Matched recent message '{}' at index {}",
                                recentMessages.get(i).getSecond(),
                                recentStart
                        );
                    // Matched against a stored message, check for a username trigger
                    String prefix = cleanStr.substring(0, recentStart);
                    for (Trigger t : Config.get().getUserNotif().triggers) {
                        Matcher triggerMatcher = normalSearch(prefix, t.string);
                        if (triggerMatcher.find()) {
                            if (debug)
                                ChatNotify.LOG.warn(
                                        "Matched trigger '{}' at index {}",
                                        t.string,
                                        triggerMatcher.start()
                                );
                            recentMessages.remove(i); // Remove stored message
                            // Modify message according to config
                            cleanOwnedStr = cleanStr.substring(
                                    0,
                                    triggerMatcher.start() + triggerMatcher.group(1).length()
                            ) + cleanStr.substring(
                                    triggerMatcher.end() - triggerMatcher.group(2).length());
                            break;
                        }
                    }
                }
            }
        }
        if (debug)
            ChatNotify.LOG.warn("Owner-checked string: '{}'", cleanOwnedStr);
        return cleanOwnedStr;
    }

    /**
     * For each trigger of each enabled notification, checks whether the trigger matches the
     * message.
     * <p>
     * When a trigger matches, checks the exclusion triggers of the notification to determine
     * whether to trigger the notification.
     * <p>
     * If the notification should be triggered, completes the relevant notification actions.
     * <p>
     * Note: For performance and simplicity reasons, this method only allows one notification to be
     * triggered by a given message.
     *
     * @param msg           the message.
     * @param cleanStr      the message string, with all format codes removed.
     * @param cleanOwnedStr cleanStr, with the sender removed if applicable.
     * @return a re-styled copy of the message, or the original message if restyling was not
     * possible.
     */
    private static @Nullable Component tryNotify(
            Component msg,
            String cleanStr,
            String cleanOwnedStr
    ) {
        boolean restyleAll = Config.get().restyleMode.equals(Config.RestyleMode.ALL_INSTANCES);
        boolean anyTriggered = false;
        boolean anySoundPlayed = false;

        // Check each notification, in order
        for (Notification notif : Config.get().getNotifs()) {
            if (!notif.canBeTriggered(ownMsg))
                continue;

            // Trigger search
            for (Trigger trig : notif.triggers) {
                if (trig.string.isBlank())
                    continue;
                Matcher matcher = null;
                boolean hit = switch (trig.type) {
                    case NORMAL -> {
                        if (normalSearch(cleanOwnedStr, trig.string).find()) {
                            matcher = normalSearch(cleanStr, trig.string);
                            yield matcher.find();
                        }
                        yield false;
                    }
                    case REGEX -> {
                        if (trig.pattern == null)
                            yield false;
                        matcher = trig.pattern.matcher(cleanStr);
                        yield matcher.find();
                    }
                    case KEY -> keySearch(msg, trig.string);
                };
                if (!hit)
                    continue;

                // Inclusion search
                boolean inMiss = false;
                if (notif.inclusionEnabled) {
                    for (Trigger inTrig : notif.inclusionTriggers) {
                        if (trig.string.isBlank())
                            continue;
                        inMiss = (!switch (inTrig.type) {
                            case NORMAL -> normalSearch(cleanOwnedStr, inTrig.string).find();
                            case REGEX -> inTrig.pattern == null
                                    || inTrig.pattern.matcher(cleanStr).find();
                            case KEY -> keySearch(msg, inTrig.string);
                        });
                        if (inMiss)
                            break;
                    }
                }
                if (inMiss)
                    continue;

                // Exclusion search
                boolean exHit = false;
                if (notif.exclusionEnabled) {
                    for (Trigger exTrig : notif.exclusionTriggers) {
                        if (trig.string.isBlank())
                            continue;
                        exHit = switch (exTrig.type) {
                            case NORMAL -> normalSearch(cleanOwnedStr, exTrig.string).find();
                            case REGEX -> exTrig.pattern != null
                                    && exTrig.pattern.matcher(cleanStr).find();
                            case KEY -> keySearch(msg, exTrig.string);
                        };
                        if (exHit)
                            break;
                    }
                }
                if (exHit)
                    continue;

                // Trigger notification
                anyTriggered = true;

                // Start countdown
                notif.countdown = notif.cooldown;

                // Play sound
                if (!anySoundPlayed || Config.get().notifMode.equals(Config.NotifMode.ALL)) {
                    anySoundPlayed = playSound(notif);
                }

                // Send response messages
                Matcher subsMatcher = trig.type == Trigger.Type.REGEX ? matcher : null;
                sendResponses(notif, subsMatcher);

                // Restyle
                msg = StyleUtil.restyle(msg, cleanStr, trig, matcher, notif.textStyle, restyleAll);

                // Send custom messages, after restyle in case of forwarding
                // the entire message. Reset match by subsMatcher.find(0)

                // If there is no delay don't bother putting them in a queue
                if (notif.delay == 0) {
                    showStatusBarMsg(notif, msg, subsMatcher);
                    showTitleMsg(notif, msg, subsMatcher);
                    showToastMsg(notif, msg, subsMatcher);
                } else {
                    final Component fMsg = msg;

                    if (notif.statusBarMsgEnabled)
                        TimingUtil.send(
                                () -> showStatusBarMsg(notif, fMsg, subsMatcher),
                                notif.delay
                        );

                    if (notif.titleMsgEnabled)
                        TimingUtil.send(() -> showTitleMsg(notif, fMsg, subsMatcher), notif.delay);

                    if (notif.toastMsgEnabled)
                        TimingUtil.send(() -> showToastMsg(notif, fMsg, subsMatcher), notif.delay);
                }

                typeTypedMsg(notif, msg, subsMatcher);
                copyClipboardMsg(notif, msg, subsMatcher);

                // If replacement enabled, process
                if (notif.replacementMsgEnabled) {
                    msg = convertMsg(notif.replacementMsg, subsMatcher, msg);
                    String str = msg.getString();
                    cleanStr = FormatUtil.stripCodes(str);
                    cleanOwnedStr = cleanStr;

                    // No other notifications can be triggered on a blank message
                    if (str.isBlank())
                        return null;
                }

                break;
            }
            // If only activating single, return early
            if (anyTriggered && Config.get().notifMode.equals(Config.NotifMode.SINGLE))
                return msg;
        }
        return msg;
    }

    /**
     * Checks whether the key matches the message;
     *
     * @param msg the message to search.
     * @param key the key (or partial key) to search for.
     * @return {@code true} if the key matches the message, {@code false} otherwise.
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
     * Performs a case-insensitive word-boundary search for the string within the message.
     *
     * @param msg the message to search.
     * @param str the string to search for.
     * @return the {@link Matcher} for the search.
     */
    public static Matcher normalSearch(String msg, String str) {
        /*
        U flag for full Unicode comparison, performance using randomly-generated
        100-character msg and 10-character str is approx 1.18 microseconds
        per check without flag, 1.31 microseconds with.

        The word-boundary regex \b is a zero-width assertion that matches if
        there is \w on one side, and either there is \W on the other or the
        position is beginning or end of string. Thus, it cannot be used here as
        it will fail to match for a trigger starting or ending in \W.
         */
        return Pattern.compile("(?iU)(?<!\\w)(\\W?)" + Pattern.quote(str) + "(\\W?)(?!\\w)")
                .matcher(msg);
    }

    /**
     * Plays the sound of the specified {@link Notification}, if enabled.
     *
     * @param notif the {@link Notification}.
     */
    private static boolean playSound(Notification notif) {
        if (notif.sound.isEnabled() && notif.sound.getVolume() > 0) {
            ResourceLocation location = notif.sound.getResourceLocation();
            if (location != null) {
                Runnable action = () -> Minecraft.getInstance().getSoundManager().play(
                        new SimpleSoundInstance(
                                notif.sound.getResourceLocation(),
                                Config.get().soundSource,
                                notif.sound.getVolume(),
                                notif.sound.getPitch(),
                                SoundInstance.createUnseededRandom(),
                                false,
                                0,
                                SoundInstance.Attenuation.NONE,
                                0,
                                0,
                                0,
                                true
                        )
                );

                if (notif.delay == 0 || !notif.soundSync) {
                    action.run();
                } else {
                    TimingUtil.send(action, notif.delay);
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Converts a custom message string into a {@link Component} for sending.
     *
     * @param msgString the custom message string.
     * @param matcher   a regex matcher for capturing group substitution.
     * @param msg       the original message.
     * @return the message, converted and with all substitutions done.
     */
    private static Component convertMsg(
            String msgString,
            @Nullable Matcher matcher,
            Component msg
    ) {
        // Replace $ with section sign
        msgString = msgString.replaceAll(Pattern.quote("$"), Unicode.SECTION.str);

        // Substitute capturing groups
        if (matcher != null && matcher.find(0)) {
            // Capturing groups preceded by a $ (now §) sign should not retain
            // their original style, so we substitute them in first
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String replacement = matcher.group(i) == null ? "" : matcher.group(i);
                msgString = msgString.replaceAll(
                        Pattern.quote(Unicode.SECTION.str + "(" + i + ")"),
                        replacement
                );
            }

            // Convert message into a format suitable for recursive processing
            msg = FormatUtil.convertToStyledLiteral(msg.copy());

            // Record indices where groups should be placed, and get the
            // replacement substring for each group.
            ArrayList<int[]> groupReplacementIndices = new ArrayList<>();
            HashMap<Integer, Component> groupReplacementMap = new HashMap<>();
            for (int groupNum = 0; groupNum <= matcher.groupCount(); groupNum++) {
                String targetString = "(" + groupNum + ")";
                if (!msgString.contains(targetString))
                    continue;

                // Work through the message, collecting indices to replace with
                // the captured group
                int index = msgString.indexOf(targetString);
                while (index >= 0) {
                    groupReplacementIndices.add(new int[]{index, groupNum});
                    index = msgString.indexOf(targetString, index + targetString.length());
                }

                Component replacement;
                if (matcher.group(groupNum) == null) {
                    replacement = Component.empty();
                } else {
                    int start = matcher.start(groupNum);
                    int end = matcher.end(groupNum);
                    replacement = StyleUtil.styledSubstring(msg, start, end);
                }
                groupReplacementMap.put(groupNum, replacement);
            }

            // Sort replacements by order that they appear in the custom message
            groupReplacementIndices.sort(Comparator.comparingInt(obj -> obj[0]));

            // Build the new message, placing in the group components
            if (!groupReplacementIndices.isEmpty()) {
                MutableComponent newMsg = Component.empty();
                int startIndex = 0;
                for (int[] replacement : groupReplacementIndices) {
                    newMsg.append(Component.literal(msgString.substring(
                            startIndex,
                            replacement[0]
                    )));
                    newMsg.append(groupReplacementMap.get(replacement[1]));
                    startIndex = replacement[0] + ("(" + replacement[1] + ")").length();
                }
                newMsg.append(Component.literal(msgString.substring(startIndex)));
                return newMsg;
            }
        }
        return Component.literal(msgString);
    }

    /**
     * Displays the status bar message for the {@link Notification}, if enabled.
     *
     * @param notif   the {@link Notification}.
     * @param msg     the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger was used, {@code null}
     *                otherwise.
     */
    private static void showStatusBarMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.statusBarMsgEnabled) {
            Component displayMsg = notif.statusBarMsg.isBlank()
                    ? msg
                    : convertMsg(notif.statusBarMsg, matcher, msg);
            Gui gui = Minecraft.getInstance().gui;
            gui.setOverlayMessage(displayMsg, false);
            ((GuiAccessor) gui).chatnotify$setOverlayMessageTime(notif.statusBarStay);
        }
    }

    /**
     * Displays the title message for the {@link Notification}, if enabled.
     *
     * @param notif   the {@link Notification}.
     * @param msg     the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger was used, {@code null}
     *                otherwise.
     */
    private static void showTitleMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.titleMsgEnabled) {
            Component displayMsg = notif.titleMsg.isBlank()
                    ? msg
                    : convertMsg(notif.titleMsg, matcher, msg);

            Component subDisplayMsg = notif.subtitleMsg.isBlank()
                    ? msg
                    : convertMsg(notif.subtitleMsg, matcher, msg);

            Minecraft.getInstance().gui.setTimes(
                    notif.titleFadeIn,
                    notif.titleStay,
                    notif.titleFadeOut
            );
            Minecraft.getInstance().gui.setTitle(displayMsg);

            if (notif.subtitleMsgEnabled)
                Minecraft.getInstance().gui.setSubtitle(subDisplayMsg);
        }
    }

    /**
     * Displays the toast message for the {@link Notification}, if enabled.
     *
     * @param notif   the {@link Notification}.
     * @param msg     the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger was used, {@code null}
     *                otherwise.
     */
    private static void showToastMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.toastMsgEnabled) {
            Component displayMsg = notif.toastMsg.isBlank()
                    ? msg
                    : convertMsg(notif.toastMsg, matcher, msg);
            // Convert from ticks to milliseconds
            Minecraft.getInstance()
                    .getToasts()
                    .addToast(new NotificationToast(displayMsg, notif.toastStay * 50));
        }
    }

    /**
     * Types the typed message for the {@link Notification}, if enabled.
     *
     * @param notif   the {@link Notification}.
     * @param msg     the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger was used, {@code null}
     *                otherwise.
     */
    private static void typeTypedMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.typedMsgEnabled && Minecraft.getInstance().screen == null) {
            Component displayMsg =
                    notif.typedMsg.isBlank() ? msg : convertMsg(notif.typedMsg, matcher, msg);
            Minecraft.getInstance().setScreen(new ChatScreen(displayMsg.getString()));
        }
    }

    /**
     * Copies the clipboard message for the {@link Notification}, if enabled.
     *
     * @param notif   the {@link Notification}.
     * @param msg     the original message.
     * @param matcher the {@link Matcher} for the trigger, if a regex trigger was used, {@code null}
     *                otherwise.
     */
    private static void copyClipboardMsg(Notification notif, Component msg, Matcher matcher) {
        if (notif.clipboardMsgEnabled) {
            Component displayMsg = notif.clipboardMsg.isBlank()
                    ? msg
                    : convertMsg(notif.clipboardMsg, matcher, msg);
            Minecraft.getInstance().keyboardHandler.setClipboard(displayMsg.getString());
        }
    }

    /**
     * Sends all response messages of the specified notification, if the relevant control is
     * enabled.
     *
     * @param notif the Notification.
     */
    private static void sendResponses(Notification notif, @Nullable Matcher matcher) {
        if (notif.responseEnabled) {
            int totalDelay = 0;
            for (Response msg : notif.responses) {
                msg.sendingString = msg.string;
                if (msg.type.equals(Response.Type.REGEX) && matcher != null && matcher.find(0)) {
                    // Capturing group substitution
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                        String replacement = matcher.group(i) == null ? "" : matcher.group(i);
                        msg.sendingString =
                                msg.sendingString.replaceAll("\\(" + i + "\\)", replacement);
                    }
                }
                totalDelay += msg.delayTicks;
                ResponseUtil.send(msg, totalDelay);
            }
        }
    }
}
