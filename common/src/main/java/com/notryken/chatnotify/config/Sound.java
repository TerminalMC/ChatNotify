/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.config;

import com.google.gson.*;
import com.notryken.chatnotify.config.util.JsonRequired;
import com.notryken.chatnotify.config.util.JsonValidator;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class Sound {
    public final int version = 1;
    public static final String DEFAULT_SOUND_ID = "block.note_block.bell";

    @JsonRequired private boolean enabled;
    @JsonRequired private String id;
    @JsonRequired private float volume;
    @JsonRequired private float pitch;

    public Sound() {
        this.enabled = true;
        this.id = DEFAULT_SOUND_ID;
        this.volume = 1f;
        this.pitch = 1f;
    }

    public Sound(boolean enabled, String id, float volume, float pitch) {
        if (!validId(id)) throw new IllegalArgumentException("Specified id is not a valid sound.");
        this.enabled = enabled;
        this.id = id;
        setVolume(volume);
        setPitch(pitch);
    }

    public Sound(Sound pSound) {
        this.enabled = pSound.enabled;
        this.id = pSound.id;
        this.volume = pSound.volume;
        this.pitch = pSound.pitch;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && volume == 0) {
            volume = 1f;
        }
    }

    public String getId() {
        return id;
    }

    public boolean setId(String id) {
        if (validId(id)) {
            this.id = id;
            return true;
        }
        return false;
    }

    public static boolean validId(String id) {
        return ResourceLocation.tryParse(id) != null;
    }

    public ResourceLocation getResourceLocation() {
        return ResourceLocation.tryParse(id);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0 || volume > 2) throw new IllegalArgumentException(
                "Value out of range. Expected 0-2, got " + volume);
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        if (pitch < 0.5 || pitch > 2) throw new IllegalArgumentException(
                "Value out of range. Expected 0.5-2, got " + pitch);
        this.pitch = pitch;
    }

    public static class Deserializer implements JsonDeserializer<Sound> {
        @Override
        public Sound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            boolean enabled = obj.get("enabled").getAsBoolean();
            String id = obj.get("id").getAsString();
            if (!validId(id)) id = DEFAULT_SOUND_ID;
            float volume = obj.get("volume").getAsFloat();
            float pitch = obj.get("pitch").getAsFloat();

            return new JsonValidator<Sound>().validateNonNull(
                    new Sound(enabled, id, volume, pitch));
        }
    }
}
