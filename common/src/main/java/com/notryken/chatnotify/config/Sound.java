package com.notryken.chatnotify.config;

import net.minecraft.resources.ResourceLocation;

public class Sound {
    public static final String DEFAULT_SOUND_ID = "block.note_block.bell";

    private boolean enabled;
    private String id;
    private float volume;
    private float pitch;

    public Sound() {
        this.enabled = true;
        this.id = DEFAULT_SOUND_ID;
        this.volume = 1f;
        this.pitch = 1f;
    }

    public Sound(String id) {
        if (!validId(id)) throw new IllegalArgumentException("Specified id is not a valid sound.");
        this.enabled = true;
        this.id = id;
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
}
