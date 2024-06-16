/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

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
     * Not validated, only for use by self-validating deserializer.
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
        return Style.create(
                Optional.ofNullable(doColor ? getTextColor() : null),
                Optional.ofNullable(bold.isEnabled() ? bold.isOn() : null),
                Optional.ofNullable(italic.isEnabled() ? italic.isOn() : null),
                Optional.ofNullable(underlined.isEnabled() ? underlined.isOn() : null),
                Optional.ofNullable(strikethrough.isEnabled() ? strikethrough.isOn() : null),
                Optional.ofNullable(obfuscated.isEnabled() ? obfuscated.isOn() : null),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
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
