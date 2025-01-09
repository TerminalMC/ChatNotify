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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ResponseMessage {
    public final int version = 2;

    public transient int countdown;
    public transient String sendingString;
    
    // Options

    /**
     * Not currently used.
     */
    public boolean enabled;
    public static final boolean enabledDefault = true;

    /**
     * The string to process when activated.
     */
    public String string;
    public static final String stringDefault = "";

    /**
     * The time in ticks to wait before activating.
     */
    public int delayTicks;
    public static final int delayTicksDefault = 0;

    /**
     * Controls how {@link ResponseMessage#string} is processed.
     */
    public Type type;
    public enum Type {
        /**
         * No additional processing.
         */
        NORMAL("~"),
        /**
         * Replace regex group indicators with groups from the trigger.
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
    public ResponseMessage() {
        enabled = enabledDefault;
        string = stringDefault;
        delayTicks = delayTicksDefault;
        type = Type.values()[0];
    }

    /**
     * Not validated.
     */
    ResponseMessage(
            boolean enabled,
            String string,
            Type type,
            int delayTicks
    ) {
        this.enabled = enabled;
        this.string = string;
        this.type = type;
        this.delayTicks = delayTicks;
    }

    // Deserialization
    
    public static class Deserializer implements JsonDeserializer<ResponseMessage> {
        @Override
        public @Nullable ResponseMessage deserialize(JsonElement json, java.lang.reflect.Type typeOfT, 
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

            f = "delayTicks";
            int delayTicks = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isNumber()
                    ? obj.get(f).getAsNumber().intValue()
                    : delayTicksDefault;
            if (delayTicks < 0) delayTicks = delayTicksDefault;

            f = "type";
            Type type = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(Type.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? Type.valueOf(obj.get(f).getAsString())
                        : Type.values()[0]
                    : Type.values()[0];
            if (version < 2) { // 2024-11-24
                f = "regexGroups";
                boolean regexGroups = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                        ? obj.get(f).getAsBoolean()
                        : false;
                type = regexGroups ? Type.REGEX : Type.NORMAL;
            }

            return new ResponseMessage(enabled, string, type, delayTicks);
        }
    }
}
