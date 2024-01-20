package notryken.chatnotify.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import notryken.chatnotify.config.Config;

public class SoundUtil {
    /**
     * Attempts to match the specified {@code String} to a Minecraft sound.
     * Accepts format "namespace:category.source.sound"
     * (such as "minecraft:block.anvil.land").
     * <p>
     * <b>Note:</b> if the "namespace:" is omitted, will default to "minecraft:"
     * @param soundName the {@code String} representing the sound.
     * @return The sound {@code ResourceLocation}, or a default
     * {@code ResourceLocation} if the {@code String} cannot be parsed.
     */
    public static ResourceLocation parseSound(String soundName) {
        ResourceLocation sound = ResourceLocation.tryParse(soundName);
        if (sound == null) {
            sound = Config.DEFAULT_SOUND;
        }
        return sound;
    }

    /**
     * Determines whether the specified {@code ResourceLocation} represents a
     * playable sound.
     * @param sound the sound {@code ResourceLocation}.
     * @return true if the check cannot be run in the current state or if the
     * ResourceLocation is valid, false otherwise.
     */
    public static boolean validSound(ResourceLocation sound) {
        /*
        Uses Minecraft's internal approach to sound validation, for lack of
        a better idea.
         */
        Minecraft client = Minecraft.getInstance();
        boolean valid = true;
        if (client.player != null) {
            if (new SimpleSoundInstance(sound, SoundSource.PLAYERS,
                    1f, 1f, SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true)
                    .resolve(client.getSoundManager()) == null) {
                valid = false;
            }
        }
        return valid;
    }
}
