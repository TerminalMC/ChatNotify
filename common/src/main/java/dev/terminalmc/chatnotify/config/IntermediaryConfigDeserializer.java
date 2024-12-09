/*
 * Copyright 2024 TerminalMC
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
import net.minecraft.sounds.SoundSource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compatible with config files generated by ChatNotify versions 1.2.0-pre.3 to
 * 1.2.0 (inclusive).
 */
public class IntermediaryConfigDeserializer implements JsonDeserializer<Config> {
    @Override
    @SuppressWarnings("unchecked")
    public Config deserialize(JsonElement json, Type typeOfT,
                              JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        TriState mixinEarly = new TriState(obj.get("mixinEarly").getAsBoolean() ? TriState.State.ON : TriState.State.DISABLED);
        boolean checkOwnMessages = obj.get("checkOwnMessages").getAsBoolean();
        SoundSource soundSource = SoundSource.valueOf(obj.get("soundSource").getAsString());
        int defaultColor = Config.DEFAULT_COLOR;
        Sound defaultSound = Config.DEFAULT_SOUND;
        List<String> prefixes = new ArrayList<>(
                obj.getAsJsonArray("prefixes")
                        .asList().stream().map(JsonElement::getAsString).toList());
        List<Notification> notifications = new ArrayList<>();

        for (JsonElement je : obj.get("notifications").getAsJsonArray()) {
            JsonObject notifObj = je.getAsJsonObject();

            boolean enabled = notifObj.get("enabled").getAsBoolean();
            boolean exclusionEnabled = notifObj.get("exclusionEnabled").getAsBoolean();
            boolean responseEnabled = notifObj.get("responseEnabled").getAsBoolean();
            Sound sound = ctx.deserialize(notifObj.get("sound"), Sound.class);
            TextStyle textStyle = ctx.deserialize(notifObj.get("textStyle"), TextStyle.class);
            List<Trigger> triggers = new ArrayList<>((List<Trigger>) (List<?>)
                    notifObj.getAsJsonArray("triggers")
                            .asList().stream().map(je2 -> ctx.deserialize(je2, Trigger.class))
                            .filter(Objects::nonNull).toList());
            List<Trigger> exclusionTriggers = new ArrayList<>((List<Trigger>) (List<?>)
                    notifObj.getAsJsonArray("exclusionTriggers")
                            .asList().stream().map(je2 -> ctx.deserialize(je2, Trigger.class))
                            .filter(Objects::nonNull).toList());
            List<ResponseMessage> responseMessages = new ArrayList<>(
                    notifObj.getAsJsonArray("responseMessages")
                    .asList().stream().map(je2 -> new ResponseMessage(
                            true, je2.getAsString(), ResponseMessage.Type.NORMAL, 0)).toList());

            notifications.add(new Notification(enabled, exclusionEnabled, responseEnabled, false,
                    sound, textStyle, new TitleText(), triggers, exclusionTriggers, responseMessages));
        }

        // Validate username notification
        if (notifications.isEmpty()) {
            notifications.add(Notification.createUser());
        }
        else if (notifications.getFirst().triggers.size() < 2) {
            notifications.set(0, Notification.createUser());
        }

        return new Config(mixinEarly, Config.DebugMode.OFF, checkOwnMessages, false, 
                soundSource, defaultColor, defaultSound, prefixes, notifications);
    }
}
