/*
 * Copyright 2025 TerminalMC
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
import dev.terminalmc.chatnotify.ChatNotify;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger {
    public final int version = 3;

    /**
     * Not currently used.
     */
    public boolean enabled;
    public static final boolean enabledDefault = true;

    /**
     * The trigger string.
     */
    public @NotNull String string;
    public static final @NotNull String stringDefault = "";

    /**
     * A regex {@link Pattern} compiled from {@link Trigger#string}, or 
     * {@code null} if the string could not be compiled.
     */
    public transient @Nullable Pattern pattern;

    /**
     * The message substring string to attempt to restyle when this trigger 
     * matches a message.
     */
    public @Nullable String styleString;
    public static final @Nullable String styleStringDefault = null;

    /**
     * Controls how {@link Trigger#string} is compared to messages.
     */
    public Type type;
    public enum Type {
        /**
         * Normal 'fuzzy' matching.
         */
        NORMAL("~"),
        /**
         * Compile the trigger string as a regex pattern for matching.
         */
        REGEX(".*"),
        /**
         * Compare the trigger string against the message translation key.
         */
        KEY("\uD83D\uDD11");

        public final String icon;

        Type(String icon) {
            this.icon = icon;
        }
    }

    /**
     * Creates a default instance.
     */
    public Trigger() {
        this(stringDefault);
    }

    /**
     * Creates a default instance with the specified value.
     */
    public Trigger(@NotNull String string) {
        this(
                enabledDefault,
                string,
                styleStringDefault,
                Type.values()[0]
        );
    }

    /**
     * Not validated.
     */
    Trigger(
            boolean enabled,
            @NotNull String string,
            @Nullable String styleString,
            Type type
    ) {
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

    // Deserialization
    
    public static class Deserializer implements JsonDeserializer<Trigger> {
        @Override
        public @Nullable Trigger deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                             JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();

            String f = "enabled";
            boolean enabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : enabledDefault;

            f = "string";
            String string = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : stringDefault;

            f = "styleString";
            String styleString = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : styleStringDefault;

            f = "type";
            Type type = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(Type.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? Type.valueOf(obj.get(f).getAsString())
                        : Type.values()[0]
                    : Type.values()[0];
            if (version < 3) { // 2024-08-25
                f = "isKey";
                boolean isKey = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                        ? obj.get(f).getAsBoolean()
                        : false;
                boolean isRegex = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                        ? obj.get(f).getAsBoolean()
                        : false;
                type = isKey ? Type.KEY : (isRegex ? Type.REGEX : Type.NORMAL);
            }

            return new Trigger(enabled, string, styleString, type);
        }
    }
}
