package notryken.chatnotify.config;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import notryken.chatnotify.client.ChatNotifyClient;

import java.util.Objects;

/**
 * Configurable notification including text color, text formatting
 * and sound parameters for a specified trigger word.
 */
public class Notification
{
    public static final Identifier DEFAULTSOUND =
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.getId();
    public boolean keep; // Tracks whether the Notification has been modified.
    private String trigger;
    private int color;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;
    private boolean playSound;
    private Identifier sound;

    /**
     * @param trigger The string to trigger the notification.
     * @param strColor The color to make the text, as ab RGB int or hex.
     *                 Defaults to white (int 16777215, hex #FFFFFF) if invalid.
     * @param bold Text format.
     * @param italic Text format.
     * @param underlined Text format.
     * @param strikethrough Text format.
     * @param obfuscated Text format.
     * @param playSound Whether to play the specified sound as part of the
     *                  notification.
     * @param soundName The identifier of the Minecraft Sound to play as part
     *                  of the notification.
     *                  Accepts the formats "namespace:category.source.sound",
     *                  and "category.source.sound" (defaults to "minecraft"
     *                  namespace).
     * @param keep Whether the notification should be kept irrespective of
     *             whether the setter methods are ever called.
     */
    public Notification(String trigger, String strColor, boolean bold, boolean italic,
                        boolean underlined, boolean strikethrough,
                        boolean obfuscated, boolean playSound, String soundName,
                        boolean keep)
    {
        setTrigger(trigger);
        setColor(parseHexInt(strColor));
        setBold(bold);
        setItalic(italic);
        setUnderlined(underlined);
        setStrikethrough(strikethrough);
        setObfuscated(obfuscated);
        setPlaySound(playSound);
        setSound(parseSound(soundName));
        this.keep = keep;
    }

    // Accessors.

    public String getTrigger()
    {
        return this.trigger;
    }

    public int getColor()
    {
        return this.color;
    }

    public boolean getBold()
    {
        return this.bold;
    }

    public boolean getItalic()
    {
        return this.italic;
    }

    public boolean getUnderlined()
    {
        return this.underlined;
    }

    public boolean getStrikethrough()
    {
        return this.strikethrough;
    }

    public boolean getObfuscated()
    {
        return this.obfuscated;
    }

    public boolean getPlaySound()
    {
        return this.playSound;
    }

    public Identifier getSound()
    {
        return this.sound;
    }

    // Mutators.

    public void setTrigger(String trigger)
    {
        this.trigger = Objects.requireNonNullElse(trigger, "");
        keep = true;
    }

    public void setColor(int color)
    {
        if (color < 0 || color > 16777215) {
            this.color = 16777215; // White.
        } else {
            this.color = color;
        }
        keep = true;
    }

    public void setBold(boolean bold)
    {
        this.bold = bold;
    }

    public void setItalic(boolean italic)
    {
        this.italic = italic;
    }

    public void setUnderlined(boolean underlined)
    {
        this.underlined = underlined;
    }

    public void setStrikethrough(boolean strikethrough)
    {
        this.strikethrough = strikethrough;
    }

    public void setObfuscated(boolean obfuscated)
    {
        this.obfuscated = obfuscated;
    }

    public void setPlaySound(boolean playSound)
    {
        this.playSound = playSound;
    }

    public void setSound(Identifier sound)
    {
        if (validSound(sound)) {
            this.sound = sound;
        }
        else {
            this.sound = DEFAULTSOUND;
        }
        keep = true;
    }

    // Other processing.

    /**
     * Parses and validates a string representing an RGB int or hex color.
     * Can handle regular int colors such as "16711680", and both expressions
     * of hex color ("#FF0000" and "FF0000").
     * @param strColor The RGB int or hex color to parse.
     * @return The validated color as an RGB int, represented by a String.
     * Defaults to white(int 16777215, hex #FFFFFF) if the input is invalid.
     */
    public int parseHexInt(String strColor)
    {
        int color;
        try {
            // Normal integer.
            color = Integer.parseInt(strColor);
        } catch (NumberFormatException e1) {
            try {
                // Hex with leading hash.
                color = Integer.parseInt(strColor.split("#")[1], 16);
            } catch (NumberFormatException | IndexOutOfBoundsException e2) {
                try {
                    // Hex without leading hash.
                    color = Integer.parseInt(strColor, 16);
                } catch (NumberFormatException e3) {
                    color = 16777215; // White.
                }
            }
        }
        return color;
    }

    /**
     * Attempts to match the given string to a Minecraft sound. Accepts format
     * "namespace:category.source.sound" (such as "minecraft:block.anvil.land").
     * If the "namespace:" is omitted, will default to "minecraft:".
     * @param soundName The string representing the sound.
     * @return The sound Identifier, or a default Identifier if the string
     * cannot be parsed.
     */
    public Identifier parseSound(String soundName)
    {
        Identifier sound = Identifier.tryParse(soundName);

        if (sound == null) {
            sound = DEFAULTSOUND;
            this.playSound = false;
        }
        return sound;
    }

    /**
     * Determines whether the specified Identifier represents a playable sound.
     * Returns true if the check cannot be run due to the Minecraft state, or
     * if the Identifier is valid, false otherwise.
     * @param sound The sound Identifier.
     * @return The boolean result of validation.
     */
    public boolean validSound(Identifier sound) {
        /*
        Uses Minecraft's internal approach to sound validation, for lack of
        a better idea. In theory this should only equal null if it is
        impossible to play the sound.
         */
        boolean valid = true;
        if (ChatNotifyClient.client.player != null) {
            if (new PositionedSoundInstance(sound, SoundCategory.PLAYERS,
                    1f, 1f, SoundInstance.createRandom(), false, 0,
                    SoundInstance.AttenuationType.NONE, 0, 0, 0, true)
                    .getSoundSet(ChatNotifyClient.client.getSoundManager())
                    == null) {
                valid = false;
            }
        }
        return valid;
    }


    /**
     * Used to validate a Notification when it is created not using the
     * constructor, such as from a config file.
     */
    public void validate()
    {
        setTrigger(getTrigger());
        setColor(getColor());
        setBold(getBold());
        setItalic(getItalic());
        setUnderlined(getUnderlined());
        setStrikethrough(getStrikethrough());
        setObfuscated(getObfuscated());
        setPlaySound(getPlaySound());
        setSound(getSound());
    }
}