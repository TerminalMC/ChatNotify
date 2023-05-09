package notryken.chatnotify.config;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import notryken.chatnotify.client.ChatNotifyClient;

import java.util.*;

/**
 * Configurable notification including text color, text formatting
 * and sound parameters for a specified trigger word.
 */
public class Notification
{
    private static final Identifier DEFAULTSOUND =
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.getId();
    public boolean enabled;
    private final List<Boolean> controls = new ArrayList<>();
    private final List<String> triggers = new ArrayList<>();
    public boolean triggerIsKey;
    private int color;
    private final List<Boolean> formatControls = new ArrayList<>();
    public float soundVolume;
    public float soundPitch;
    private Identifier sound;
    public boolean persistent;

    /**
     * @param trigger The string to trigger the notification.
     * @param triggerIsKey Whether the trigger is an event key.
     * @param strColor The color to make the text, as ab RGB int or hex.
     *                 Defaults to white (int 16777215, hex #FFFFFF) if invalid.
     * @param bold Text format.
     * @param italic Text format.
     * @param underlined Text format.
     * @param strikethrough Text format.
     * @param obfuscated Text format.
     * @param soundVolume Sound volume.
     * @param soundPitch Sound pitch.
     * @param soundName The identifier of the Minecraft Sound to play as part
     *                  of the notification.
     *                  Accepts the formats "namespace:category.source.sound",
     *                  and "category.source.sound" (defaults to "minecraft"
     *                  namespace).
     * @param persistent Whether the notification should be kept irrespective of
     *                   whether it is ever modified.
     */
    public Notification(boolean enabled, boolean doColor, boolean doFormat,
                        boolean doSound, String trigger, boolean triggerIsKey,
                        String strColor, boolean bold, boolean italic,
                        boolean underlined, boolean strikethrough,
                        boolean obfuscated, float soundVolume,
                        float soundPitch, String soundName, boolean persistent)
    {
        this.enabled = enabled;
        controls.add(doColor);
        controls.add(doFormat);
        controls.add(doSound);
        setTrigger(trigger);
        this.triggerIsKey = triggerIsKey;
        setColor(parseHexInt(strColor));
        formatControls.add(bold);
        formatControls.add(italic);
        formatControls.add(underlined);
        formatControls.add(strikethrough);
        formatControls.add(obfuscated);
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        setSound(parseSound(soundName));
        this.persistent = persistent;
    }

    // Accessors.

    /**
     * Key -> 0:color, 1:format, 2:sound.
     */
    public boolean getControl(int index)
    {
        return this.controls.get(index);
    }


    public String getTrigger()
    {
        return this.triggers.get(0);
    }

    public String getTrigger(int index)
    {
        if (this.triggers.size() > index)
        {
            return this.triggers.get(index);
        }
        return null;
    }

    public Iterator<String> getTriggerIterator()
    {
        return this.triggers.iterator();
    }

    public int getNumTriggers()
    {
        return this.triggers.size();
    }

    public int getColor()
    {
        return this.color;
    }

    /**
     * Key -> 0:bold, 1:italic, 2:underlined, 3:strikethrough, 4:obfuscated.
     */
    public boolean getFormatControl(int index)
    {
        return this.formatControls.get(index);
    }

    public Identifier getSound()
    {
        return this.sound;
    }

    // Mutators.

    /**
     * 'Smart' setter; disables the parent if all sibling control are disabled,
     * and enables the parent when enabled. If the control has sub-controls,
     * prevents enabling it if they are all disabled.
     * Key -> 0:color, 1:format, 2:sound.
     */
    public void setControl(int index, boolean value)
    {
        if (index != 1 || controls.get(index) || formatControls.contains(true))
        {
            this.controls.set(index, value);
            if (!value) {
                if (!controls.contains(true)) {
                    enabled = false;
                }
            }
            else {
                this.enabled = true;
            }
        }
    }

    public void setTrigger(String trigger)
    {
        if (this.triggers.size() > 0) {
            this.triggers.set(0, Objects.requireNonNullElse(trigger, ""));
        }
        else {
            this.triggers.add(Objects.requireNonNullElse(trigger, ""));
        }
        persistent = true;
    }

    public void setTrigger(int index, String trigger)
    {
        if (this.triggers.size() > index && trigger != null && !this.triggers.contains(trigger)) {
            this.triggers.set(index, trigger);
        }
    }

    public void addTrigger(String trigger)
    {
        if (trigger != null && !this.triggers.contains(trigger)) {
            this.triggers.add(trigger);
        }
    }

    public void purgeTriggers()
    {
        Iterator<String> iter = this.triggers.iterator();
        iter.next();
        while (iter.hasNext()) {
            if (iter.next().strip().equals("")) {
                iter.remove();
            }
        }
    }

    public void removeTrigger(int index)
    {
        if (this.triggers.size() > index) {
            this.triggers.remove(index);
        }
    }

    public void setTriggerIsKey(boolean triggerIsKey)
    {
        this.triggerIsKey = triggerIsKey;
    }

    /**
     * 'Smart' setter; if color is invalid, defaults to white (16777215). If
     * color is white, disables message coloring.
     */
    public void setColor(int color)
    {
        if (color < 0 || color > 16777215) {
            this.color = 16777215; // White.
        } else {
            this.color = color;
        }
        this.controls.set(0, this.color != 16777215);
        persistent = true;
    }

    /**
     * 'Smart' setter; disables the parent if all sibling options are disabled,
     * and enables the parent when enabled.
     * Key -> 0:bold, 1:italic, 2:underlined, 3:strikethrough, 4:obfuscated.
     */
    public void setFormatControl(int index, boolean value)
    {
        formatControls.set(index, value);
        if (!value) {
            if (!formatControls.contains(true)) {
                setControl(1, false);
            }
        }
        else {
            setControl(1, true);
        }
    }

    public void setSound(Identifier sound)
    {
        if (validSound(sound)) {
            this.sound = sound;
        }
        else {
            this.sound = DEFAULTSOUND;
        }
        persistent = true;
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
            this.soundVolume = 0;
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
        setSound(getSound());
    }
}