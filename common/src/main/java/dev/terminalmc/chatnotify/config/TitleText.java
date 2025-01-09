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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class TitleText {
    public final int version = 1;

    /**
     * Whether this instance is eligible for use.
     */
    public boolean enabled;
    public static final boolean enabledDefault = true;

    /**
     * The text color, from {@link 0x000000} to {@link 0xffffff} inclusive.
     */
    public int color;
    public static final int colorDefault = 0xffffff;

    /**
     * The string to display.
     */
    public @NotNull String text;
    public static final @NotNull String textDefault = "";

    /**
     * Creates a default instance.
     */
    public TitleText() {
        this.enabled = false;
        this.color = 16777215;
        this.text = "";
    }

    /**
     * Not validated.
     */
    TitleText(
            boolean enabled,
            int color,
            @NotNull String text
    ) {
        this.enabled = enabled;
        this.color = color;
        this.text = text;
    }

    /**
     * @return {@code true} if this instance is enabled and has text to display,
     * {@code false} otherwise.
     */
    public boolean canDisplay() {
        return enabled && !text.isBlank();
    }

    // Deserialization
    
    public static class Deserializer implements JsonDeserializer<TitleText> {
        @Override
        public @Nullable TitleText deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();

            String f = "enabled";
            boolean enabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : enabledDefault;

            f = "color";
            int color = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isNumber()
                    ? obj.get(f).getAsNumber().intValue()
                    : colorDefault;
            if (color < 0 || color > 16777215) color = colorDefault;

            f = "text";
            String text = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : textDefault;

            return new TitleText(enabled, color, text);
        }
    }
}
