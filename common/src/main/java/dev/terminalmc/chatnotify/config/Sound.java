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
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class Sound {
    public static final int VERSION = 1;
    public final int version = VERSION;

    /**
     * Whether this instance is eligible for activation.
     */
    private boolean enabled;
    private static final boolean enabledDefault = true;

    /**
     * The string from which to get the sound {@link ResourceLocation}.
     */
    private String id;
    private static final String idDefault = "block.note_block.bell";

    /**
     * The sound volume, from {@code 0} to {@code 1} inclusive.
     */
    private float volume;
    private static final float volumeDefault = 1.0F;

    /**
     * The sound pitch, from {@code 0.5} to {@code 2} inclusive.
     */
    private float pitch;
    private static final float pitchDefault = 1.0F;

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
     * Not validated.
     */
    Sound(
            boolean enabled,
            String id,
            float volume,
            float pitch
    ) {
        if (!validId(id)) throw new IllegalArgumentException("Specified id is not a valid sound.");
        this.enabled = enabled;
        this.id = id;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Copy constructor.
     */
    public Sound(Sound pSound) {
        this.enabled = pSound.enabled;
        this.id = pSound.id;
        this.volume = pSound.volume;
        this.pitch = pSound.pitch;
    }

    /**
     * @return {@code true} if this instance is eligible for activation, 
     * {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables this instance.
     *
     * <p>{@link Sound#volume} is 0 and {@code enabled} is true, sets 
     * {@link Sound#volume} to 1.</p>
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && volume == 0) {
            volume = 1.0F;
        }
    }

    /**
     * @return the sound {@link ResourceLocation} string.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the sound {@link ResourceLocation} string to the specified value
     * if it represents a valid {@link ResourceLocation}.
     * @param id the {@link ResourceLocation} string.
     * @return {@code true} if {@code id} represents a valid 
     * {@link ResourceLocation}, {@code false} otherwise.
     */
    public boolean setId(String id) {
        if (validId(id)) {
            this.id = id;
            return true;
        }
        return false;
    }

    /**
     * @return the sound {@link ResourceLocation}.
     */
    public @Nullable ResourceLocation getResourceLocation() {
        return ResourceLocation.tryParse(id);
    }

    /**
     * @return the sound volume, from {@code 0} to {@code 1} inclusive.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Sets the sound volume.
     *
     * <p>If {@code volume} is {@code 0}, sets {@link Sound#enabled} to 
     * {@code false}.</p>
     *
     * <p>If {@code volume} is not {@code 0} and {@link Sound#enabled} is 
     * {@code false}, sets {@link Sound#enabled} to {@code true}.</p>
     *
     * @throws IllegalArgumentException if {@code volume} is less than {@code 0}
     * or greater than {@code 1}.
     */
    public void setVolume(float volume) {
        if (volume < 0 || volume > 1) throw new IllegalArgumentException(
                "Value out of range. Expected 0-1, got " + volume);
        this.volume = volume;
        if (volume == 0) this.enabled = false;
        else if (!this.enabled) this.enabled = true;
    }

    /**
     * @return the sound pitch, from {@code 0.5} to {@code 2} inclusive.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Sets the sound pitch.
     * @throws IllegalArgumentException if {@code pitch} is less than 
     * {@code 0.5} or greater than {@code 2}.
     */
    public void setPitch(float pitch) {
        if (pitch < 0.5 || pitch > 2) throw new IllegalArgumentException(
                "Value out of range. Expected 0.5-2, got " + pitch);
        this.pitch = pitch;
    }

    // Validation

    Sound validate() {
        if (volume < 0 || volume > 1) volume = volumeDefault;
        if (pitch < 0.5 || pitch > 2) pitch = volumeDefault;
        return this;
    }

    /**
     * @return {@code true} if {@code id} represents a valid
     * {@link ResourceLocation}, {@code false} otherwise.
     */
    public static boolean validId(String id) {
        return ResourceLocation.tryParse(id) != null;
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Sound> {
        @Override
        public Sound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            boolean enabled = JsonUtil.getOrDefault(obj, "enabled",
                    enabledDefault, silent);

            String id = JsonUtil.getOrDefault(obj, "id",
                    idDefault, silent);

            float volume = JsonUtil.getOrDefault(obj, "volume",
                    volumeDefault, silent);

            float pitch = JsonUtil.getOrDefault(obj, "volume",
                    volumeDefault, silent);

            return new Sound(
                    enabled,
                    id,
                    volume,
                    pitch
            ).validate();
        }
    }
}
