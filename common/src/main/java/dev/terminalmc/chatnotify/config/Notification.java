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

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.config.util.JsonUtil;
import dev.terminalmc.chatnotify.util.functional.StringSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A {@link Notification} is triggered by a chat message and, when triggered, can in turn trigger a
 * range of other events. Consists of:
 * <p>
 * - A range of controls (boolean or enum) defining behavior.
 * <p>
 * - A {@link Sound} instance, defining the sound to be played on triggering, with volume and pitch
 * controls.
 * <p>
 * - A {@link TextStyle} instance, defining how the triggering message should be restyled.
 * <p>
 * - A series of custom message strings, to be optionally displayed to the user in various different
 * ways on triggering.
 * <p>
 * - A list of {@link Trigger} instances, to be checked against incoming messages to determine
 * whether the {@link Notification} should be triggered.
 * <p>
 * - A list of inclusion {@link Trigger} instances which, if not matched, prevent triggering.
 * <p>
 * - A list of exclusion {@link Trigger} instances which, if matched, prevent triggering.</p>
 * <p>
 * - A list of {@link Response} instances, to be sent on triggering.
 */
public class Notification implements StringSupplier {

    public static final int VERSION = 8;
    public final int version = VERSION;

    /**
     * A status flag to indicate that this instance is being edited, and should not be triggered
     * irrespective of {@link Notification#enabled}.
     */
    public transient boolean editing = false;

    // Options

    /**
     * Whether this instance is set by the user to be eligible for triggering.
     * <p>
     * Necessary but not sufficient condition; never trigger if this is {@code false}, but it being
     * {@code true} does not mean that this instance can be safely triggered. Instead, check
     * {@link Notification#canBeTriggered}.
     */
    public boolean enabled;
    public static final boolean enabledDefault = true;

    /**
     * Controls whether this instance is eligible for triggering on user-sent messages.
     * <p>
     * {@link CheckOwnMode#DEFER} means that {@link Config#checkOwnMessages} should be used
     * instead.
     */
    public CheckOwnMode checkOwnMode;

    public enum CheckOwnMode {
        DEFER,
        ON,
        OFF,
    }

    /**
     * Whether this instance allows use of inclusion triggers.
     */
    public boolean inclusionEnabled;
    public static final boolean inclusionEnabledDefault = true;

    /**
     * Whether this instance allows use of exclusion triggers.
     */
    public boolean exclusionEnabled;
    public static final boolean exclusionEnabledDefault = true;

    /**
     * Whether this instance allows use of response messages.
     */
    public boolean responseEnabled;
    public static final boolean responseEnabledDefault = true;

    /**
     * The {@link Sound} to play on triggering.
     */
    public final Sound sound;
    public static final Supplier<Sound> soundDefault = Sound::new;

    /**
     * The {@link TextStyle} to use for restyling messages on triggering.
     */
    public final TextStyle textStyle;
    public static final Supplier<TextStyle> textStyleDefault = TextStyle::new;

    /**
     * Optional message to replace the triggering chat message.
     */
    public String replacementMsg;
    public static final String replacementMsgDefault = "";
    public boolean replacementMsgEnabled;
    public static final boolean replacementMsgEnabledDefault = false;

    /**
     * Optional message to display in the status bar (above the hotbar).
     */
    public String statusBarMsg;
    public static final String statusBarMsgDefault = "";
    public boolean statusBarMsgEnabled;
    public static final boolean statusBarMsgEnabledDefault = false;

    /**
     * Optional message to display in title text (large-font, center-screen).
     */
    public String titleMsg;
    public static final String titleMsgDefault = "";
    public boolean titleMsgEnabled;
    public static final boolean titleMsgEnabledDefault = false;

    /**
     * Optional message to display in subtitle text.
     */
    public String subtitleMsg;
    public static final String subtitleMsgDefault = "";
    public boolean subtitleMsgEnabled;
    public static final boolean subtitleMsgEnabledDefault = false;

    /**
     * Optional message to display in a toast popup.
     */
    public String toastMsg;
    public static final String toastMsgDefault = "";
    public boolean toastMsgEnabled;
    public static final boolean toastMsgEnabledDefault = false;

    /**
     * Typed chat message.
     */
    public String typedMsg;
    public static final String typedMsgDefault = "";
    public boolean typedMsgEnabled;
    public static final boolean typedMsgEnabledDefault = false;

    /**
     * Clipboard copy message.
     */
    public String clipboardMsg;
    public static final String clipboardMsgDefault = "";
    public boolean clipboardMsgEnabled;
    public static final boolean clipboardMsgEnabledDefault = false;

    /**
     * Optional message timing (status bar, title text or toast popup).
     */
    public boolean soundSync;
    public static final boolean soundSyncDefault = true;
    public int delay;
    public static final int timingDelayDefault = 0;
    public int titleFadeIn;
    public static final int titleFadeInDefault = 10;
    public int titleStay;
    public static final int titleStayDefault = 70;
    public int titleFadeOut;
    public static final int titleFadeOutDefault = 20;
    public int statusBarStay;
    public static final int statusBarStayDefault = 60;
    public int toastStay;
    public static final int toastStayDefault = 100;

    /**
     * A list of {@link Trigger} instances, of which any one can trigger this instance.
     */
    public final List<Trigger> triggers;
    public static final Supplier<List<Trigger>> triggersDefault = ArrayList::new;

    /**
     * A list of {@link Trigger} instances, of which all must match for this instance to be
     * triggered.
     * <p>
     * Note: For simplicity, this list uses the same {@link Trigger} class as
     * {@link Notification#triggers}. However, instances in this list will never use the
     * {@link Trigger#styleTarget} capability.
     */
    public final List<Trigger> inclusionTriggers;
    public static final Supplier<List<Trigger>> inclusionTriggersDefault = ArrayList::new;

    /**
     * The list of {@link Trigger}s which prevent triggering of this instance.
     * <p>
     * Note: For simplicity, this list uses the same {@link Trigger} class as
     * {@link Notification#triggers}. However, instances in this list will never use the
     * {@link Trigger#styleTarget} capability.
     */
    public final List<Trigger> exclusionTriggers;
    public static final Supplier<List<Trigger>> exclusionTriggersDefault = ArrayList::new;

    /**
     * The list of {@link Response}s to be sent on triggering.
     */
    public final List<Response> responses;
    public static final Supplier<List<Response>> responsesDefault = ArrayList::new;

    /**
     * Not validated.
     */
    Notification(
            boolean enabled,
            CheckOwnMode checkOwnMode,
            boolean inclusionEnabled,
            boolean exclusionEnabled,
            boolean responseEnabled,
            Sound sound,
            TextStyle textStyle,
            String replacementMsg,
            boolean replacementMsgEnabled,
            String statusBarMsg,
            boolean statusBarMsgEnabled,
            String titleMsg,
            boolean titleMsgEnabled,
            String subtitleMsg,
            boolean subtitleMsgEnabled,
            String toastMsg,
            boolean toastMsgEnabled,
            String typedMsg,
            boolean typedMsgEnabled,
            String clipboardMsg,
            boolean clipboardMsgEnabled,
            boolean soundSync,
            int delay,
            int titleFadeIn,
            int titleStay,
            int titleFadeOut,
            int statusBarStay,
            int toastStay,
            List<Trigger> triggers,
            List<Trigger> inclusionTriggers,
            List<Trigger> exclusionTriggers,
            List<Response> responses
    ) {
        this.enabled = enabled;
        this.checkOwnMode = checkOwnMode;
        this.inclusionEnabled = inclusionEnabled;
        this.exclusionEnabled = exclusionEnabled;
        this.responseEnabled = responseEnabled;
        this.sound = sound;
        this.textStyle = textStyle;
        this.replacementMsg = replacementMsg;
        this.replacementMsgEnabled = replacementMsgEnabled;
        this.statusBarMsg = statusBarMsg;
        this.statusBarMsgEnabled = statusBarMsgEnabled;
        this.titleMsg = titleMsg;
        this.titleMsgEnabled = titleMsgEnabled;
        this.subtitleMsg = subtitleMsg;
        this.subtitleMsgEnabled = subtitleMsgEnabled;
        this.toastMsg = toastMsg;
        this.toastMsgEnabled = toastMsgEnabled;
        this.typedMsg = typedMsg;
        this.typedMsgEnabled = typedMsgEnabled;
        this.clipboardMsg = clipboardMsg;
        this.clipboardMsgEnabled = clipboardMsgEnabled;
        this.soundSync = soundSync;
        this.delay = delay;
        this.titleFadeIn = titleFadeIn;
        this.titleStay = titleStay;
        this.titleFadeOut = titleFadeOut;
        this.statusBarStay = statusBarStay;
        this.toastStay = toastStay;
        this.triggers = triggers;
        this.inclusionTriggers = inclusionTriggers;
        this.exclusionTriggers = exclusionTriggers;
        this.responses = responses;
    }

    /**
     * Creates a new {@link Notification} for the user's name, with two default placeholder
     * {@link Trigger}s.
     */
    static Notification createUser() {
        return new Notification(
                enabledDefault,
                CheckOwnMode.values()[0],
                inclusionEnabledDefault,
                exclusionEnabledDefault,
                responseEnabledDefault,
                soundDefault.get(),
                textStyleDefault.get(),
                replacementMsgDefault,
                replacementMsgEnabledDefault,
                statusBarMsgDefault,
                statusBarMsgEnabledDefault,
                titleMsgDefault,
                titleMsgEnabledDefault,
                subtitleMsgDefault,
                subtitleMsgEnabledDefault,
                toastMsgDefault,
                toastMsgEnabledDefault,
                typedMsgDefault,
                typedMsgEnabledDefault,
                clipboardMsgDefault,
                clipboardMsgEnabledDefault,
                soundSyncDefault,
                timingDelayDefault,
                titleFadeInDefault,
                titleStayDefault,
                titleFadeOutDefault,
                statusBarStayDefault,
                toastStayDefault,
                new ArrayList<>(List.of(new Trigger("Profile name"), new Trigger("Display name"))),
                inclusionTriggersDefault.get(),
                exclusionTriggersDefault.get(),
                responsesDefault.get()
        );
    }

    /**
     * Creates a new generic {@link Notification}, with a single blank {@link Trigger}.
     */
    static Notification createBlank(Sound sound, TextStyle textStyle) {
        return new Notification(
                enabledDefault,
                CheckOwnMode.values()[0],
                inclusionEnabledDefault,
                exclusionEnabledDefault,
                responseEnabledDefault,
                sound,
                textStyle,
                replacementMsgDefault,
                replacementMsgEnabledDefault,
                statusBarMsgDefault,
                statusBarMsgEnabledDefault,
                titleMsgDefault,
                titleMsgEnabledDefault,
                subtitleMsgDefault,
                subtitleMsgEnabledDefault,
                toastMsgDefault,
                toastMsgEnabledDefault,
                typedMsgDefault,
                typedMsgEnabledDefault,
                clipboardMsgDefault,
                clipboardMsgEnabledDefault,
                soundSyncDefault,
                timingDelayDefault,
                titleFadeInDefault,
                titleStayDefault,
                titleFadeOutDefault,
                statusBarStayDefault,
                toastStayDefault,
                new ArrayList<>(List.of(new Trigger(""))),
                inclusionTriggersDefault.get(),
                exclusionTriggersDefault.get(),
                responsesDefault.get()
        );
    }

    /**
     * @return {@code true} if this instance is eligible for triggering (on a message sent by the
     * user if {@code ownMsg} is {@code true}).
     */
    public boolean canBeTriggered(boolean ownMsg) {
        if (enabled && !editing) {
            if (ownMsg) {
                return switch (checkOwnMode) {
                    case DEFER -> Config.get().checkOwnMessages;
                    case ON -> true;
                    case OFF -> false;
                };
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    // List reordering

    /**
     * Moves the {@link Trigger} at the source index to the destination index in the list.
     *
     * @param srcIdx the index of the element to move.
     * @param dstIdx the desired final index of the element.
     * @return {@code true} if the list was modified.
     */
    public boolean moveTrigger(int srcIdx, int dstIdx) {
        if (srcIdx != dstIdx) {
            triggers.add(dstIdx, triggers.remove(srcIdx));
            return true;
        }
        return false;
    }

    /**
     * Moves the inclusion {@link Trigger} at the source index to the destination index in the
     * list.
     *
     * @param srcIdx the index of the element to move.
     * @param dstIdx the desired final index of the element.
     * @return {@code true} if the list was modified.
     */
    public boolean moveInclusionTrigger(int srcIdx, int dstIdx) {
        if (srcIdx != dstIdx) {
            inclusionTriggers.add(dstIdx, inclusionTriggers.remove(srcIdx));
            return true;
        }
        return false;
    }

    /**
     * Moves the exclusion {@link Trigger} at the source index to the destination index in the
     * list.
     *
     * @param srcIdx the index of the element to move.
     * @param dstIdx the desired final index of the element.
     * @return {@code true} if the list was modified.
     */
    public boolean moveExclusionTrigger(int srcIdx, int dstIdx) {
        if (srcIdx != dstIdx) {
            exclusionTriggers.add(dstIdx, exclusionTriggers.remove(srcIdx));
            return true;
        }
        return false;
    }

    /**
     * Moves the {@link Response} at the source index to the destination index in the list.
     *
     * @param srcIdx the index of the element to move.
     * @param dstIdx the desired final index of the element.
     * @return {@code true} if the list was modified.
     */
    public boolean moveResponseMessage(int srcIdx, int dstIdx) {
        if (srcIdx != dstIdx) {
            responses.add(dstIdx, responses.remove(srcIdx));
            return true;
        }
        return false;
    }

    /**
     * Options UI utility.
     *
     * @return a string formed by concatenating all {@link Trigger} strings together.
     */
    @Override
    public String getString() {
        StringBuilder sb = new StringBuilder();
        for (Trigger t : triggers) {
            sb.append(t.getString());
        }
        return sb.toString();
    }

    // Validation

    /**
     * Validates this instance. Called after deserialization and before saving.
     */
    Notification validate() {
        textStyle.validate();
        sound.validate();

        triggers.removeIf(t -> {
            t.validate();
            return t.string.isBlank();
        });

        inclusionTriggers.removeIf(t -> {
            t.validate();
            return t.string.isBlank();
        });

        exclusionTriggers.removeIf(t -> {
            t.validate();
            return t.string.isBlank();
        });

        responses.removeIf(m -> {
            m.validate();
            return m.string.isBlank();
        });

        return this;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Notification> {

        @Override
        public Notification deserialize(
                JsonElement json,
                java.lang.reflect.Type typeOfT,
                JsonDeserializationContext ctx
        ) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            boolean enabled = JsonUtil.getOrDefault(
                    obj,
                    "enabled",
                    enabledDefault,
                    silent
            );

            boolean inclusionEnabled = JsonUtil.getOrDefault(
                    obj,
                    "inclusionEnabled",
                    inclusionEnabledDefault,
                    silent
            );

            boolean exclusionEnabled = JsonUtil.getOrDefault(
                    obj,
                    "exclusionEnabled",
                    exclusionEnabledDefault,
                    silent
            );

            CheckOwnMode checkOwnMode = JsonUtil.getOrDefault(
                    obj,
                    "checkOwnMode",
                    CheckOwnMode.class,
                    CheckOwnMode.values()[0],
                    silent
            );

            boolean responseEnabled = JsonUtil.getOrDefault(
                    obj,
                    "responseEnabled",
                    responseEnabledDefault,
                    silent
            );

            Sound sound = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "sound",
                    Sound.class,
                    soundDefault.get(),
                    silent
            );

            TextStyle textStyle = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "textStyle",
                    TextStyle.class,
                    textStyleDefault.get(),
                    silent
            );

            String replacementMsg = JsonUtil.getOrDefault(
                    obj,
                    "replacementMsg",
                    replacementMsgDefault,
                    silent
            );

            boolean replacementMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "replacementMsgEnabled",
                    replacementMsgEnabledDefault,
                    silent
            );

            String statusBarMsg = JsonUtil.getOrDefault(
                    obj,
                    "statusBarMsg",
                    statusBarMsgDefault,
                    silent
            );

            boolean statusBarMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "statusBarMsgEnabled",
                    statusBarMsgEnabledDefault,
                    silent
            );

            String titleMsg = JsonUtil.getOrDefault(
                    obj,
                    "titleMsg",
                    titleMsgDefault,
                    silent
            );

            boolean titleMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "titleMsgEnabled",
                    titleMsgEnabledDefault,
                    silent
            );

            String subtitleMsg = JsonUtil.getOrDefault(
                    obj,
                    "subtitleMsg",
                    subtitleMsgDefault,
                    silent
            );

            boolean subtitleMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "subtitleMsgEnabled",
                    subtitleMsgEnabledDefault,
                    silent
            );

            String toastMsg = JsonUtil.getOrDefault(
                    obj,
                    "toastMsg",
                    toastMsgDefault,
                    silent
            );

            boolean toastMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "toastMsgEnabled",
                    toastMsgEnabledDefault,
                    silent
            );

            String typedMsg = JsonUtil.getOrDefault(
                    obj,
                    "typedMsg",
                    typedMsgDefault,
                    silent
            );

            boolean typedMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "typedMsgEnabled",
                    typedMsgEnabledDefault,
                    silent
            );

            String clipboardMsg = JsonUtil.getOrDefault(
                    obj,
                    "clipboardMsg",
                    clipboardMsgDefault,
                    silent
            );

            boolean clipboardMsgEnabled = JsonUtil.getOrDefault(
                    obj,
                    "clipboardMsgEnabled",
                    clipboardMsgEnabledDefault,
                    silent
            );

            boolean soundSync = JsonUtil.getOrDefault(
                obj,
                "soundSync",
                soundSyncDefault,
                silent
            );

            int delay = JsonUtil.getOrDefault(
                obj,
                "delay",
                timingDelayDefault,
                silent
            );

            int titleFadeIn = JsonUtil.getOrDefault(
                obj,
                "titleFadeIn",
                titleFadeInDefault,
                silent
            );

            int titleStay = JsonUtil.getOrDefault(
                obj,
                "titleStay",
                titleStayDefault,
                silent
            );

            int titleFadeOut = JsonUtil.getOrDefault(
                obj,
                "titleFadeOut",
                titleFadeOutDefault,
                silent
            );

            int statusBarStay = JsonUtil.getOrDefault(
                obj,
                "statusBarStay",
                statusBarStayDefault,
                silent
            );

            int toastStay = JsonUtil.getOrDefault(
                obj,
                "toastStay",
                toastStayDefault,
                silent
            );

            List<Trigger> triggers = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "triggers",
                    Trigger.class,
                    triggersDefault.get(),
                    silent
            );

            List<Trigger> inclusionTriggers = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "inclusionTriggers",
                    Trigger.class,
                    inclusionTriggersDefault.get(),
                    silent
            );

            List<Trigger> exclusionTriggers = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "exclusionTriggers",
                    Trigger.class,
                    exclusionTriggersDefault.get(),
                    silent
            );

            List<Response> responses = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "responses",
                    Response.class,
                    responsesDefault.get(),
                    silent
            );
            if (version < 8) { // pre-v2.4.7 (2025-07-10)
                responses = JsonUtil.getOrDefault(
                        ctx,
                        obj,
                        "responseMessages",
                        Response.class,
                        responsesDefault.get(),
                        true
                );
            }
            if (version <= 3) {
                int totalDelay = 0;
                for (Response resMsg : responses) {
                    resMsg.delayTicks -= totalDelay;
                    totalDelay += resMsg.delayTicks;
                }
            }

            return new Notification(
                    enabled,
                    checkOwnMode,
                    inclusionEnabled,
                    exclusionEnabled,
                    responseEnabled,
                    sound,
                    textStyle,
                    replacementMsg,
                    replacementMsgEnabled,
                    statusBarMsg,
                    statusBarMsgEnabled,
                    titleMsg,
                    titleMsgEnabled,
                    subtitleMsg,
                    subtitleMsgEnabled,
                    toastMsg,
                    toastMsgEnabled,
                    typedMsg,
                    typedMsgEnabled,
                    clipboardMsg,
                    clipboardMsgEnabled,
                    soundSync,
                    delay,
                    titleFadeIn,
                    titleStay,
                    titleFadeOut,
                    statusBarStay,
                    toastStay,
                    triggers,
                    inclusionTriggers,
                    exclusionTriggers,
                    responses
            ).validate();
        }
    }
}
