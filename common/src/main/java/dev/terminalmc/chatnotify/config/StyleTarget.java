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

public class StyleTarget {
    public final int version = 1;

    /**
     * Whether this instance is eligible for editing or usage.
     */
    public boolean enabled;
    public static final boolean enabledDefault = false;

    /**
     * The target string.
     */
    public @NotNull String string;
    public static final @NotNull String stringDefault = "";

    /**
     * A regex {@link Pattern} compiled from {@link Trigger#string}, or 
     * {@code null} if type is not {@link Type#REGEX} or the string could not 
     * be compiled.
     */
    public transient @Nullable Pattern pattern;

    /**
     * Controls how {@link Trigger#string} is interpreted.
     */
    public Type type;
    public enum Type {
        /**
         * Case-ignorant substring matching.
         */
        NORMAL("~"),
        /**
         * Regex matching.
         */
        REGEX(".*");

        public final String icon;

        Type(String icon) {
            this.icon = icon;
        }
    }

    /**
     * Creates a default instance.
     */
    public StyleTarget() {
        this(
                enabledDefault,
                stringDefault,
                Type.values()[0]
        );
    }

    /**
     * Creates a default instance, enabled, with the specified value.
     */
    public StyleTarget(@NotNull String string) {
        this(
                true,
                string,
                Type.values()[0]
        );
    }

    /**
     * Not validated.
     */
    StyleTarget(
            boolean enabled,
            @NotNull String string,
            Type type
    ) {
        this.enabled = enabled;
        this.string = string;
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

    public static class Deserializer implements JsonDeserializer<StyleTarget> {
        @Override
        public @Nullable StyleTarget deserialize(JsonElement json, java.lang.reflect.Type typeOfT, 
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

            f = "type";
            Type type = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(Type.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                    ? Type.valueOf(obj.get(f).getAsString())
                    : Type.values()[0]
                    : Type.values()[0];

            return new StyleTarget(enabled, string, type);
        }
    }
}
