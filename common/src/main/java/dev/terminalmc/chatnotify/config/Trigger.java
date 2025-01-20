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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger {
    public final int version = 4;

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
     * {@code null} if {@link Trigger#type} is not {@link Type#REGEX} or the 
     * string could not be compiled.
     */
    public transient @Nullable Pattern pattern;

    /**
     * The restyle target.
     */
    public StyleTarget styleTarget;
    public static final Supplier<StyleTarget> styleTargetDefault = StyleTarget::new;

    /**
     * Controls how {@link Trigger#string} is interpreted.
     */
    public Type type;
    public enum Type {
        /**
         * Case-ignorant word-break matching.
         */
        NORMAL("~"),
        /**
         * Regex matching.
         */
        REGEX(".*"),
        /**
         * Translation key substring matching.
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
                styleTargetDefault.get(),
                Type.values()[0]
        );
    }

    /**
     * Not validated.
     */
    Trigger(
            boolean enabled,
            @NotNull String string,
            StyleTarget styleTarget,
            Type type
    ) {
        this.enabled = enabled;
        this.string = string;
        this.styleTarget = styleTarget;
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

            StyleTarget styleTarget;
            if (version < 4) { // 2025-01-19
                f = "styleString";
                String styleString = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                        ? obj.get(f).getAsString()
                        : null;
                styleTarget = new StyleTarget(styleString == null ? StyleTarget.stringDefault : styleString);
            } else {
                f = "styleTarget";
                styleTarget = obj.has(f) && obj.get(f).isJsonObject()
                        ? ctx.deserialize(obj.get(f), StyleTarget.class)
                        : styleTargetDefault.get();
            }

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

            return new Trigger(enabled, string, styleTarget, type);
        }
    }
}
