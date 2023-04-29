package notryken.chatnotify.config;

import notryken.chatnotify.misc.ChatNotifyException;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * User-customizable configuration options class, with defaults.
 */
public class Config
{
    // Whether to reload the config file when joining a world or server.
    public boolean reloadOnJoin = true;
    public String playerName;
    // Skip processing for messages sent by the user.
    public boolean ignoreOwnMessages = false;
    private final Map<Integer,NotifyOption> options;

    public Config()
    {
        options = new TreeMap<>();

        // Default notification when the user's name appears in chat.
        options.put(0, new NotifyOption(
                "username", "#FFC400", false, false, false, false, false, true,
                "ENTITY_EXPERIENCE_ORB_PICKUP"));

        // Template notification for the user to modify.
        options.put(1, new NotifyOption(
                "template", "#FFFFFF", false, false, false, false, false, false,
                "ENTITY_EXPERIENCE_ORB_PICKUP"));
    }

    /**
     * Ensures that the loaded config is valid and complete, fixing it if it
     * is not.
     */
    public void validateOptions()
    {
        // If option zero (player name) doesn't exist, create it.
        if(options.get(0) == null) {
            options.put(0, new NotifyOption(
                    "username", "FFFFFF", false, false, false, false, false,
                    false, "default"));
        }

        for (NotifyOption o : options.values()) {
            o.validate();
        }
    }

    /**
     * Returns the option assigned to the given key, or null if no option exists
     * for that key. Note that the username option is assigned to key 0.
     */
    public NotifyOption getOption(int key)
    {
        return options.get(key);
    }

    /**
     * @return An unmodifiable view of the options map.
     */
    public Map<Integer,NotifyOption> getOptions()
    {
        return Collections.unmodifiableMap(options);
    }

    // This method is not currently used.
    /**
     * Adds a new option. If there is already an option with the specified key,
     * will move the existing option to a lower priority. If the key is zero,
     * or greater than all existing options, the new option will be added in the
     * lowest priority position.
     * @param key The priority of the option (lower number is higher priority,
     *            except for zero, which means lowest priority).
     * @param option The option.
     * @throws ChatNotifyException If there is already an option with the same
     * word as the given option, or if the key is not valid (0).
     */
    public void addOption(int key, NotifyOption option) throws ChatNotifyException
    {
        if (key < 0) {
            throw new ChatNotifyException("Option key must be at least 0");
        }

        for (NotifyOption o : options.values()) {
            if (o.getWord().equals(option.getWord())) {
                throw new ChatNotifyException(
                        "There is already a notify option for that word.");
            }
        }

        if (key != 0 && options.containsKey(key)) {
            // Shuffle all higher-key options up one, to make space.
            int end = options.size();
            for (int i = end; i > key; i--) {
                options.put(i, options.get(i-1)); // Replaces existing option.
            }
            options.put(key, option); // Insert new option.
        }
        else {
            // Add to end (highest key, lowest priority).
            options.put(options.size(), option);
        }
    }
}
