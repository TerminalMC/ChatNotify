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

public class Trigger {
    public final int version = 1;

    @JsonRequired public boolean enabled;
    @JsonRequired public String string;
    @JsonRequired public boolean isKey;
    @JsonRequired public boolean isRegex;

    public Trigger() {
        this.enabled = true;
        this.string = "";
        this.isKey = false;
        this.isRegex = false;
    }

    public Trigger(String string) {
        this.enabled = true;
        this.string = string;
        this.isKey = false;
        this.isRegex = false;
    }

    public Trigger(boolean enabled, String string, boolean isKey, boolean isRegex) {
        this.enabled = enabled;
        this.string = string;
        this.isKey = isKey;
        this.isRegex = isRegex;
    }

    public static class Deserializer implements JsonDeserializer<Trigger> {
        @Override
        public @Nullable Trigger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            try {
                boolean enabled = obj.get("enabled").getAsBoolean();
                String string = obj.get("string").getAsString();
                boolean isKey = obj.get("isKey").getAsBoolean();
                boolean isRegex = obj.get("isRegex").getAsBoolean();

                return new JsonValidator<Trigger>().validateNonNull(
                        new Trigger(enabled, string, isKey, isRegex));
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize Trigger", e);
                return null;
            }
        }
    }
}
