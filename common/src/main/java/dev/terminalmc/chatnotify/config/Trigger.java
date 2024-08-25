/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger {
    public final int version = 3;

    public enum Type {
        NORMAL,
        REGEX,
        KEY,
    }

    public static String iconOf(Type t) {
        return switch(t) {
            case NORMAL -> "~";
            case REGEX -> ".*";
            case KEY -> "\uD83D\uDD11";
        };
    }

    public boolean enabled;

    public @NotNull String string;
    public transient @Nullable Pattern pattern;
    public @Nullable String styleString;
    public Type type;

    /**
     * Creates a default instance.
     */
    public Trigger() {
        this.enabled = true;
        this.string = "";
        this.styleString = null;
        this.type = Type.NORMAL;
    }

    /**
     * Creates a default instance with the specified value.
     */
    public Trigger(@NotNull String string) {
        this.enabled = true;
        this.string = string;
        this.styleString = null;
        this.type = Type.NORMAL;
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    Trigger(boolean enabled, @NotNull String string, @Nullable String styleString, Type type) {
        this.enabled = enabled;
        this.string = string;
        this.styleString = styleString;
        this.type = type;
    }

    public void tryCompilePattern() {
        try {
            pattern = Pattern.compile(string);
        } catch (PatternSyntaxException e) {
            ChatNotify.LOG.warn("ChatNotify: Error processing regex: " + e);
            pattern = null;
        }
    }

    public static class Deserializer implements JsonDeserializer<Trigger> {
        @Override
        public @Nullable Trigger deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                             JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            try {
                int version = obj.get("version").getAsInt();

                boolean enabled = obj.get("enabled").getAsBoolean();
                String string = obj.get("string").getAsString();
                String styleString = obj.has("styleString")
                        ? obj.get("styleString").getAsString() : null;

                Type type;
                if (version < 3) {
                    boolean isKey = obj.get("isKey").getAsBoolean();
                    boolean isRegex = obj.get("isRegex").getAsBoolean();
                    type = isKey ? Type.KEY : (isRegex ? Type.REGEX : Type.NORMAL);
                } else {
                    type = Type.valueOf(obj.get("type").getAsString());
                }

                return new Trigger(enabled, string, styleString, type);
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize Trigger", e);
                return null;
            }
        }
    }
}
