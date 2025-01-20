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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Notification {
    public final int version = 5;

    /**
     * Indicates that this instance is being edited, and should not be activated
     * irrespective of {@link Notification#enabled}
     */
    public transient boolean editing = false;

    // Options

    /**
     * Whether this instance is eligible for activation.
     */
    public boolean enabled;
    public static final boolean enabledDefault = true;

    /**
     * Whether this instance is eligible for activation on user-sent messages.
     */
    public CheckOwnMode checkOwnMode;
    public enum CheckOwnMode {
        DEFER,
        ON,
        OFF,
    }

    /**
     * Whether this instance allows use of exclusion triggers.
     */
    public boolean exclusionEnabled;
    public static final boolean exclusionEnabledDefault = false;

    /**
     * Whether this instance allows use of response messages.
     */
    public boolean responseEnabled;
    public static final boolean responseEnabledDefault = false;

    /**
     * The {@link Sound} to play on activation.
     */
    public final Sound sound;
    public static final Supplier<Sound> soundDefault = Sound::new;

    /**
     * The {@link TextStyle} to use for restyling messages on activation.
     */
    public final TextStyle textStyle;
    public static final Supplier<TextStyle> textStyleDefault = TextStyle::new;

    /**
     * Message to replace the triggering chat message.
     */
    public String replacementMsg;
    public static final String replacementMsgDefault = "";
    public boolean replacementMsgEnabled;
    public static final boolean replacementMsgEnabledDefault = false;

    /**
     * Message to display in the status bar (above the hotbar).
     */
    public String statusBarMsg;
    public static final String statusBarMsgDefault = "";
    public boolean statusBarMsgEnabled;
    public static final boolean statusBarMsgEnabledDefault = false;

    /**
     * Message to display in title text (large-font, center-screen).
     */
    public String titleMsg;
    public static final String titleMsgDefault = "";
    public boolean titleMsgEnabled;
    public static final boolean titleMsgEnabledDefault = false;

    /**
     * The list of {@link Trigger}s which can activate this instance.
     */
    public final List<Trigger> triggers;
    public static final Supplier<List<Trigger>> triggersDefault = ArrayList::new;

    /**
     * The list of {@link Trigger}s which can cancel activation of this 
     * instance.
     */
    public final List<Trigger> exclusionTriggers;
    public static final Supplier<List<Trigger>> exclusionTriggersDefault = ArrayList::new;

    /**
     * The list of {@link ResponseMessage}s to be sent on activation.
     */
    public final List<ResponseMessage> responseMessages;
    public static final Supplier<List<ResponseMessage>> responseMessagesDefault = ArrayList::new;

    /**
     * Not validated.
     */
    Notification(
            boolean enabled,
            CheckOwnMode checkOwnMode,
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
            List<Trigger> triggers,
            List<Trigger> exclusionTriggers,
            List<ResponseMessage> responseMessages
    ) {
        this.enabled = enabled;
        this.checkOwnMode = checkOwnMode;
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
        this.triggers = triggers;
        this.exclusionTriggers = exclusionTriggers;
        this.responseMessages = responseMessages;
    }

    /**
     * Creates a new {@link Notification} for the user's name, with two default 
     * placeholder {@link Trigger}s.
     */
    static Notification createUser() {
        return new Notification(
                enabledDefault,
                CheckOwnMode.values()[0],
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
                new ArrayList<>(List.of(
                        new Trigger("Profile name"),
                        new Trigger("Display name")
                )),
                exclusionTriggersDefault.get(),
                responseMessagesDefault.get()
        );
    }

    /**
     * Creates a new generic {@link Notification}, with a single blank 
     * {@link Trigger}.
     */
    static Notification createBlank(Sound sound, TextStyle textStyle) {
        return new Notification(
                enabledDefault,
                CheckOwnMode.values()[0],
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
                new ArrayList<>(List.of(
                        new Trigger("")
                )),
                exclusionTriggersDefault.get(),
                responseMessagesDefault.get()
        );
    }

    /**
     * @return {@code true} if this instance is eligible for activation, 
     * {@code false} otherwise.
     */
    public boolean canActivate(boolean ownMsg) {
        if (enabled && !editing) {
            if (ownMsg) {
                return switch(checkOwnMode) {
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
     * Moves the {@link Trigger} at the source index to the destination index 
     * in the list.
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     * @return {@code true} if the list was modified, {@code false} otherwise.
     */
    public boolean moveTrigger(int sourceIndex, int destIndex) {
        if (sourceIndex != destIndex) {
            triggers.add(destIndex, triggers.remove(sourceIndex));
            return true;
        }
        return false;
    }

    /**
     * Moves the exclusion {@link Trigger} at the source index to the
     * destination index in the list.
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     * @return {@code true} if the list was modified, {@code false} otherwise.
     */
    public boolean moveExclusionTrigger(int sourceIndex, int destIndex) {
        if (sourceIndex != destIndex) {
            exclusionTriggers.add(destIndex, exclusionTriggers.remove(sourceIndex));
            return true;
        }
        return false;
    }

    /**
     * Moves the {@link ResponseMessage} at the source index to the destination
     * index in the list.
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     * @return {@code true} if the list was modified, {@code false} otherwise.
     */
    public boolean moveResponseMessage(int sourceIndex, int destIndex) {
        if (sourceIndex != destIndex) {
            responseMessages.add(destIndex, responseMessages.remove(sourceIndex));
            return true;
        }
        return false;
    }
    
    // Reset
    
    /**
     * Sets all advanced settings to their respective defaults.
     */
    public void resetAdvanced() {
        exclusionEnabled = exclusionEnabledDefault;
        exclusionTriggers.clear();
        responseEnabled = responseEnabledDefault;
        responseMessages.clear();
    }

    // Cleanup and validation

    public void cleanup() {
        // Remove all blank triggers, and convert all key triggers to lowercase.
        triggers.removeIf(trigger -> trigger.string.isBlank());
        for (Trigger t : triggers) {
            if (t.type == Trigger.Type.KEY) t.string = t.string.toLowerCase();
            if (t.styleTarget.string.isBlank()) t.styleTarget.enabled = false;
        }

        // Remove all blank exclusion triggers, convert all key triggers to
        // lowercase, and disable exclusion if there are none remaining.
        exclusionTriggers.removeIf(trigger -> trigger.string.isBlank());
        if (exclusionTriggers.isEmpty()) exclusionEnabled = false;
        for (Trigger t : exclusionTriggers) {
            if (t.type == Trigger.Type.KEY) t.string = t.string.toLowerCase();
        }

        // Remove all blank response messages, and disable response if there are
        // none remaining.
        responseMessages.removeIf(responseMessage -> responseMessage.string.isBlank());
        if (responseMessages.isEmpty()) responseEnabled = false;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Notification> {
        @Override
        public @Nullable Notification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            
            String f = "enabled";
            boolean enabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : enabledDefault;

            f = "exclusionEnabled";
            boolean exclusionEnabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : exclusionEnabledDefault;

            f = "checkOwnMode";
            CheckOwnMode checkOwnMode = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(CheckOwnMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? CheckOwnMode.valueOf(obj.get(f).getAsString())
                        : CheckOwnMode.values()[0]
                    : CheckOwnMode.values()[0];

            f = "responseEnabled";
            boolean responseEnabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : responseEnabledDefault;

            f = "sound";
            Sound sound = obj.has(f) && obj.get(f).isJsonObject()
                    ? ctx.deserialize(obj.get(f), Sound.class)
                    : soundDefault.get();

            f = "textStyle";
            TextStyle textStyle = obj.has(f) && obj.get(f).isJsonObject()
                    ? ctx.deserialize(obj.get(f), TextStyle.class)
                    : textStyleDefault.get();

            f = "replacementMsg";
            String replacementMsg = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : replacementMsgDefault;

            f = "replacementMsgEnabled";
            boolean replacementMsgEnabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : replacementMsgEnabledDefault;

            f = "statusBarMsg";
            String statusBarMsg = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : statusBarMsgDefault;

            f = "statusBarMsgEnabled";
            boolean statusBarMsgEnabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : statusBarMsgEnabledDefault;

            f = "titleMsg";
            String titleMsg = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : titleMsgDefault;

            f = "titleMsgEnabled";
            boolean titleMsgEnabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : titleMsgEnabledDefault;

            f = "triggers";
            List<Trigger> triggers = obj.has(f) && obj.get(f).isJsonArray()
                    ? obj.getAsJsonArray(f).asList().stream()
                        .filter(JsonElement::isJsonObject)
                        .map((je) -> (Trigger)ctx.deserialize(je, Trigger.class)).toList()
                        .stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new))
                    : triggersDefault.get();

            f = "exclusionTriggers";
            List<Trigger> exclusionTriggers = obj.has(f) && obj.get(f).isJsonArray()
                    ? obj.getAsJsonArray(f).asList().stream()
                        .filter(JsonElement::isJsonObject)
                        .map((je) -> (Trigger)ctx.deserialize(je, Trigger.class)).toList()
                        .stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new))
                    : exclusionTriggersDefault.get();

            f = "responseMessages";
            List<ResponseMessage> responseMessages = obj.has(f) && obj.get(f).isJsonArray()
                    ? obj.getAsJsonArray(f).asList().stream()
                        .filter(JsonElement::isJsonObject)
                        .map((je) -> (ResponseMessage)ctx.deserialize(je, ResponseMessage.class)).toList()
                        .stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new))
                    : responseMessagesDefault.get();
            if (version <= 3) {
                int totalDelay = 0;
                for (ResponseMessage resMsg : responseMessages) {
                    resMsg.delayTicks -= totalDelay;
                    totalDelay += resMsg.delayTicks;
                }
            }

            return new Notification(
                    enabled,
                    checkOwnMode,
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
                    triggers,
                    exclusionTriggers,
                    responseMessages
            );
        }
    }
}