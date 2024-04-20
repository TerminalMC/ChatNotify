/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.config;

import java.util.*;

/**
 * A {@code Notification} consists of activation criteria, and parameters for
 * all ChatNotify user-notification functions.
 */
public class Notification {
    // Not saved, not modifiable by user
    public transient boolean editing = false;

    // Saved, modifiable by user
    private boolean enabled;
    public boolean allowRegex;
    public boolean exclusionEnabled;
    public boolean responseEnabled;
    public final Sound sound;
    public final TextStyle textStyle;
    public final ArrayList<Trigger> triggers;
    public final ArrayList<Trigger> exclusionTriggers;
    public final ArrayList<String> responseMessages;

    public Notification() {
        this.enabled = true;
        this.allowRegex = false;
        this.exclusionEnabled = false;
        this.responseEnabled = false;
        this.sound = new Sound();
        this.textStyle = new TextStyle();
        this.triggers = new ArrayList<>();
        this.exclusionTriggers = new ArrayList<>();
        this.responseMessages = new ArrayList<>();
    }

    public Notification(boolean enabled, boolean allowRegex, boolean exclusionEnabled,
                        boolean responseEnabled, Sound sound, TextStyle textStyle,
                        ArrayList<Trigger> triggers, ArrayList<Trigger> exclusionTriggers,
                        ArrayList<String> responseMessages) {
        this.enabled = enabled;
        this.allowRegex = allowRegex;
        this.exclusionEnabled = exclusionEnabled;
        this.responseEnabled = responseEnabled;
        this.sound = sound;
        this.textStyle = textStyle;
        this.triggers = triggers;
        this.exclusionTriggers = exclusionTriggers;
        this.responseMessages = responseMessages;
    }

    public static Notification createUserNotification() {
        return new Notification(true, false, false, false,
                new Sound(), new TextStyle(), new ArrayList<>(List.of(
                        new Trigger("Profile name"), new Trigger("Display name"))),
                new ArrayList<>(), new ArrayList<>());
    }

    public static Notification createBlankNotification() {
        return new Notification(true, false, false, false,
                new Sound(), new TextStyle(), new ArrayList<>(List.of(new Trigger(""))),
                new ArrayList<>(), new ArrayList<>());
    }



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
     * If all notification options are disabled, disables the
     * {@code Notification}.
     */
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
        allowRegex = false;
        exclusionEnabled = false;
        responseEnabled = false;
        exclusionTriggers.clear();
        responseMessages.clear();
    }

    /**
     * Removes all blank triggers.
     */
    public void purgeTriggers() {
        triggers.removeIf((trigger) -> trigger.string.isBlank());
    }

    /**
     * Removes all blank exclusion triggers, and disables exclusion if there are
     * none remaining.
     */
    public void purgeExclusionTriggers() {
        exclusionTriggers.removeIf((trigger) -> trigger.string.isBlank());
        if (exclusionTriggers.isEmpty()) exclusionEnabled = false;
    }

    /**
     * Removes all blank response messages, and disables response if there are
     * none remaining.
     */
    public void purgeResponseMessages() {
        responseMessages.removeIf(String::isBlank);
        if (responseMessages.isEmpty()) responseEnabled = false;
    }
}