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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class TextStyle {
    public final int version = 1;

    public boolean doColor;
    public int color;
    public TriState bold;
    public TriState italic;
    public TriState underlined;
    public TriState strikethrough;
    public TriState obfuscated;

    /**
     * Creates a default instance.
     */
    public TextStyle() {
        this.doColor = true;
        this.color = Config.DEFAULT_COLOR;
        this.bold = new TriState();
        this.italic = new TriState();
        this.underlined = new TriState();
        this.strikethrough = new TriState();
        this.obfuscated = new TriState();
    }

    /**
     * Creates a default instance with specified color.
     */
    TextStyle(int color) {
        this.doColor = true;
        this.color = color;
        this.bold = new TriState();
        this.italic = new TriState();
        this.underlined = new TriState();
        this.strikethrough = new TriState();
        this.obfuscated = new TriState();
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    public TextStyle(boolean doColor, int color, TriState bold, TriState italic,
                     TriState underlined, TriState strikethrough, TriState obfuscated) {
        this.doColor = doColor;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    public TextColor getTextColor() {
        return TextColor.fromRgb(color);
    }

    public boolean isEnabled() {
        return doColor ||
                bold.isEnabled() ||
                italic.isEnabled() ||
                underlined.isEnabled() ||
                strikethrough.isEnabled() ||
                obfuscated.isEnabled();
    }
    
    public Style getStyle() {
        return new Style(
                doColor ? TextColor.fromRgb(color) : null, 
                bold.isEnabled() ? bold.isOn() : null,
                italic.isEnabled() ? italic.isOn() : null,
                underlined.isEnabled() ? underlined.isOn() : null,
                strikethrough.isEnabled() ? strikethrough.isOn() : null,
                obfuscated.isEnabled() ? obfuscated.isOn() : null,
                null, 
                null, 
                null, 
                null
        );
    }

    public static class Deserializer implements JsonDeserializer<TextStyle> {
        @Override
        public @Nullable TextStyle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            boolean doColor = obj.get("doColor").getAsBoolean();
            int color = obj.get("color").getAsInt();
            TriState bold = ctx.deserialize(obj.get("bold"), TriState.class);
            TriState italic = ctx.deserialize(obj.get("italic"), TriState.class);
            TriState underlined = ctx.deserialize(obj.get("underlined"), TriState.class);
            TriState strikethrough = ctx.deserialize(obj.get("strikethrough"), TriState.class);
            TriState obfuscated = ctx.deserialize(obj.get("obfuscated"), TriState.class);

            // Validation
            if (color < 0 || color > 16777215) throw new JsonParseException("TextStyle #1");
            if (bold == null) throw new JsonParseException("TextStyle #2");
            if (italic == null) throw new JsonParseException("TextStyle #3");
            if (underlined == null) throw new JsonParseException("TextStyle #4");
            if (strikethrough == null) throw new JsonParseException("TextStyle #5");
            if (obfuscated == null) throw new JsonParseException("TextStyle #6");

            return new TextStyle(doColor, color, bold, italic, underlined, strikethrough, obfuscated);
        }
    }
}
