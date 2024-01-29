package notryken.chatnotify.config;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import notryken.chatnotify.util.MiscUtil;
import notryken.chatnotify.util.SoundUtil;

import java.util.*;

/**
 * A {@code Notification} consists of activation criteria, and parameters for
 * all ChatNotify user-notification functions.
 */
public class Notification {
    // Saved, modifiable by user
    private boolean enabled;
    private final ArrayList<Boolean> controls;
    private final ArrayList<String> triggers;
    public boolean triggerIsKey;
    private TextColor color;
    private final ArrayList<Boolean> formatControls;
    private float soundVolume;
    private float soundPitch;
    private ResourceLocation sound;
    public boolean regexEnabled;
    public boolean exclusionEnabled;
    private final ArrayList<String> exclusionTriggers;
    public boolean responseEnabled;
    private final ArrayList<String> responseMessages;

    /**
     * <b>Note:</b> Not validated.
     */
    public Notification(boolean enabled, ArrayList<Boolean> controls,
                 ArrayList<String> triggers, boolean triggerIsKey,
                 TextColor color, ArrayList<Boolean> formatControls,
                 float soundVolume, float soundPitch, ResourceLocation sound, boolean regexEnabled,
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
        this.regexEnabled = regexEnabled;
        this.exclusionEnabled = exclusionEnabled;
        this.exclusionTriggers = exclusionTriggers;
        this.responseEnabled = responseEnabled;
        this.responseMessages = responseMessages;
    }


    // Controls get and set ////////////////////////////////////////////////////

    /*
    Enabling and Disabling Notifications

    Each Notification has a boolean "enabled", which indicates whether it should
    be processed when checking incoming messages, an ArrayList of three booleans
    "controls" indicating the enabled status of color, format and sound
    notification elements respectively, and an ArrayList of five booleans
    "formatControls" indicating the status of each of the five format elements.

    The value of "enabled" can be changed by the user via a toggle in the main
    config screen, however for ease of use the following automatic changes are
    implemented:

    - If all controls are false and "enabled" is made true, controls 0 and 2
    (color and sound) are made true.
    - autoDisable() can be used to make "enabled" false if all controls are
    false.
    - The value of control 1 (format) is tied to the value of
    formatControls.contains(true), and cannot be directly changed by the user.
     */

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && !controls.contains(true)) {
            controls.set(0, true);
            controls.set(2, true);
        }
    }

    /**
     * @param index the control index (0:color, 1:format, 2:sound).
     * @return the value of the control.
     */
    public boolean getControl(int index) {
        return controls.get(index);
    }

    /**
     * <b>Note:</b> Index 1 (format) should never be set out of sync with
     * formatControls.contains(true).
     * @param index the index of the control (0:color, 1:format, 2:sound).
     * @param value the value to set the control to.
     */
    public void setControl(int index, boolean value) {
        controls.set(index, value);
        // If sound is turned on and volume is off, set volume to 1.
        if (index == 2 && value && soundVolume == 0f) {
            soundVolume = 1f;
        }
    }

    /**
     * @param index the format control index (0:bold, 1:italic, 2:underlined,
     *              3:strikethrough, 4:obfuscated).
     * @return the value of the format control.
     */
    public boolean getFormatControl(int index) {
        return formatControls.get(index);
    }

    /**
     * @param index the index of the format control (0:bold, 1:italic,
     *              2:underlined, 3:strikethrough, 4:obfuscated).
     * @param value the value to set the format control to.
     */
    public void setFormatControl(int index, boolean value) {
        formatControls.set(index, value);
        setControl(1, formatControls.contains(true));
    }


    // Triggers get and set ////////////////////////////////////////////////////

    /**
     * @return the primary trigger for the {@code Notification} if one exists,
     * {@code null} otherwise.
     */
    public String getTrigger() {
        return triggers.isEmpty() ? null : triggers.get(0);
    }

    /**
     * @param index the index of the trigger.
     * @return the trigger at the specified index if it exists, {@code null}
     * otherwise.
     */
    public String getTrigger(int index) {
        if (index >= 0 && index < triggers.size()) {
            return triggers.get(index);
        }
        return null;
    }

    /**
     * @return an unmodifiable view of the trigger list.
     */
    public List<String> getTriggers() {
        return Collections.unmodifiableList(triggers);
    }

    /**
     * Sets the primary trigger of the {@code Notification}.
     * @param trigger the {@code String} to trigger the notification.
     */
    public void setTrigger(String trigger) {
        if (triggers.isEmpty()) {
            triggers.add(Objects.requireNonNullElse(trigger, ""));
        }
        else {
            triggers.set(0, Objects.requireNonNullElse(trigger, ""));
        }
    }

    /**
     * Sets the trigger at the specified index to the specified value, if the
     * index is valid.
     * @param index the index of the trigger to set.
     * @param trigger the new trigger {@code String}.
     */
    public void setTrigger(int index, String trigger) {
        if (index >= 0 && index < triggers.size()) {
            triggers.set(index, trigger);
        }
    }

    /**
     * Adds the specified trigger.
     * @param trigger the trigger {@code String} to add.
     */
    public void addTrigger(String trigger) {
        triggers.add(trigger);
    }

    /**
     * Removes the trigger at the specified index, if the index is valid.
     * <p>
     * <b>Note:</b> Will fail without error if the specified index is invalid
     * or the trigger is the only one.
     * @param index the index of the trigger to remove.
     */
    public void removeTrigger(int index) {
        if (index >= 0 && index < triggers.size() && (index > 0 || triggers.size() > 1)) {
            triggers.remove(index);
        }
    }


    // Color get and set ///////////////////////////////////////////////////////

    /**
     * @return The {@code TextColor} used by the {@code Notification}.
     */
    public TextColor getColor() {
        return color;
    }

    /**
     * @return The integer value of the {@code TextColor} used by the
     * {@code Notification}.
     */
    public int getColorInt() {
        return color.getValue();
    }

    /**
     * @param color the {@code TextColor} to be used by the notification.
     */
    public void setColor(TextColor color) {
        if (color != null) {
            this.color = color;
        }
    }

    /**
     * @param color the RGB integer value of the color to be used by the
     * {@code Notification}.
     */
    public void setColorInt(int color) {
        this.color = TextColor.fromRgb(color);
    }


    // Sound get and set ///////////////////////////////////////////////////////

    /**
     * @return the {@code ResourceLocation} of the sound used by the
     * {@code Notification}.
     */
    public ResourceLocation getSound() {
        return sound;
    }

    /**
     * If the specified sound is invalid, uses a default. Also enables sound.
     * @param sound the {@code ResourceLocation} of the sound.
     */
    public void setSound(ResourceLocation sound) {
        this.sound = SoundUtil.validSound(sound) ? sound : Config.DEFAULT_SOUND;
        setControl(2, true);
    }

    public float getSoundVolume() {
        return this.soundVolume;
    }

    /**
     * @param soundVolume the notification sound volume between 0.0 and 1.0
     *                    inclusive.
     */
    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
        setControl(2, soundVolume != 0f);
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    /**
     * @param soundPitch the notification sound pitch between 0.5 and 2.0
     *                   inclusive.
     */
    public void setSoundPitch(float soundPitch) {
        this.soundPitch = soundPitch;
    }


    // Exclusion triggers get and set //////////////////////////////////////////

    /**
     * @param index the index of the exclusion trigger.
     * @return the trigger at the specified index if it exists, {@code null}
     * otherwise.
     */
    public String getExclusionTrigger(int index) {
        if (index >= 0 && index < exclusionTriggers.size()) {
            return exclusionTriggers.get(index);
        }
        return null;
    }

    /**
     * @return the list of exclusion triggers.
     */
    public List<String> getExclusionTriggers() {
        return exclusionTriggers;
    }

    /**
     * Sets the exclusion trigger at the specified index to the specified value.
     * @param index the index of the trigger to set.
     * @param exclusionTrigger the new exclusion trigger {@code String}.
     */
    public void setExclusionTrigger(int index, String exclusionTrigger) {
        if (index >= 0 && index < exclusionTriggers.size()) {
            exclusionTriggers.set(index, exclusionTrigger);
        }
    }

    /**
     * Adds the specified exclusion trigger.
     * @param exclusionTrigger the exclusion trigger {@code String} to add.
     */
    public void addExclusionTrigger(String exclusionTrigger) {
        exclusionTriggers.add(exclusionTrigger);
    }

    /**
     * Removes the exclusion trigger at the specified index, if the index is
     * valid.
     * @param index the index of the exclusion trigger to remove.
     */
    public void removeExclusionTrigger(int index) {
        if (index >= 0 && index < exclusionTriggers.size()) {
            exclusionTriggers.remove(index);
        }
    }


    // Response messages get and set ///////////////////////////////////////////

    /**
     * @param index the index of the response message.
     * @return the message at the specified index if it exists, {@code null}
     * otherwise.
     */
    public String getResponseMessage(int index) {
        if (index >= 0 && index < responseMessages.size()) {
            return responseMessages.get(index);
        }
        return null;
    }

    /**
     * @return the list of response messages.
     */
    public List<String> getResponseMessages()
    {
        return responseMessages;
    }

    /**
     * Sets the response message at the specified index to the specified value.
     * @param index the index of the message to set.
     * @param responseMessage the new response message {@code String}.
     */
    public void setResponseMessage(int index, String responseMessage) {
        if (index >= 0 && index < responseMessages.size()) {
            responseMessages.set(index, responseMessage);
        }
    }

    /**
     * Adds the specified response message.
     * @param responseMessage the response message {@code String} to add.
     */
    public void addResponseMessage(String responseMessage) {
        responseMessages.add(responseMessage);
    }

    /**
     * Removes the response message at the specified index, if the index is
     * valid.
     * @param index the index of the response message to remove.
     */
    public void removeResponseMessage(int index) {
        if (index >= 0 && index < responseMessages.size()) {
            responseMessages.remove(index);
        }
    }


    // Validation and cleanup //////////////////////////////////////////////////

    /**
     * Disables regex, exclusion triggers and response messages and clears the
     * lists of the latter two.
     */
    public void resetAdvanced() {
        regexEnabled = false;
        exclusionEnabled = false;
        responseEnabled = false;
        exclusionTriggers.clear();
        responseMessages.clear();
    }

    /**
     * Removes all blank and duplicate non-primary triggers. Comparison is
     * case-sensitive if regexEnabled is {@code true}, case-insensitive
     * otherwise.
     */
    public void purgeTriggers() {
        triggers.removeIf(String::isBlank);
        if (regexEnabled) MiscUtil.removeDuplicates(triggers);
        else MiscUtil.removeDuplicatesCaseInsensitive(triggers);
        if (triggers.isEmpty()) triggers.add("");
    }

    /**
     * If configured for key-activation, converts all keys to lowercase.
     */
    public void fixKeyTriggerCase() {
        if (triggerIsKey) {
            triggers.replaceAll(s -> s.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Removes all blank and duplicate exclusion triggers. Comparison is
     * case-sensitive if regexEnabled is {@code true}, case-insensitive
     * otherwise.
     */
    public void purgeExclusionTriggers() {
        exclusionTriggers.removeIf(String::isBlank);
        if (regexEnabled) MiscUtil.removeDuplicates(exclusionTriggers);
        else MiscUtil.removeDuplicatesCaseInsensitive(exclusionTriggers);
        if (exclusionTriggers.isEmpty()) exclusionEnabled = false;
    }

    /**
     * Removes all blank and duplicate response messages.
     */
    public void purgeResponseMessages() {
        responseMessages.removeIf(String::isBlank);
        MiscUtil.removeDuplicates(responseMessages);
        if (responseMessages.isEmpty()) responseEnabled = false;
    }

    /**
     * If all controls are disabled, disables the notification.
     */
    public void autoDisable() {
        if (!controls.contains(true)) {
            enabled = false;
        }
    }
}