/*
 * Copyright 2026 TerminalMC
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
import dev.terminalmc.chatnotify.config.util.JsonUtil;
import dev.terminalmc.chatnotify.util.Unicode;
import dev.terminalmc.chatnotify.util.functional.StringSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger implements StringSupplier {

    public static final int VERSION = 4;
    public final int version = VERSION;

    /**
     * A regex {@link Pattern} compiled from {@link Trigger#string}, or {@code null} if
     * {@link Trigger#type} is not {@link Type#REGEX} or the string could not be compiled.
     */
    public transient @Nullable Pattern pattern;

    // Options

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
     * The restyle target.
     */
    public final StyleTarget styleTarget;
    public static final Supplier<StyleTarget> styleTargetDefault = StyleTarget::new;

    /**
     * Controls how {@link Trigger#string} is interpreted.
     */
    public Type type;

    public enum Type {
        /**
         * Case-ignorant word-boundary matching.
         */
        NORMAL("~"),
        /**
         * Regex matching.
         */
        REGEX(".*"),
        /**
         * Translation key substring matching.
         */
        KEY(Unicode.KEY.str);

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
        this(enabledDefault, string, styleTargetDefault.get(), Type.values()[0]);
    }

    /**
     * Not validated.
     */
    Trigger(boolean enabled, @NotNull String string, StyleTarget styleTarget, Type type) {
        this.enabled = enabled;
        this.string = string;
        this.styleTarget = styleTarget;
        this.type = type;
    }

    @Override
    public @NotNull String getString() {
        return string;
    }

    public void tryCompilePattern() {
        try {
            pattern = Pattern.compile(string);
        } catch (PatternSyntaxException e) {
            ChatNotify.LOG.warn("ChatNotify: Error processing regex: " + e);
            pattern = null;
        }
    }

    // Validation

    /**
     * Validates this instance. Called after deserialization and before saving.
     */
    Trigger validate() {
        if (type == Type.KEY)
            string = string.toLowerCase(Locale.ROOT);
        styleTarget.validate();
        if (styleTarget.string.isBlank())
            styleTarget.enabled = false;
        return this;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Trigger> {

        @Override
        public Trigger deserialize(
                JsonElement json,
                java.lang.reflect.Type typeOfT,
                JsonDeserializationContext ctx
        ) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            boolean enabled = JsonUtil.getOrDefault(
                    obj,
                    "enabled",
                    enabledDefault,
                    silent
            );

            String string = JsonUtil.getOrDefault(
                    obj,
                    "string",
                    stringDefault,
                    silent
            );

            StyleTarget styleTarget;
            if (version < 4) { // 2025-01-19
                String styleString = JsonUtil.getOrDefault(
                        obj,
                        "styleString",
                        stringDefault,
                        true
                );
                styleTarget = new StyleTarget(styleString);
            } else {
                styleTarget = JsonUtil.getOrDefault(
                        ctx,
                        obj,
                        "styleTarget",
                        StyleTarget.class,
                        styleTargetDefault.get(),
                        silent
                );
            }

            Type type = JsonUtil.getOrDefault(
                    obj,
                    "type",
                    Type.class,
                    Type.values()[0],
                    silent
            );
            if (version < 3) { // 2024-08-25
                boolean isKey = JsonUtil.getOrDefault(
                        obj,
                        "isKey",
                        false,
                        true
                );
                boolean isRegex = JsonUtil.getOrDefault(
                        obj,
                        "isRegex",
                        false,
                        true
                );
                type = isKey ? Type.KEY : (isRegex ? Type.REGEX : Type.NORMAL);
            }

            return new Trigger(enabled, string, styleTarget, type).validate();
        }
    }
}
