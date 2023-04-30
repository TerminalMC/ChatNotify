package notryken.chatnotify.config;

import notryken.chatnotify.misc.Sounds;

import java.util.Objects;

/**
 * Configurable notification including text color, text formatting
 * and sound parameters for a specified trigger word.
 */
public class Notification
{
    private String trigger;
    private String color;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;
    private boolean playSound;
    private Sounds sound;

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
     *                  Accepts the format "minecraft:category.source.sound"
     *                  as well as "CATEGORY_SOURCE_SOUND".
     */
    public Notification(String trigger, String strColor, boolean bold, boolean italic,
                        boolean underlined, boolean strikethrough,
                        boolean obfuscated, boolean playSound, String soundName)
    {
        setTrigger(trigger);
        setColor(parseHexInt(strColor));
        setBold(bold);
        setItalic(italic);
        setUnderlined(underlined);
        setStrikethrough(strikethrough);
        setObfuscated(obfuscated);
        setPlaySound(playSound);
        setSound(findSound(soundName));
    }

    // Accessors.

    public String getTrigger()
    {
        return this.trigger;
    }

    public String getColor()
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

    public Sounds getSound()
    {
        return this.sound;
    }

    // Mutators.

    public void setTrigger(String trigger)
    {
        this.trigger = Objects.requireNonNullElse(trigger, "");
    }

    public void setColor(String color)
    {
        int intColor = Integer.parseInt(color);
        if (intColor < 0 || intColor > 16777215) {
            this.color = "16777215"; // White.
        } else {
            this.color = color;
        }
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

    public void setSound(Sounds sound)
    {
        this.sound = Objects.requireNonNullElse(sound,
                Sounds.ENTITY_EXPERIENCE_ORB_PICKUP);
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
    public String parseHexInt(String strColor)
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
        return String.valueOf(color);
    }

    /**
     * Attempts to match the given string to a Minecraft sound. Accepts the
     * format "minecraft:category.source.sound" as well as
     * "CATEGORY_SOURCE_SOUND".
     * @param soundName The sound identifier.
     * @return The matched Sounds object. Defaults to
     * "ENTITY_EXPERIENCE_ORB_PICKUP" if no match was found.
     */
    public Sounds findSound(String soundName)
    {
        Sounds sound;
        try {
            sound = Sounds.valueOf(soundName);
        } catch (IllegalArgumentException e1) {
            try {
                /* Convert from "minecraft:category.source.sound" to
                 * CATEGORY_SOURCE_SOUND */
                soundName = soundName.split(":")[1].replace(".", "_").toUpperCase();
                sound = Sounds.valueOf(soundName);
            } catch (IllegalArgumentException | IndexOutOfBoundsException e2) {
                sound = Sounds.ENTITY_EXPERIENCE_ORB_PICKUP; // Default
            }
        }
        return sound;
    }

    /**
     * Used to validate a Notification when it is created not using the
     * constructor, such as from a config file.
     */
    public void validate()
    {
        setTrigger(getTrigger());
        setColor(parseHexInt(getColor()));
        setBold(getBold());
        setItalic(getItalic());
        setUnderlined(getUnderlined());
        setStrikethrough(getStrikethrough());
        setObfuscated(getObfuscated());
        setPlaySound(getPlaySound());
        setSound(getSound());
    }
}