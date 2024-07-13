/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class Trigger {
    public final int version = 2;

    public boolean enabled;
    public String string;
    public @Nullable String styleString;
    public boolean isKey;
    public boolean isRegex;

    /**
     * Creates a default instance.
     */
    public Trigger() {
        this.enabled = true;
        this.string = "";
        this.styleString = null;
        this.isKey = false;
        this.isRegex = false;
    }

    /**
     * Creates a default instance with the specified value.
     */
    public Trigger(String string) {
        this.enabled = true;
        this.string = string;
        this.styleString = null;
        this.isKey = false;
        this.isRegex = false;
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    Trigger(boolean enabled, String string, @Nullable String styleString, boolean isKey, boolean isRegex) {
        this.enabled = enabled;
        this.string = string;
        this.styleString = styleString;
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
                String styleString = obj.has("styleString")
                        ? obj.get("styleString").getAsString() : null;
                boolean isKey = obj.get("isKey").getAsBoolean();
                boolean isRegex = obj.get("isRegex").getAsBoolean();

                return new Trigger(enabled, string, styleString, isKey, isRegex);
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize Trigger", e);
                return null;
            }
        }
    }
}
