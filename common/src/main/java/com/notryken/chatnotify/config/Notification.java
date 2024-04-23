/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.config;

import com.google.gson.*;
import com.notryken.chatnotify.ChatNotify;
import com.notryken.chatnotify.config.util.JsonRequired;
import com.notryken.chatnotify.config.util.JsonValidator;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A Notification consists of activation criteria and audio/visual notification
 * parameters.
 */
public class Notification {
    public final int version = 1;

    // Flags the notification as being actively edited.
    public transient boolean editing = false;

    // Options
    @JsonRequired private boolean enabled;
    @JsonRequired public boolean exclusionEnabled;
    @JsonRequired public boolean responseEnabled;
    @JsonRequired public final Sound sound;
    @JsonRequired public final TextStyle textStyle;
    @JsonRequired public final List<Trigger> triggers;
    @JsonRequired public final List<Trigger> exclusionTriggers;
    @JsonRequired public final List<ResponseMessage> responseMessages;

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

    public Notification(boolean enabled, boolean exclusionEnabled, boolean responseEnabled,
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

    public static Notification createUser() {
        return new Notification(true, false, false,
                new Sound(), new TextStyle(), new ArrayList<>(List.of(
                        new Trigger("Profile name"), new Trigger("Display name"))),
                new ArrayList<>(), new ArrayList<>());
    }

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

    public void autoDisable() {
        if (!sound.isEnabled() && !textStyle.isEnabled()) {
            enabled = false;
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
            if (t.isKey) t.string = t.string.toLowerCase(Locale.ROOT);
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
            if (t.isKey) t.string = t.string.toLowerCase(Locale.ROOT);
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
        @SuppressWarnings("unchecked")
        public @Nullable Notification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            try {
                boolean enabled = obj.get("enabled").getAsBoolean();
                boolean exclusionEnabled = obj.get("exclusionEnabled").getAsBoolean();
                boolean responseEnabled = obj.get("responseEnabled").getAsBoolean();
                Sound sound = ctx.deserialize(obj.get("sound"), Sound.class);
                TextStyle textStyle = ctx.deserialize(obj.get("textStyle"), TextStyle.class);
                List<Trigger> triggers = new ArrayList<>((List<Trigger>) (List<?>)
                        obj.getAsJsonArray("triggers")
                                .asList().stream().map(je -> ctx.deserialize(je, Trigger.class))
                                .filter(Objects::nonNull).toList());
                List<Trigger> exclusionTriggers = new ArrayList<>((List<Trigger>) (List<?>)
                        obj.getAsJsonArray("exclusionTriggers")
                                .asList().stream().map(je -> ctx.deserialize(je, Trigger.class))
                                .filter(Objects::nonNull).toList());
                List<ResponseMessage> responseMessages = new ArrayList<>((List<ResponseMessage>) (List<?>)
                        obj.getAsJsonArray("responseMessages")
                                .asList().stream().map(je -> ctx.deserialize(je, ResponseMessage.class))
                                .filter(Objects::nonNull).toList());

                return new JsonValidator<Notification>().validateNonNull(
                        new Notification(enabled, exclusionEnabled, responseEnabled, sound,
                                textStyle, triggers, exclusionTriggers, responseMessages));
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize Notification", e);
                return null;
            }
        }
    }
}