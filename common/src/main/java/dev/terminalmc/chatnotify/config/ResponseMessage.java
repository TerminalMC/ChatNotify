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

    public enum Type {
        NORMAL("~"),
        REGEX(".*"),
        COMMANDKEYS("K");

        public final String icon;

        Type(String icon) {
            this.icon = icon;
        }
    }

    public transient int countdown;
    public transient String sendingString;
    
    // Options

    public static final boolean enabledDefault = true;
    public boolean enabled;
    
    public static final String stringDefault = "";
    public String string;
    
    public static final int delayTicksDefault = 0;
    public int delayTicks;
    
    public Type type;

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
     * Not validated, only for use by self-validating deserializer.
     */
    ResponseMessage(boolean enabled, String string, Type type, int delayTicks) {
        this.enabled = enabled;
        this.string = string;
        this.type = type;
        this.delayTicks = delayTicks;
    }
    
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
