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
    public boolean ignoreOwnMessages = false;
    private final Map<Integer, Notification> notifications;

    public Config()
    {
        notifications = new TreeMap<>();

        // Default notification when the user's name appears in chat.
        notifications.put(0, new Notification(
                "username", "#FFC400", false, false, false, false, false, true,
                "ENTITY_EXPERIENCE_ORB_PICKUP"));

        // Template notification for the user to modify.
        notifications.put(1, new Notification(
                "template", "#FFFFFF", false, false, false, false, false, false,
                "ENTITY_EXPERIENCE_ORB_PICKUP"));
    }

    /**
     * Ensures that the loaded config is valid and complete, fixing it if it
     * is not.
     */
    public void validateOptions()
    {
        // If notification zero (player name) doesn't exist, create it.
        if(notifications.get(0) == null) {
            notifications.put(0, new Notification(
                    "username", "FFFFFF", false, false, false, false, false,
                    false, "ENTITY_EXPERIENCE_ORB_PICKUP"));
        }

        // All notification objects can self-validate.
        for (Notification o : notifications.values()) {
            o.validate();
        }
    }

    /**
     * Returns the notification assigned to the given key, or null if none
     * exists for that key. Note that the username notification is assigned to
     * key 0.
     */
    public Notification getNotification(int key)
    {
        return notifications.get(key);
    }

    /**
     * @return An unmodifiable view of the options map.
     */
    public Map<Integer, Notification> getNotifications()
    {
        return Collections.unmodifiableMap(notifications);
    }

    // The methods below are not currently used.
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
    public void addNotification(int key, Notification option) throws ChatNotifyException
    {
        if (key < 0) {
            throw new ChatNotifyException("Option key must be at least 0");
        }

        for (Notification o : notifications.values()) {
            if (o.getTrigger().equals(option.getTrigger())) {
                throw new ChatNotifyException(
                        "There is already a notify option for that word.");
            }
        }

        if (key != 0 && notifications.containsKey(key)) {
            // Shuffle all higher-key options up one, to make space.
            int end = notifications.size();
            for (int i = end; i > key; i--) {
                notifications.put(i, notifications.get(i-1)); // Replaces existing option.
            }
            notifications.put(key, option); // Insert new option.
        }
        else {
            // Add to end (highest key, lowest priority).
            notifications.put(notifications.size(), option);
        }
    }

    public void removeNotification(int key)
    {
        notifications.remove(key);
    }
}
