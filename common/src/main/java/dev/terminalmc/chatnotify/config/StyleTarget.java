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
import dev.terminalmc.chatnotify.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StyleTarget {
    public static final int VERSION = 2;
    public final int version = VERSION;

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
     * {@code null} if {@link StyleTarget#type} is not {@link Type#REGEX} or 
     * the string could not be compiled.
     */
    public transient @Nullable Pattern pattern;

    /**
     * A list of integers parsed from {@link StyleTarget#string}, or an empty
     * list if {@link StyleTarget#type} is not {@link Type#CAPTURING} or the
     * string could not be parsed.
     */
    public transient final List<Integer> groupIndexes = new ArrayList<>();

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
        REGEX(".*"),
        /**
         * Regex capturing group indexing.
         */
        CAPTURING("()");

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
            ChatNotify.LOG.warn("Error processing regex: " + e);
            pattern = null;
        }
    }

    public void tryParseIndexes() {
        groupIndexes.clear();
        String[] split = string.split(",");
        for (String str : split) {
            try {
                groupIndexes.add(Integer.parseInt(str));
            } catch (NumberFormatException e) {
                ChatNotify.LOG.warn("Error processing style target: " + e);
                pattern = null;
            }
        }
    }

    // Validation

    StyleTarget validate() {
        return this;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<StyleTarget> {
        @Override
        public @Nullable StyleTarget deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                                 JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            boolean enabled = JsonUtil.getOrDefault(obj, "enabled",
                    enabledDefault, silent);

            String string = JsonUtil.getOrDefault(obj, "string",
                    stringDefault, silent);

            Type type = JsonUtil.getOrDefault(obj, "type",
                    Type.class, Type.values()[0], silent);

            return new StyleTarget(
                    enabled,
                    string,
                    type
            ).validate();
        }
    }
}
