package notryken.chatnotify.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Configurable notification including text color, text formatting and sound
 * parameters for a set of trigger words.
 */
public class Notification
{
    private static final ResourceLocation DEFAULTSOUND =
            ResourceLocation.tryParse(Config.DEFAULTSOUND);
    public boolean enabled;
    private final ArrayList<Boolean> controls;
    private final ArrayList<String> triggers;
    public boolean triggerIsKey;
    private TextColor color;
    private final ArrayList<Boolean> formatControls;
    public float soundVolume;
    public float soundPitch;
    private ResourceLocation sound;
    public boolean persistent;
    public boolean regexEnabled;
    public boolean exclusionEnabled;
    private final ArrayList<String> exclusionTriggers;
    public boolean responseEnabled;
    private final ArrayList<String> responseMessages;

    /**
     * Validated.
     */
    Notification(boolean enabled, boolean doColor, boolean doFormat,
                 boolean doSound, String trigger, boolean triggerIsKey,
                 String strColor, boolean bold, boolean italic,
                 boolean underlined, boolean strikethrough, boolean obfuscated,
                 float soundVolume, float soundPitch, String soundName,
                 boolean persistent)
    {
        this.controls = new ArrayList<>(List.of(true, true, true));
        this.triggers = new ArrayList<>();
        this.formatControls = new ArrayList<>(List.of(true, true, true, true, true));

        this.enabled = enabled;
        setControl(0, doColor);
        setControl(1, doFormat);
        setControl(2, doSound);
        setTrigger(trigger);
        this.triggerIsKey = triggerIsKey;
        setColor((strColor == null ? null : parseColor(strColor)));
        setFormatControl(0, bold);
        setFormatControl(1, italic);
        setFormatControl(2, underlined);
        setFormatControl(3, strikethrough);
        setFormatControl(4, obfuscated);
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        setSound(parseSound(soundName));
        this.persistent = persistent;
        regexEnabled = false;
        exclusionEnabled = false;
        exclusionTriggers = new ArrayList<>();
        responseEnabled = false;
        responseMessages = new ArrayList<>();
    }

    /**
     * Not validated.
     */
    Notification(boolean enabled, ArrayList<Boolean> controls,
                 ArrayList<String> triggers, boolean triggerIsKey,
                 TextColor color, ArrayList<Boolean> formatControls,
                 float soundVolume, float soundPitch, ResourceLocation sound,
                 boolean persistent, boolean regexEnabled,
                 boolean exclusionEnabled, ArrayList<String> exclusionTriggers,
                 boolean responseEnabled, ArrayList<String> responseMessages)
    {
        this.enabled = enabled;
        this.controls = controls;
        this.triggers = triggers;
        this.triggerIsKey = triggerIsKey;
        this.color = color;
        this.formatControls = formatControls;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.sound = sound;
        this.persistent = persistent;
        this.regexEnabled = regexEnabled;
        this.exclusionEnabled = exclusionEnabled;
        this.exclusionTriggers = exclusionTriggers;
        this.responseEnabled = responseEnabled;
        this.responseMessages = responseMessages;
    }

    // Accessors.

    /**
     * @param index The control index (0:color, 1:format, 2:sound).
     * @return The value of the control.
     */
    public boolean getControl(int index)
    {
        return this.controls.get(index);
    }

    /**
     * @return The primary trigger for the notification.
     */
    public String getTrigger()
    {
        return this.triggers.get(0);
    }

    /**
     * @param index The index of the trigger.
     * @return The trigger at the specified index if it exists, else null.
     */
    public String getTrigger(int index)
    {
        if (index >= 0 && index < this.triggers.size())
        {
            return this.triggers.get(index);
        }
        return null;
    }

    /**
     * @return The list of triggers.
     */
    public List<String> getTriggers()
    {
        return this.triggers;
    }

    /**
     * @return The TextColor used by the notification.
     */
    public TextColor getColor()
    {
        return this.color;
    }

    /**
     * @param index The format control index (0:bold, 1:italic, 2:underlined,
     *              3:strikethrough, 4:obfuscated).
     * @return The value of the format control.
     */
    public boolean getFormatControl(int index)
    {
        return this.formatControls.get(index);
    }

    /**
     * @return The ResourceLocation of the notification's sound.
     */
    public ResourceLocation getSound()
    {
        return this.sound;
    }


    /**
     * @param index The index of the exclusion trigger.
     * @return The trigger at the specified index if it exists, else null.
     */
    public String getExclusionTrigger(int index)
    {
        if (index >= 0 && index < this.exclusionTriggers.size())
        {
            return this.exclusionTriggers.get(index);
        }
        return null;
    }

    /**
     * @return The list of exclusion triggers.
     */
    public List<String> getExclusionTriggers()
    {
        return this.exclusionTriggers;
    }

    /**
     * @param index The index of the response message.
     * @return The message at the specified index if it exists, else null.
     */
    public String getResponseMessage(int index)
    {
        if (index >= 0 && index < this.responseMessages.size())
        {
            return this.responseMessages.get(index);
        }
        return null;
    }

    /**
     * @return The list of response messages.
     */
    public List<String> getResponseMessages()
    {
        return this.responseMessages;
    }

    // Mutators.

    /**
     * 'Smart' setter; disables the parent if all sibling control are disabled,
     * and enables the parent when enabled. If the control has sub-controls,
     * prevents enabling it if they are all disabled.
     * @param index The index of the control (0:color, 1:format, 2:sound).
     * @param value The value to set the control to.
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
                // If sound is turned on and volume is off, set volume to 1.
                if (index == 2 && this.soundVolume == 0f) {
                    this.soundVolume = 1f;
                }
            }
        }
    }

    /**
     * Sets the primary trigger of the notification.
     * @param trigger The String to trigger the notification.
     */
    public void setTrigger(String trigger)
    {
        if (this.triggers.isEmpty()) {
            this.triggers.add(0, Objects.requireNonNullElse(trigger, ""));
        }
        else {
            this.triggers.set(0, Objects.requireNonNullElse(trigger, ""));
        }
    }

    /**
     * Sets the trigger at the specified index to the specified value, if the
     * index is valid and the notification does not already contain the trigger.
     * @param index The index of the trigger to set.
     * @param trigger The new trigger String.
     */
    public void setTrigger(int index, String trigger)
    {
        if (index >= 0 && index < this.triggers.size() &&
                !this.triggers.contains(trigger)) {
            this.triggers.set(index, trigger);
        }
    }

    /**
     * Adds the specified trigger, if the notification does not already contain
     * it.
     * @param trigger The trigger String to add.
     */
    public void addTrigger(String trigger)
    {
        if (!this.triggers.contains(trigger)) {
            this.triggers.add(trigger);
        }
    }

    /**
     * Removes the trigger at the specified index, if the index is not 0 and
     * not out of range.
     * @param index The index of the trigger to remove.
     */
    public void removeTrigger(int index)
    {
        if (index > 0 && index < this.triggers.size()) {
            this.triggers.remove(index);
        }
    }

    /**
     * 'Smart' setter; If color is null, disables message coloring, else
     * enables message coloring. Makes the notification persistent.
     */
    public void setColor(TextColor color)
    {
        this.color = color;
        setControl(0, color != null);
        persistent = true;
    }

    /**
     * 'Smart' setter; disables the parent control if all sibling options are
     * disabled, and enables the parent control when enabled. If value is true,
     * makes the notification persistent.
     * @param index The index of the format control (0:bold, 1:italic,
     *              2:underlined, 3:strikethrough, 4:obfuscated).
     * @param value The value to set the format control to.
     */
    public void setFormatControl(int index, boolean value)
    {
        formatControls.set(index, value);
        if (value) {
            setControl(1, true);
            persistent = true;
        }
        else {
            if (!formatControls.contains(true)) {
                setControl(1, false);
            }
        }
    }

    /**
     * 'Smart' setter; if soundVolume is 0, disables sound. Else, enables sound.
     * @param soundVolume The notification sound volume between 0.0 and 1.0
     *                    inclusive.
     */
    public void setSoundVolume(float soundVolume)
    {
        this.soundVolume = soundVolume;
        setControl(2, soundVolume != 0f);
    }

    /**
     * 'Smart' setter; if the specified sound is invalid, uses a default.
     * Additionally, enables sound and makes the notification persistent.
     * @param sound The ResourceLocation of the sound.
     */
    public void setSound(ResourceLocation sound)
    {
        this.sound = validSound(sound) ? sound : DEFAULTSOUND;
        setControl(2, true);
        persistent = true;
    }

    /**
     * Sets the exclusion trigger at the specified index to the specified
     * value, if the index is valid and the notification does not already
     * contain the exclusion trigger.
     * @param index The index of the trigger to set.
     * @param exclusionTrigger The new exclusion trigger String.
     */
    public void setExclusionTrigger(int index, String exclusionTrigger)
    {
        if (index >= 0 && index < this.exclusionTriggers.size() &&
                !this.exclusionTriggers.contains(exclusionTrigger)) {
            this.exclusionTriggers.set(index, exclusionTrigger);
        }
    }

    /**
     * Adds the specified exclusion trigger, if the notification does not
     * already contain it.
     * @param exclusionTrigger The exclusion trigger String to add.
     */
    public void addExclusionTrigger(String exclusionTrigger)
    {
        if (!this.exclusionTriggers.contains(exclusionTrigger)) {
            this.exclusionTriggers.add(exclusionTrigger);
        }
    }

    /**
     * Removes the exclusion trigger at the specified index, if the index is
     * not out of range.
     * @param index The index of the exclusion trigger to remove.
     */
    public void removeExclusionTrigger(int index)
    {
        if (index >= 0 && index < this.exclusionTriggers.size()) {
            this.exclusionTriggers.remove(index);
        }
    }

    /**
     * Sets the response message at the specified index to the specified
     * value, if the index is valid and the notification does not already
     * contain the response message.
     * @param index The index of the trigger to set.
     * @param responseMessage The new response message String.
     */
    public void setResponseMessage(int index, String responseMessage)
    {
        if (index >= 0 && index < this.responseMessages.size() &&
                !this.responseMessages.contains(responseMessage)) {
            this.responseMessages.set(index, responseMessage);
        }
    }

    /**
     * Adds the specified response message, if the notification does not
     * already contain it.
     * @param responseMessage The response message String to add.
     */
    public void addResponseMessage(String responseMessage)
    {
        if (!this.responseMessages.contains(responseMessage)) {
            this.responseMessages.add(responseMessage);
        }
    }

    /**
     * Removes the response message at the specified index, if the index is
     * not out of range.
     * @param index The index of the response message to remove.
     */
    public void removeResponseMessage(int index)
    {
        if (index >= 0 && index < this.responseMessages.size()) {
            this.responseMessages.remove(index);
        }
    }

    // Other processing.

    /**
     * Removes all empty non-primary triggers.
     */
    public void purgeTriggers()
    {
        Iterator<String> iter = this.triggers.iterator();
        iter.next(); // Skip the primary trigger.
        while (iter.hasNext()) {
            if (iter.next().isBlank()) {
                iter.remove();
            }
        }
    }

    /**
     * Removes all empty exclusion triggers.
     */
    public void purgeExclusionTriggers()
    {
        this.exclusionTriggers.removeIf(String::isBlank);
    }

    /**
     * Removes all empty response messages.
     */
    public void purgeResponseMessages()
    {
        this.responseMessages.removeIf(String::isBlank);
    }

    /**
     * If all controls are disabled, disables the notification.
     */
    public void autoDisable()
    {
        if (!controls.contains(true)) {
            enabled = false;
        }
    }

    /**
     * Parses and validates a string representing an RGB int or hex color.
     * Can handle int format e.g. "16711680", and hex format e.g. "#FF0000".
     * @param strColor The RGB int or hex color to parse.
     * @return The validated TextColor.
     * Defaults to null if the input is invalid.
     */
    public TextColor parseColor(String strColor)
    {
        TextColor color;

        if (strColor.startsWith("#")) {
            color = TextColor.parseColor(strColor);
        }
        else {
            try {
                int intColor = Integer.parseInt(strColor);
                if (intColor > 0 && intColor <= 16777215) {
                    color = TextColor.fromRgb(intColor);
                }
                else {
                    color = null;
                }
            }
            catch (NumberFormatException e) {
                color = null;
            }
        }
        return color;
    }

    /**
     * Attempts to match the given string to a Minecraft sound. Accepts format
     * "namespace:category.source.sound" (such as "minecraft:block.anvil.land").
     * If the "namespace:" is omitted, will default to "minecraft:".
     * @param soundName The string representing the sound.
     * @return The sound ResourceLocation, or a default ResourceLocation if the string
     * cannot be parsed.
     */
    public ResourceLocation parseSound(String soundName)
    {
        ResourceLocation sound = ResourceLocation.tryParse(soundName);

        if (sound == null) {
            sound = DEFAULTSOUND;
        }
        return sound;
    }

    /**
     * Determines whether the specified ResourceLocation represents a playable sound.
     * Returns true if the check cannot be run due to the Minecraft state, or
     * if the ResourceLocation is valid, false otherwise.
     * @param sound The sound ResourceLocation.
     * @return The boolean result of validation.
     */
    public boolean validSound(ResourceLocation sound) {
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