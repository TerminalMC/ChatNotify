package com.notryken.chatnotify.config.serialize;

import com.google.gson.*;
import com.notryken.chatnotify.config.*;
import net.minecraft.sounds.SoundSource;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * {@code Config} deserializer. Backwards-compatible to ChatNotify v1.0.2.
 */
public class LegacyConfigDeserializer implements JsonDeserializer<Config> {
    @Override
    public Config deserialize(JsonElement jsonGuiEventListener, Type type,
                              JsonDeserializationContext context) throws JsonParseException {
        JsonObject configObject = jsonGuiEventListener.getAsJsonObject();

        boolean mixinEarly; // v1.2.0-pre.2
        boolean checkOwnMessages;
        SoundSource soundSource; // v1.1.0
        ArrayList<String> messagePrefixes = new ArrayList<>();
        ArrayList<Notification> notifications = new ArrayList<>();

        mixinEarly = configObject.has("mixinEarly") ?
                configObject.get("mixinEarly").getAsBoolean() :
                false;

        checkOwnMessages = !configObject.get("ignoreOwnMessages").getAsBoolean();

        soundSource = configObject.has("notifSoundSource") ?
                SoundSource.valueOf(configObject.get("notifSoundSource").getAsString()) :
                Config.DEFAULT_SOUND_SOURCE;

        for (JsonElement je : configObject.get("messagePrefixes").getAsJsonArray()) {
            messagePrefixes.add(je.getAsString());
        }

        for (JsonElement je : configObject.get("notifications").getAsJsonArray()) {
            JsonObject notifObject = je.getAsJsonObject();

            // Legacy-only data

            boolean triggerIsKey;
            ArrayList<Boolean> controls = new ArrayList<>();
            ArrayList<Boolean> formatControls = new ArrayList<>();
            ArrayList<String> triggerStrings = new ArrayList<>();
            ArrayList<String> exclusionTriggerStrings = new ArrayList<>();

            triggerIsKey = notifObject.get("triggerIsKey").getAsBoolean();

            for (JsonElement je2 : notifObject.get("controls").getAsJsonArray()) {
                controls.add(je2.getAsBoolean());
            }

            for (JsonElement je2 : notifObject.get("formatControls").getAsJsonArray()) {
                formatControls.add(je2.getAsBoolean());
            }

            for (JsonElement je2 : notifObject.get("triggers").getAsJsonArray()) {
                triggerStrings.add(je2.getAsString());
            }

            for (JsonElement je2 : notifObject.get("exclusionTriggers").getAsJsonArray()) {
                exclusionTriggerStrings.add(je2.getAsString());
            }

            // New data

            boolean enabled;
            boolean allowRegex;
            boolean exclusionEnabled;
            boolean responseEnabled;
            Sound sound;
            TextStyle textStyle;
            ArrayList<Trigger> triggers = new ArrayList<>();
            ArrayList<Trigger> exclusionTriggers = new ArrayList<>();
            ArrayList<String> responseMessages = new ArrayList<>();

            enabled = notifObject.get("enabled").getAsBoolean();

            allowRegex = notifObject.get("regexEnabled").getAsBoolean();

            exclusionEnabled = notifObject.get("exclusionEnabled").getAsBoolean();

            responseEnabled = notifObject.get("responseEnabled").getAsBoolean();

            String id = Sound.DEFAULT_SOUND_ID;
            JsonObject soundObject = notifObject.get("sound").getAsJsonObject();
            if (soundObject.has("path")) { // NeoForge
                id = soundObject.get("path").getAsString();
            }
            else if (soundObject.has("field_13355")) { // Fabric
                id = soundObject.get("field_13355").getAsString();
            }
            else if (soundObject.has("f_135805_")) {
                id = soundObject.get("f_135805_").getAsString(); // Forge
            }
            sound = new Sound(
                    controls.get(2),
                    id,
                    notifObject.get("soundVolume").getAsFloat(),
                    notifObject.get("soundPitch").getAsFloat());

            int color = TextStyle.DEFAULT_COLOR;
            if (notifObject.has("color")) {
                JsonObject textColorObject = notifObject.get("color").getAsJsonObject();
                if (textColorObject.has("value")) { // NeoForge
                    color = textColorObject.get("value").getAsInt();
                }
                else if (textColorObject.has("field_24364")) { // Fabric
                    color = textColorObject.get("field_24364").getAsInt();
                }
                else if (textColorObject.has("f_131257_")) {
                    color = textColorObject.get("f_131257_").getAsInt(); // Forge
                }
            }
            textStyle = new TextStyle(
                    controls.get(0),
                    color,
                    formatControls.get(0) ? new TriState(TriState.State.ON) : new TriState(TriState.State.DISABLED),
                    formatControls.get(1) ? new TriState(TriState.State.ON) : new TriState(TriState.State.DISABLED),
                    formatControls.get(2) ? new TriState(TriState.State.ON) : new TriState(TriState.State.DISABLED),
                    formatControls.get(3) ? new TriState(TriState.State.ON) : new TriState(TriState.State.DISABLED),
                    formatControls.get(4) ? new TriState(TriState.State.ON) : new TriState(TriState.State.DISABLED));

            for (String triggerStr : triggerStrings) {
                triggers.add(new Trigger(triggerStr, true, triggerIsKey, allowRegex));
            }

            for (String exclTriggerStr : exclusionTriggerStrings) {
                exclusionTriggers.add(new Trigger(exclTriggerStr));
            }

            for (JsonElement je2 : notifObject.get("responseMessages").getAsJsonArray()) {
                responseMessages.add(je2.getAsString());
            }

            notifications.add(new Notification(enabled, allowRegex, exclusionEnabled, responseEnabled,
                    sound, textStyle, triggers, exclusionTriggers, responseMessages));
        }

        // Ensure username Notification is valid
        if (notifications.isEmpty()) {
            notifications.add(Notification.createUserNotification());
        }
        else if (notifications.get(0).triggers.size() < 2) {
            notifications.set(0, Notification.createUserNotification());
        }

        return new Config(mixinEarly, checkOwnMessages, soundSource, messagePrefixes, notifications);
    }
}
