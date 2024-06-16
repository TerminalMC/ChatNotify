/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.util.JsonRequired;
import dev.terminalmc.chatnotify.config.util.JsonValidator;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class ResponseMessage {
    public final int version = 1;

    public transient int countdown;
    public transient String sendingString;

    @JsonRequired public boolean enabled;
    @JsonRequired public String string;
    @JsonRequired public boolean regexGroups;
    @JsonRequired public int delayTicks;

    public ResponseMessage() {
        enabled = true;
        string = "";
        regexGroups = false;
        delayTicks = 0;
    }

    public ResponseMessage(boolean enabled, String string, boolean regexGroups, int delayTicks) {
        this.enabled = enabled;
        this.string = string;
        this.regexGroups = regexGroups;
        this.delayTicks = delayTicks;
    }

    public static class Deserializer implements JsonDeserializer<ResponseMessage> {
        @Override
        public @Nullable ResponseMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            try {
                boolean enabled = obj.get("enabled").getAsBoolean();
                String string = obj.get("string").getAsString();
                boolean regexGroups = obj.get("regexGroups").getAsBoolean();
                int delayTicks = obj.get("delayTicks").getAsInt();

                return new JsonValidator<ResponseMessage>().validateNonNull(
                        new ResponseMessage(enabled, string, regexGroups, delayTicks));
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize ResponseMessage", e);
                return null;
            }
        }
    }
}
