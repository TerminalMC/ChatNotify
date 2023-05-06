package notryken.chatnotify.config;

import java.util.*;

/**
 * User-customizable configuration options class, with defaults.
 */
public class Config
{
    private String username;
    private boolean ignoreOwnMessages = false;
    private final List<Notification> notifications;

    public Config()
    {
        notifications = new ArrayList<>();

        // Default notification when the user's name appears in chat.
        notifications.add(0, new Notification(
                "username", "#FFC400", false, false, false, false, false, true,
                "minecraft:entity.experience_orb.pickup", true));
    }

    /**
     * Ensures that the loaded config is valid and complete, fixing it if it
     * is not.
     */
    public void validateOptions()
    {
        // If notification zero (player name) doesn't exist, create it.
        if(notifications.get(0) == null) {
            notifications.add(0, new Notification(
                    "username", "#FFFFFF", false, false, false, false, false,
                    false, "ENTITY_EXPERIENCE_ORB_PICKUP", true));
        }

        // All notification objects can self-validate.
        for (Notification o : notifications) {
            o.validate();
        }
    }

    public int getNumNotifications()
    {
        return this.notifications.size();
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
    public List<Notification> getNotifications()
    {
        return Collections.unmodifiableList(notifications);
    }

    public boolean getIgnoreOwnMsg()
    {
        return ignoreOwnMessages;
    }

    public void setIgnoreOwnMsg(boolean newStatus)
    {
        ignoreOwnMessages = newStatus;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
        reloadUsername();
    }

    public void reloadUsername()
    {
        notifications.get(0).setTrigger(username);
    }


    public void purge()
    {
        Notification notif;
        Iterator<Notification> iter = notifications.iterator();
        iter.next(); // Skip the first notification.
        while (iter.hasNext()) {
            notif = iter.next();
            if (!notif.keep || notif.getTrigger().strip().equals("")) {
                iter.remove();
            }
        }
    }

    public void addNotification()
    {
        notifications.add(new Notification("", "#FFFFFF", false, false, false,
                false, false, true, "minecraft:block.anvil.land", false));
    }

    public void removeNotification(int key)
    {
        notifications.remove(key);
    }
}
