/*
 * Copyright 2024 TerminalMC
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
import org.jetbrains.annotations.Nullable;

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

    public boolean enabled;
    public String string;
    public int delayTicks;
    public Type type;

    /**
     * Creates a default instance.
     */
    public ResponseMessage() {
        enabled = true;
        string = "";
        type = Type.NORMAL;
        delayTicks = 0;
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

            try {
                int version = obj.get("version").getAsInt();
                
                boolean enabled = obj.get("enabled").getAsBoolean();
                String string = obj.get("string").getAsString();
                int delayTicks = obj.get("delayTicks").getAsInt();
                Type responseType = version >= 2 
                        ? ctx.deserialize(obj.get("type"), Type.class) 
                        : obj.get("regexGroups").getAsBoolean() ? Type.REGEX : Type.NORMAL;

                // Validation
                if (delayTicks < 0) throw new JsonParseException("ResponseMessage #1");

                return new ResponseMessage(enabled, string, responseType, delayTicks);
            }
            catch (Exception e) {
                ChatNotify.LOG.warn("Unable to deserialize ResponseMessage", e);
                return null;
            }
        }
    }
}
