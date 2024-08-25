/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Consists of activation criteria and audio/visual notification parameters.
 */
public class Notification {
    public final int version = 1;

    // Flags the notification as being actively edited.
    public transient boolean editing = false;

    // Options
    private boolean enabled;
    public boolean exclusionEnabled;
    public boolean responseEnabled;
    public final Sound sound;
    public final TextStyle textStyle;
    public final List<Trigger> triggers;
    public final List<Trigger> exclusionTriggers;
    public final List<ResponseMessage> responseMessages;

    /**
     * Initializes default configuration with all lists empty.
     */
    public Notification() {
        this.enabled = true;
        this.exclusionEnabled = false;
        this.responseEnabled = false;
        this.sound = new Sound();
        this.textStyle = new TextStyle();
        this.triggers = new ArrayList<>();
        this.exclusionTriggers = new ArrayList<>();
        this.responseMessages = new ArrayList<>();
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    Notification(boolean enabled, boolean exclusionEnabled, boolean responseEnabled,
                 Sound sound, TextStyle textStyle, List<Trigger> triggers,
                 List<Trigger> exclusionTriggers, List<ResponseMessage> responseMessages) {
        this.enabled = enabled;
        this.exclusionEnabled = exclusionEnabled;
        this.responseEnabled = responseEnabled;
        this.sound = sound;
        this.textStyle = textStyle;
        this.triggers = triggers;
        this.exclusionTriggers = exclusionTriggers;
        this.responseMessages = responseMessages;
    }

    /**
     * Creates a notification for the user's name, with two default placeholder
     * triggers.
     */
    public static Notification createUser() {
        return new Notification(true, false, false,
                new Sound(), new TextStyle(), new ArrayList<>(List.of(
                        new Trigger("Profile name"), new Trigger("Display name"))),
                new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Creates a blank notification with one blank placeholder trigger.
     */
    public static Notification createBlank(Sound sound, TextStyle textStyle) {
        return new Notification(true, false, false,
                sound, textStyle, new ArrayList<>(List.of(new Trigger(""))),
                new ArrayList<>(), new ArrayList<>());
    }


    // Automatically turn certain controls on and off for user convenience

    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * If all notification options are disabled and {@code enabled} is true,
     * enables notification sound and text color.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && !sound.isEnabled() && !textStyle.isEnabled()) {
            sound.setEnabled(true);
            textStyle.doColor = true;
        }
    }

    /**
     * Disables this notification if sound and text restyling are both disabled.
     */
    public void autoDisable() {
        if (!sound.isEnabled() && !textStyle.isEnabled()) {
            enabled = false;
        }
    }

    /**
     * Moves the {@link Trigger} at the source index to the destination index.
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     */
    public void moveTrigger(int sourceIndex, int destIndex) {
        if (sourceIndex != destIndex) {
            triggers.add(destIndex, triggers.remove(sourceIndex));
        }
    }

    /**
     * Moves the exclusion {@link Trigger} at the source index to the
     * destination index.
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     */
    public void moveExclusionTrigger(int sourceIndex, int destIndex) {
        if (sourceIndex != destIndex) {
            exclusionTriggers.add(destIndex, exclusionTriggers.remove(sourceIndex));
        }
    }

    /**
     * Moves the {@link ResponseMessage} at the source index to the destination
     * index.
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     */
    public void moveResponseMessage(int sourceIndex, int destIndex) {
        if (sourceIndex != destIndex) {
            responseMessages.add(destIndex, responseMessages.remove(sourceIndex));
        }
    }

    // Validation and cleanup

    /**
     * Disables regex, exclusion triggers and response messages and clears the
     * lists of the latter two.
     */
    public void resetAdvanced() {
        exclusionEnabled = false;
        responseEnabled = false;
        exclusionTriggers.clear();
        responseMessages.clear();
    }

    /**
     * Removes all blank triggers, and converts all key triggers to lowercase.
     */
    public void purgeTriggers() {
        triggers.removeIf(trigger -> trigger.string.isBlank());
        for (Trigger t : triggers) {
            if (t.type == Trigger.Type.KEY) t.string = t.string.toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Removes all blank exclusion triggers, converts all key triggers to
     * lowercase, and disables exclusion if there are none remaining.
     */
    public void purgeExclusionTriggers() {
        exclusionTriggers.removeIf(trigger -> trigger.string.isBlank());
        if (exclusionTriggers.isEmpty()) exclusionEnabled = false;
        for (Trigger t : exclusionTriggers) {
            if (t.type == Trigger.Type.KEY) t.string = t.string.toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Removes all blank response messages, and disables response if there are
     * none remaining.
     */
    public void purgeResponseMessages() {
        responseMessages.removeIf(responseMessage -> responseMessage.string.isBlank());
        if (responseMessages.isEmpty()) responseEnabled = false;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Notification> {
        @Override
        public @Nullable Notification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            try {
                boolean enabled = obj.get("enabled").getAsBoolean();
                boolean exclusionEnabled = obj.get("exclusionEnabled").getAsBoolean();
                boolean responseEnabled = obj.get("responseEnabled").getAsBoolean();
                Sound sound = ctx.deserialize(obj.get("sound"), Sound.class);
                TextStyle textStyle = ctx.deserialize(obj.get("textStyle"), TextStyle.class);
                List<Trigger> triggers = new ArrayList<>();
                for (JsonElement je : obj.getAsJsonArray("triggers")) {
                    Trigger t = ctx.deserialize(je, Trigger.class);
                    if (t != null) triggers.add(t);
                }
                List<Trigger> exclusionTriggers = new ArrayList<>();
                for (JsonElement je : obj.getAsJsonArray("exclusionTriggers")) {
                    Trigger t = ctx.deserialize(je, Trigger.class);
                    if (t != null) exclusionTriggers.add(t);
                }
                List<ResponseMessage> responseMessages = new ArrayList<>();
                for (JsonElement je : obj.getAsJsonArray("responseMessages")) {
                    ResponseMessage r = ctx.deserialize(je, ResponseMessage.class);
                    if (r != null) responseMessages.add(r);
                }

                // Validation
                if (sound == null) throw new JsonParseException("Notification #1");
                if (textStyle == null) throw new JsonParseException("Notification #1");

                return new Notification(enabled, exclusionEnabled, responseEnabled, sound,
                        textStyle, triggers, exclusionTriggers, responseMessages);
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize Notification", e);
                return null;
            }
        }
    }
}