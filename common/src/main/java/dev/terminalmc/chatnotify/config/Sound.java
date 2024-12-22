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
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class Sound {
    public final int version = 1;
    
    // Options

    private static final boolean enabledDefault = true;
    private boolean enabled;
    
    private static final String idDefault = "block.note_block.bell";
    private String id;
    
    private static final float volumeDefault = 1.0F;
    private float volume;
    
    private static final float pitchDefault = 1.0F;
    private float pitch;

    /**
     * Creates a default instance.
     */
    public Sound() {
        this.enabled = enabledDefault;
        this.id = idDefault;
        this.volume = volumeDefault;
        this.pitch = pitchDefault;
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    Sound(boolean enabled, String id, float volume, float pitch) {
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
        if (volume < 0 || volume > 1) throw new IllegalArgumentException(
                "Value out of range. Expected 0-1, got " + volume);
        this.volume = volume;
        if (volume == 0) this.enabled = false;
        else if (!this.enabled) this.enabled = true;
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
            int version = obj.get("version").getAsInt();

            String f = "enabled";
            boolean enabled = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean()
                    ? obj.get(f).getAsBoolean()
                    : enabledDefault;

            f = "id";
            String id = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? obj.get(f).getAsString()
                    : idDefault;
            if (!validId(id)) id = idDefault;

            f = "volume";
            float volume = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isNumber()
                    ? obj.get(f).getAsNumber().floatValue()
                    : volumeDefault;
            if (volume < 0 || volume > 1) volume = volumeDefault;

            f = "pitch";
            float pitch = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isNumber()
                    ? obj.get(f).getAsNumber().floatValue()
                    : pitchDefault;
            if (pitch < 0.5 || pitch > 2) pitch = volumeDefault;

            return new Sound(enabled, id, volume, pitch);
        }
    }
}
