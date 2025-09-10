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
import dev.terminalmc.chatnotify.config.util.JsonUtil;
import dev.terminalmc.chatnotify.util.functional.StringSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A response to be sent when a {@link Notification} is triggered.
 */
public class Response implements StringSupplier {

    public static final int VERSION = 2;
    public final int version = VERSION;

    /**
     * The active cooldown value.
     */
    public transient int cooldown;

    /**
     * The processed version of {@link Response#string}
     */
    public transient @Nullable String sendingString;

    // Options

    /**
     * Not currently used.
     */
    public boolean enabled;
    public static final boolean enabledDefault = true;

    /**
     * The string to process when triggered.
     */
    public String string;
    public static final String stringDefault = "";

    /**
     * The time in ticks to wait before activating.
     */
    public int delayTicks;
    public static final int delayTicksDefault = 0;

    /**
     * The minimum time in ticks between consecutive sends.
     */
    public int cooldownTicks;
    public static final int cooldownTicksDefault = 0;

    /**
     * Controls how {@link Response#string} is processed.
     */
    public Type type;

    public enum Type {
        /**
         * No additional processing.
         */
        NORMAL("~"),
        /**
         * Replace regex group indicators with groups from the activating {@link Trigger}.
         */
        REGEX(".*"),
        /**
         * Convert into a pair of keys for use by the CommandKeys mod.
         */
        COMMANDKEYS("K");

        public final String icon;

        Type(String icon) {
            this.icon = icon;
        }
    }

    /**
     * Creates a default instance.
     */
    public Response() {
        enabled = enabledDefault;
        string = stringDefault;
        delayTicks = delayTicksDefault;
        type = Type.values()[0];
    }

    /**
     * Not validated.
     */
    Response(boolean enabled, String string, Type type, int delayTicks, int cooldownTicks) {
        this.enabled = enabled;
        this.string = string;
        this.type = type;
        this.delayTicks = delayTicks;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public @NotNull String getString() {
        return string;
    }

    // Validation

    /**
     * Validates this instance. Called after deserialization and before saving.
     */
    Response validate() {
        if (delayTicks < 0)
            delayTicks = delayTicksDefault;
        return this;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Response> {

        @Override
        public Response deserialize(
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

            int delayTicks = JsonUtil.getOrDefault(
                    obj,
                    "delayTicks",
                    delayTicksDefault,
                    silent
            );

            int cooldownTicks = JsonUtil.getOrDefault(
                    obj,
                    "cooldownTicks",
                    cooldownTicksDefault,
                    silent
            );

            Type type = JsonUtil.getOrDefault(
                    obj,
                    "type",
                    Type.class,
                    Type.values()[0],
                    silent
            );

            return new Response(enabled, string, type, delayTicks, cooldownTicks).validate();
        }
    }
}
