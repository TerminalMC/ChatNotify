/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.config;

import com.google.gson.*;
import com.notryken.chatnotify.config.util.JsonRequired;
import com.notryken.chatnotify.config.util.JsonValidator;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

public class TextStyle {
    public final int version = 1;

    @JsonRequired public boolean doColor;
    @JsonRequired public int color;
    @JsonRequired public TriState bold;
    @JsonRequired public TriState italic;
    @JsonRequired public TriState underlined;
    @JsonRequired public TriState strikethrough;
    @JsonRequired public TriState obfuscated;

    public TextStyle() {
        this.doColor = true;
        this.color = Config.DEFAULT_COLOR;
        this.bold = new TriState();
        this.italic = new TriState();
        this.underlined = new TriState();
        this.strikethrough = new TriState();
        this.obfuscated = new TriState();
    }

    public TextStyle(int color) {
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
            TriState bold = new TriState(TriState.State.valueOf(obj.getAsJsonObject("bold").get("state").getAsString()));
            TriState italic = new TriState(TriState.State.valueOf(obj.getAsJsonObject("italic").get("state").getAsString()));
            TriState underlined = new TriState(TriState.State.valueOf(obj.getAsJsonObject("underlined").get("state").getAsString()));
            TriState strikethrough = new TriState(TriState.State.valueOf(obj.getAsJsonObject("strikethrough").get("state").getAsString()));
            TriState obfuscated = new TriState(TriState.State.valueOf(obj.getAsJsonObject("obfuscated").get("state").getAsString()));

            return new JsonValidator<TextStyle>().validateNonNull(
                    new TextStyle(doColor, color, bold, italic, underlined, strikethrough, obfuscated));
        }
    }
}
