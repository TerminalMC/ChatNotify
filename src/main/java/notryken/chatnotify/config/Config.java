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
                    "username", "#FFFFFF", false, false, false, false, false,
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
     * @return An unmodifiable view of the notification map.
     */
    public Map<Integer, Notification> getNotifications()
    {
        return Collections.unmodifiableMap(notifications);
    }

    // The methods below are not currently used.
    /**
     * Adds a new notification. If there is already a notification with the
     * specified key, will move the existing one to a lower priority. If the
     * key is zero, or greater than all existing notifications, the new one will
     * be added in the lowest priority position.
     * @param key The priority of the notification (lower number is higher
     *            priority, except for zero, which defaults to the lowest
     *            priority, because notification zero is reserved).
     * @param notif The notification.
     * @throws ChatNotifyException If there is already a notification with the
     * same trigger as the given notification, or if the key is not valid (<0).
     */
    public void addNotification(int key, Notification notif)
            throws ChatNotifyException
    {
        if (key < 0) {
            throw new ChatNotifyException("Key must be at least 0");
        }

        for (Notification o : notifications.values()) {
            if (o.getTrigger().equals(notif.getTrigger())) {
                throw new ChatNotifyException(
                        "There is already a notification for that trigger.");
            }
        }

        if (key != 0 && notifications.containsKey(key)) {
            // Shuffle all higher-key notifications up one, to make space.
            int end = notifications.size();
            for (int i = end; i > key; i--) {
                notifications.put(i, notifications.get(i-1));
            }
            notifications.put(key, notif); // Insert new notification.
        }
        else {
            // Add to end (highest key, lowest priority).
            notifications.put(notifications.size(), notif);
        }
    }

    public void removeNotification(int key)
    {
        notifications.remove(key);
    }
}
