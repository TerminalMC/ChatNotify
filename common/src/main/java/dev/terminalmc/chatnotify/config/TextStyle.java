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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class TextStyle {
    public static final int VERSION = 2;
    public final int version = VERSION;

    /**
     * Whether color should be used when applying style.
     */
    public boolean doColor;
    public static final boolean doColorDefault = true;

    /**
     * The text color, from {@link 0x000000} to {@link 0xffffff} inclusive.
     */
    public int color;
    public static final int colorDefault = 0xffc400;

    // Format controls

    public FormatMode bold;
    public FormatMode italic;
    public FormatMode underlined;
    public FormatMode strikethrough;
    public FormatMode obfuscated;

    public enum FormatMode {
        DISABLED,
        ON,
        OFF,
    }

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
     * Not validated.
     */
    public TextStyle(
            boolean doColor,
            int color,
            FormatMode bold,
            FormatMode italic,
            FormatMode underlined,
            FormatMode strikethrough,
            FormatMode obfuscated
    ) {
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

    // Validation

    TextStyle validate() {
        if (color < 0 || color > 16777215) color = colorDefault;
        return this;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<TextStyle> {
        @Override
        public @Nullable TextStyle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            boolean doColor = JsonUtil.getOrDefault(obj, "doColor",
                    doColorDefault, silent);

            int color = JsonUtil.getOrDefault(obj, "color",
                    colorDefault, silent);

            FormatMode bold = JsonUtil.getOrDefault(obj, "bold",
                    FormatMode.class, FormatMode.values()[0], silent);

            FormatMode italic = JsonUtil.getOrDefault(obj, "italic",
                    FormatMode.class, FormatMode.values()[0], silent);

            FormatMode underlined = JsonUtil.getOrDefault(obj, "underlined",
                    FormatMode.class, FormatMode.values()[0], silent);

            FormatMode strikethrough = JsonUtil.getOrDefault(obj, "strikethrough",
                    FormatMode.class, FormatMode.values()[0], silent);

            FormatMode obfuscated = JsonUtil.getOrDefault(obj, "obfuscated",
                    FormatMode.class, FormatMode.values()[0], silent);

            return new TextStyle(
                    doColor,
                    color,
                    bold,
                    italic,
                    underlined,
                    strikethrough,
                    obfuscated
            ).validate();
        }
    }
}
