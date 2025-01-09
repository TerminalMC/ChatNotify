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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;

public class TextStyle {
    public final int version = 2;

    public enum FormatMode {
        DISABLED,
        ON,
        OFF,
    }

    public static final boolean doColorDefault = true;
    public boolean doColor;
    
    public static final int colorDefault = 0xffc400;
    public int color;
    
    public FormatMode bold;
    public FormatMode italic;
    public FormatMode underlined;
    public FormatMode strikethrough;
    public FormatMode obfuscated;

    /**
     * Creates a default instance.
     */
    public TextStyle() {
        this(colorDefault);
    }

    /**
     * Creates a default instance with specified color.
     */
    TextStyle(int color) {
        this(
                doColorDefault,
                color,
                FormatMode.values()[0],
                FormatMode.values()[0],
                FormatMode.values()[0],
                FormatMode.values()[0],
                FormatMode.values()[0]
        );
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    public TextStyle(boolean doColor, int color, FormatMode bold, FormatMode italic,
                     FormatMode underlined, FormatMode strikethrough, FormatMode obfuscated) {
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
                bold != FormatMode.DISABLED ||
                italic != FormatMode.DISABLED ||
                underlined != FormatMode.DISABLED ||
                strikethrough != FormatMode.DISABLED ||
                obfuscated != FormatMode.DISABLED;
    }
    
    public Style getStyle() {
        return new Style(
                doColor ? TextColor.fromRgb(color) : null, 
                bold != FormatMode.DISABLED ? bold == FormatMode.ON : null,
                italic != FormatMode.DISABLED ? italic == FormatMode.ON : null,
                underlined != FormatMode.DISABLED ? underlined == FormatMode.ON : null,
                strikethrough != FormatMode.DISABLED ? strikethrough == FormatMode.ON : null,
                obfuscated != FormatMode.DISABLED ? obfuscated == FormatMode.ON : null,
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
            int version = obj.get("version").getAsInt();

            String f = "doColor";
            boolean doColor = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : doColorDefault;

            f = "color";
            int color = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isNumber()
                    ? obj.get(f).getAsNumber().intValue()
                    : colorDefault;
            if (color < 0 || color > 16777215) color = colorDefault;

            f = "bold";
            FormatMode bold = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(FormatMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? FormatMode.valueOf(obj.get(f).getAsString())
                        : FormatMode.values()[0]
                    : FormatMode.values()[0];

            f = "italic";
            FormatMode italic = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(FormatMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? FormatMode.valueOf(obj.get(f).getAsString())
                        : FormatMode.values()[0]
                    : FormatMode.values()[0];

            f = "underlined";
            FormatMode underlined = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(FormatMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? FormatMode.valueOf(obj.get(f).getAsString())
                        : FormatMode.values()[0]
                    : FormatMode.values()[0];

            f = "strikethrough";
            FormatMode strikethrough = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(FormatMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? FormatMode.valueOf(obj.get(f).getAsString())
                        : FormatMode.values()[0]
                    : FormatMode.values()[0];

            f = "obfuscated";
            FormatMode obfuscated = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(FormatMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? FormatMode.valueOf(obj.get(f).getAsString())
                        : FormatMode.values()[0]
                    : FormatMode.values()[0];
            
            return new TextStyle(doColor, color, bold, italic, underlined, strikethrough, obfuscated);
        }
    }
}
