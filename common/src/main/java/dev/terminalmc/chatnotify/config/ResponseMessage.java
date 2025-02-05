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
import dev.terminalmc.chatnotify.util.JsonUtil;
import org.jetbrains.annotations.Nullable;

public class ResponseMessage {
    public static final int VERSION = 2;
    public final int version = VERSION;

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

    // Validation

    ResponseMessage validate() {
        if (delayTicks < 0) delayTicks = delayTicksDefault;
        return this;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<ResponseMessage> {
        @Override
        public @Nullable ResponseMessage deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                                     JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            boolean enabled = JsonUtil.getOrDefault(obj, "enabled",
                    enabledDefault, silent);

            String string = JsonUtil.getOrDefault(obj, "string",
                    stringDefault, silent);

            int delayTicks = JsonUtil.getOrDefault(obj, "delayTicks",
                    delayTicksDefault, silent);

            Type type = JsonUtil.getOrDefault(obj, "type",
                    Type.class, Type.values()[0], silent);

            return new ResponseMessage(
                    enabled,
                    string,
                    type,
                    delayTicks
            ).validate();
        }
    }
}
