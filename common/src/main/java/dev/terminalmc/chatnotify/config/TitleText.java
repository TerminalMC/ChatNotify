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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class TitleText {
    public final int version = 1;

    public boolean enabled;
    public int color;
    public @NotNull String text;

    /**
     * Creates a default instance.
     */
    public TitleText() {
        this.enabled = false;
        this.color = 16777215;
        this.text = "";
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    TitleText(boolean enabled, int color, @NotNull String text) {
        this.enabled = enabled;
        this.color = color;
        this.text = text;
    }

    public boolean isEnabled() {
        return enabled && !text.isBlank();
    }

    public static class Deserializer implements JsonDeserializer<TitleText> {
        @Override
        public @Nullable TitleText deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            boolean enabled = obj.get("enabled").getAsBoolean();
            int color = obj.get("color").getAsInt();
            String text = obj.get("text").getAsString();

            // Validation
            if (color < 0 || color > 16777215) throw new JsonParseException("TitleText #1");

            return new TitleText(enabled, color, text);
        }
    }
}
