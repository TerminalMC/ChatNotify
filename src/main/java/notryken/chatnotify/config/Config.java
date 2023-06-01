package notryken.chatnotify.config;

import java.util.*;

/**
 * User-customizable configuration options class, with defaults.
 */
public class Config
{
    public static final String DEFAULTSOUND = "entity.arrow.hit_player";
    public static final Notification DEFAULTNOTIF =
            new Notification(true, true, false, true, "username", false,
                    "#FFC400", false, false, false, false, false, 1f, 1f,
                    DEFAULTSOUND, true);

    public boolean ignoreOwnMessages;
    private String username;
    private final ArrayList<Notification> notifications;

    /**
     * Initialises the config with minimum default values.
     */
    public Config()
    {
        ignoreOwnMessages = false;
        username = null;
        notifications = new ArrayList<>();
        notifications.add(0, DEFAULTNOTIF);
    }

    /**
     * Initializes the config with specified values.
     */
    Config(boolean ignoreOwnMessages, String username,
           ArrayList<Notification> notifications)
    {
        this.ignoreOwnMessages = ignoreOwnMessages;
        this.username = username;
        this.notifications = notifications;
    }

    // Accessors.

    /**
     * @return The total number of notifications, including disabled ones.
     */
    public int getNumNotifs()
    {
        return this.notifications.size();
    }

    /**
     * @return The notification at the specified index, or null if none exists.
     * Note that the username notification is located at index 0.
     */
    public Notification getNotif(int index)
    {
        return notifications.get(index);
    }

    /**
     * @return An unmodifiable view of the notification map.
     */
    public List<Notification> getNotifs()
    {
        return Collections.unmodifiableList(notifications);
    }

    // Mutators

    /**
     * 'Smart' setter; ensures that the username notification's trigger is also
     * set to the specified username.
     * @param username The user's in-game name.
     */
    public void setUsername(String username)
    {
        this.username = username;
        refreshUsernameNotif();
    }

    /**
     * Ensures that the username notification's trigger and trigger type are
     * valid.
     */
    public void refreshUsernameNotif()
    {
        Notification notif = notifications.get(0);
        notif.triggerIsKey = false;
        if (username != null) {
            notif.setTrigger(username);
        }
    }

    /**
     * Adds a new notification with empty default values
     */
    public void addNotif()
    {
        notifications.add(new Notification(true, false, false, false, "", false,
                "#FFFFFF", false, false, false, false, false, 1f, 1f,
                DEFAULTSOUND, false));
    }

    /**
     * Removes the notification at the specified index, if it exists. Will not
     * remove the notification at index 0.
     * @param index The index of the notification.
     * @return 0 if the operation was successful, -1 otherwise.
     */
    public int removeNotif(int index)
    {
        if (index > 0 && index < notifications.size()) {
            notifications.remove(index);
            return 0;
        }
        return -1;
    }

    /**
     * Increments the notification 'up' to a higher priority (lower index).
     * @param index The current location of the notification to move.
     */
    public void moveNotifUp(int index)
    {
        if (index > 1 && index < notifications.size()) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index - 1));
            notifications.set(index - 1, temp);
        }
    }

    /**
     * Increments the notification 'down' to a lower priority (higher index).
     * @param index The current location of the notification to move.
     */
    public void moveNotifDown(int index)
    {
        if (index > 0 && index < notifications.size() - 1) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index + 1));
            notifications.set(index + 1, temp);
        }
    }

    // Other processing.

    /**
     * Removes all non-persistent notifications with empty primary triggers,
     * and removes all empty secondary triggers from the remaining
     * notifications.
     */
    public void purge()
    {
        Notification notif;
        Iterator<Notification> iter = notifications.iterator();
        iter.next(); // Skip the first notification.
        while (iter.hasNext()) {
            notif = iter.next();
            if (!notif.persistent || notif.getTrigger().strip().equals(""))
            {
                iter.remove();
            }
        }
        for (Notification notif2: notifications) {
            notif2.purgeTriggers();
            notif2.purgeExclusionTriggers();
            notif2.purgeResponseMessages();
        }
    }
}
