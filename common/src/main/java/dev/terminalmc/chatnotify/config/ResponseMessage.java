/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class ResponseMessage {
    public final int version = 1;

    public transient int countdown;
    public transient String sendingString;

    public boolean enabled;
    public String string;
    public boolean regexGroups;
    public int delayTicks;

    /**
     * Creates a default instance.
     */
    public ResponseMessage() {
        enabled = true;
        string = "";
        regexGroups = false;
        delayTicks = 0;
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    ResponseMessage(boolean enabled, String string, boolean regexGroups, int delayTicks) {
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

                // Validation
                if (delayTicks < 0) throw new JsonParseException("ResponseMessage #1");

                return new ResponseMessage(enabled, string, regexGroups, delayTicks);
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize ResponseMessage", e);
                return null;
            }
        }
    }
}
