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

package dev.terminalmc.chatnotify.config.util;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides JSON deserialization utility methods.
 */
public class JsonUtil {

    public static final String LOG_STR =
            "Unable to deserialize key '{}' with type '{}': {}. Using default value.";
    public static boolean hasChanged = false;

    private JsonUtil() {
    }

    public static void reset() {
        hasChanged = false;
    }

    /**
     * @return the {@link Boolean} value of the key, if the value exists and has the correct type.
     */
    public static boolean getOrDefault(JsonObject obj, String key, boolean def, boolean silent) {
        JsonElement element = getAsJsonPrimitiveOrNull(obj, key, Boolean.class.getName(), silent);
        if (element == null)
            return def;

        if (!element.getAsJsonPrimitive().isBoolean()) {
            if (!silent)
                ChatNotify.LOG.error(
                        LOG_STR,
                        key,
                        Boolean.class.getName(),
                        "Value '%s' is not Boolean".formatted(element)
                );
            hasChanged = true;
            return def;
        }

        return element.getAsBoolean();
    }

    /**
     * @return the {@link Integer} value of the key, if the value exists and has the correct type.
     */
    public static int getOrDefault(JsonObject obj, String key, int def, boolean silent) {
        Number value = getAsNumberOrNull(obj, key, Integer.class.getName(), silent);
        if (value == null)
            return def;
        return value.intValue();
    }

    /**
     * @return the {@link Float} value of the key, if the value exists and has the correct type.
     */
    public static float getOrDefault(JsonObject obj, String key, float def, boolean silent) {
        Number value = getAsNumberOrNull(obj, key, Float.class.getName(), silent);
        if (value == null)
            return def;
        return value.floatValue();
    }

    /**
     * @return the {@link Long} value of the key, if the value exists and has the correct type.
     */
    public static long getOrDefault(JsonObject obj, String key, long def, boolean silent) {
        Number value = getAsNumberOrNull(obj, key, Long.class.getName(), silent);
        if (value == null)
            return def;
        return value.longValue();
    }

    /**
     * @return the {@link Double} value of the key, if the value exists and has the correct type.
     */
    public static double getOrDefault(JsonObject obj, String key, double def, boolean silent) {
        Number value = getAsNumberOrNull(obj, key, Double.class.getName(), silent);
        if (value == null)
            return def;
        return value.doubleValue();
    }

    /**
     * @return the enum constant matching the string value of the key, if the value exists and has
     * the correct type, and a matching enum constant exists.
     */
    public static <T extends Enum<T>> T getOrDefault(
            JsonObject obj,
            String key,
            Class<T> cls,
            T def,
            boolean silent
    ) {
        String value = getAsStringOrNull(obj, key, cls.getName(), silent);
        if (value == null)
            return def;

        for (T enumVal : cls.getEnumConstants()) {
            if (enumVal.name().equals(value)) {
                return enumVal;
            }
        }

        if (!silent)
            ChatNotify.LOG.error(
                    LOG_STR,
                    key,
                    cls.getName(),
                    "Value '%s' is not in Enum Constants".formatted(value)
            );
        hasChanged = true;
        return def;
    }

    /**
     * @return the {@link String} value of the key, if the value exists and has the correct type.
     */
    public static String getOrDefault(JsonObject obj, String key, String def, boolean silent) {
        String value = getAsStringOrNull(obj, key, String.class.getName(), silent);
        if (value == null)
            return def;
        return value;
    }

    /**
     * Note: requires the deserializer for {@code cls} to be registered to the deserializing
     * {@link Gson}.
     *
     * @return the {@link T} instance value of the key, if it exists and has the correct type.
     * @throws JsonParseException if a {@link T} instance could not be deserialized from the value.
     */
    public static <T> T getOrDefault(
            JsonDeserializationContext ctx,
            JsonObject obj,
            String key,
            Class<T> cls,
            T def,
            boolean silent
    ) throws JsonParseException {
        JsonElement element = getAsJsonObjectOrNull(obj, key, cls.getName(), silent);
        if (element == null)
            return def;

        return ctx.deserialize(element, cls);
    }

    /**
     * @return a {@link List} containing all the {@link String} elements of the {@link JsonArray}
     * value of the key, if it exists and has the correct type.
     */
    public static List<String> getOrDefault(
            JsonObject obj,
            String key,
            List<String> def,
            boolean silent
    ) {
        JsonArray array = getAsJsonArrayOrNull(obj, key, String.class.getName(), silent);
        if (array == null)
            return def;

        return array.asList()
                .stream()
                .filter((je) -> (je.isJsonPrimitive() && je.getAsJsonPrimitive().isString()))
                .map(JsonElement::getAsString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Note: requires the deserializer for {@code cls} to be registered to the deserializing
     * {@link Gson}.
     *
     * @return a {@link List} containing all the elements of the {@link JsonArray} value of the key
     * which could be deserialized into non-null {@link T} instances, if the value exists and has
     * the correct type.
     * @throws JsonParseException if a {@link T} instance could not be deserialized from any array
     *                            element.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getOrDefault(
            JsonDeserializationContext ctx,
            JsonObject obj,
            String key,
            Class<T> cls,
            List<T> def,
            boolean silent
    ) throws JsonParseException {
        JsonArray array = getAsJsonArrayOrNull(obj, key, cls.getName(), silent);
        if (array == null)
            return def;

        return array.asList()
                .stream()
                .filter(JsonElement::isJsonObject)
                .map((je) -> (T) ctx.deserialize(je, cls))
                .toList()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Supporting methods

    /**
     * @return the {@link JsonPrimitive} value of the key, if it exists and has the correct type.
     */
    private static @Nullable JsonPrimitive getAsJsonPrimitiveOrNull(
            JsonObject obj,
            String key,
            String clsName,
            boolean silent
    ) {
        if (!obj.has(key)) {
            if (!silent)
                ChatNotify.LOG.error(LOG_STR, key, clsName, "Key not found");
            hasChanged = true;
            return null;
        }

        JsonElement element = obj.get(key);
        if (!element.isJsonPrimitive()) {
            if (!silent)
                ChatNotify.LOG.error(
                        LOG_STR,
                        key,
                        clsName,
                        "Value '%s' is not JsonPrimitive".formatted(element)
                );
            hasChanged = true;
            return null;
        }

        return element.getAsJsonPrimitive();
    }

    /**
     * @return the {@link JsonObject} value of the key, if it exists and has the correct type.
     */
    private static @Nullable JsonObject getAsJsonObjectOrNull(
            JsonObject obj,
            String key,
            String clsName,
            boolean silent
    ) {
        if (!obj.has(key)) {
            if (!silent)
                ChatNotify.LOG.error(LOG_STR, key, clsName, "Key not found");
            hasChanged = true;
            return null;
        }

        JsonElement element = obj.get(key);
        if (!element.isJsonObject()) {
            if (!silent)
                ChatNotify.LOG.error(
                        LOG_STR,
                        key,
                        clsName,
                        "Value '%s' is not JsonObject".formatted(element)
                );
            hasChanged = true;
            return null;
        }

        return element.getAsJsonObject();
    }

    /**
     * @return the {@link JsonArray} value of the key, if it exists and has the correct type.
     */
    private static @Nullable JsonArray getAsJsonArrayOrNull(
            JsonObject obj,
            String key,
            String clsName,
            boolean silent
    ) {
        if (!obj.has(key)) {
            if (!silent)
                ChatNotify.LOG.error(LOG_STR, key, clsName, "Key not found");
            hasChanged = true;
            return null;
        }

        JsonElement element = obj.get(key);
        if (!element.isJsonArray()) {
            if (!silent)
                ChatNotify.LOG.error(
                        LOG_STR,
                        key,
                        clsName,
                        "Value '%s' is not JsonArray".formatted(element)
                );
            hasChanged = true;
            return null;
        }

        return element.getAsJsonArray();
    }

    /**
     * @return the {@link Number} value of the key, if it exists and has the correct type.
     */
    private static @Nullable Number getAsNumberOrNull(
            JsonObject obj,
            String key,
            String clsName,
            boolean silent
    ) {
        JsonElement element = getAsJsonPrimitiveOrNull(obj, key, clsName, silent);
        if (element == null)
            return null;

        if (!element.getAsJsonPrimitive().isNumber()) {
            if (!silent)
                ChatNotify.LOG.error(
                        LOG_STR,
                        key,
                        clsName,
                        "Value '%s' is not Number".formatted(element)
                );
            hasChanged = true;
            return null;
        }

        return element.getAsNumber();
    }

    /**
     * @return the {@link String} value of the key, if it exists and has the correct type.
     */
    private static @Nullable String getAsStringOrNull(
            JsonObject obj,
            String key,
            String clsName,
            boolean silent
    ) {
        JsonElement element = getAsJsonPrimitiveOrNull(obj, key, clsName, silent);
        if (element == null)
            return null;

        if (!element.getAsJsonPrimitive().isString()) {
            if (!silent)
                ChatNotify.LOG.error(
                        LOG_STR,
                        key,
                        clsName,
                        "Value '%s' is not String".formatted(element)
                );
            hasChanged = true;
            return null;
        }

        return element.getAsString();
    }
}
