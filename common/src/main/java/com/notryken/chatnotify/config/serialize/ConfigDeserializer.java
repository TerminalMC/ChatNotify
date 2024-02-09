package com.notryken.chatnotify.config.serialize;

import com.google.gson.*;
import com.notryken.chatnotify.config.*;
import net.minecraft.sounds.SoundSource;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConfigDeserializer implements JsonDeserializer<Config> {
    @Override
    public Config deserialize(JsonElement json, Type typeOfT,
                              JsonDeserializationContext context) throws JsonParseException {
        JsonObject configObject = json.getAsJsonObject();

        boolean mixinEarly;
        boolean checkOwnMessages;
        SoundSource soundSource;
        ArrayList<String> prefixes = new ArrayList<>();
        ArrayList<Notification> notifications = new ArrayList<>();

        mixinEarly = configObject.get("mixinEarly").getAsBoolean();

        checkOwnMessages = configObject.get("checkOwnMessages").getAsBoolean();

        soundSource = SoundSource.valueOf(configObject.get("soundSource").getAsString());

        for (JsonElement je : configObject.get("prefixes").getAsJsonArray()) {
            prefixes.add(je.getAsString());
        }

        for (JsonElement je : configObject.get("notifications").getAsJsonArray()) {
            JsonObject notifObject = je.getAsJsonObject();

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
            allowRegex = notifObject.get("allowRegex").getAsBoolean();
            exclusionEnabled = notifObject.get("exclusionEnabled").getAsBoolean();
            responseEnabled = notifObject.get("responseEnabled").getAsBoolean();

            JsonObject soundObject = notifObject.get("sound").getAsJsonObject();
            sound = new Sound(
                    soundObject.get("enabled").getAsBoolean(),
                    soundObject.get("id").getAsString(),
                    soundObject.get("volume").getAsFloat(),
                    soundObject.get("pitch").getAsFloat());

            JsonObject textStyleObject = notifObject.get("textStyle").getAsJsonObject();
            textStyle = new TextStyle(
                    textStyleObject.get("doColor").getAsBoolean(),
                    textStyleObject.get("color").getAsInt(),
                    new TriState(TriState.State.valueOf(textStyleObject.get("bold")
                            .getAsJsonObject().get("state").getAsString())),
                    new TriState(TriState.State.valueOf(textStyleObject.get("italic")
                            .getAsJsonObject().get("state").getAsString())),
                    new TriState(TriState.State.valueOf(textStyleObject.get("underlined")
                            .getAsJsonObject().get("state").getAsString())),
                    new TriState(TriState.State.valueOf(textStyleObject.get("strikethrough")
                            .getAsJsonObject().get("state").getAsString())),
                    new TriState(TriState.State.valueOf(textStyleObject.get("obfuscated")
                            .getAsJsonObject().get("state").getAsString())));

            for (JsonElement je2 : notifObject.get("triggers").getAsJsonArray()) {
                JsonObject triggerObject = je2.getAsJsonObject();
                triggers.add(new Trigger(
                        triggerObject.get("string").getAsString(),
                        triggerObject.get("enabled").getAsBoolean(),
                        triggerObject.get("isKey").getAsBoolean(),
                        triggerObject.get("isRegex").getAsBoolean()));
            }

            for (JsonElement je2 : notifObject.get("exclusionTriggers").getAsJsonArray()) {
                JsonObject exclTriggerObject = je2.getAsJsonObject();
                exclusionTriggers.add(new Trigger(
                        exclTriggerObject.get("string").getAsString(),
                        exclTriggerObject.get("enabled").getAsBoolean(),
                        exclTriggerObject.get("isKey").getAsBoolean(),
                        exclTriggerObject.get("isRegex").getAsBoolean()));
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

        return new Config(mixinEarly, checkOwnMessages, soundSource, prefixes, notifications);
    }
}
