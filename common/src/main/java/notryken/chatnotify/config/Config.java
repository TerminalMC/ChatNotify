package notryken.chatnotify.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import notryken.chatnotify.Constants;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * User-customizable configuration options class, with defaults.
 */
public class Config
{
    // Defaults
    private static final String DEFAULT_FILE_NAME = "chatnotify.json";
    public static final String DEFAULT_SOUND = "block.note_block.bell";
    public static final Notification DEFAULT_NOTIF =
            new Notification(true, true, false, true, "username", false,
                    "#FFC400", false, false, false, false, false, 1f, 1f,
                    DEFAULT_SOUND, true);
    public static final List<String> DEFAULT_PREFIXES = List.of("/shout", "!");

    // Stored but not saved
    private static String username;
    private static Path configPath;

    // Options
    // TODO add soundSource field
    private final String FORMAT_VERSION = "001";
    public boolean ignoreOwnMessages;
    private final ArrayList<Notification> notifications;
    private final ArrayList<String> messagePrefixes;

    /**
     * Initialises the config with minimum default values.
     */
    public Config()
    {
        ignoreOwnMessages = false;
        notifications = new ArrayList<>();
        notifications.add(0, DEFAULT_NOTIF);
        messagePrefixes = new ArrayList<>(DEFAULT_PREFIXES);
    }

    /**
     * Initializes the config with specified values.
     */
    Config(boolean ignoreOwnMessages, ArrayList<Notification> notifications,
           ArrayList<String> messagePrefixes)
    {
        this.ignoreOwnMessages = ignoreOwnMessages;
        this.notifications = notifications;
        this.messagePrefixes = messagePrefixes;
    }

    // Accessors

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

    /**
     * @return The prefix at the specified index, or null if none exists or the index is invalid.
     */
    public String getPrefix(int index)
    {
        return (index >= 0 && index < messagePrefixes.size()) ? messagePrefixes.get(index) : null;
    }

    /**
     * @return An unmodifiable view of the prefix list.
     */
    public List<String> getPrefixes()
    {
        return Collections.unmodifiableList(messagePrefixes);
    }

    // Mutators

    /**
     * 'Smart' setter; ensures that the username notification's trigger is also
     * set to the specified username.
     * @param pUsername The user's in-game name.
     */
    public void setUsername(String pUsername)
    {
        username = pUsername;
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
                null, false, false, false, false, false, 1f, 1f,
                DEFAULT_SOUND, false));
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

    /**
     * Sets the prefix at the specified index to the specified value, if the
     * index is valid and the prefix does not already exist.
     * @param index The index of the prefix to set.
     * @param prefix The new prefix String.
     */
    public void setPrefix(int index, String prefix)
    {
        if (index >= 0 && index < this.messagePrefixes.size() &&
                !this.messagePrefixes.contains(prefix)) {
            this.messagePrefixes.set(index, prefix);
        }
    }

    /**
     * Adds the specified prefix, if it does not already exist.
     * @param prefix The prefix String to add.
     */
    public void addPrefix(String prefix)
    {
        if (!this.messagePrefixes.contains(prefix)) {
            this.messagePrefixes.add(prefix);
        }
    }

    /**
     * Removes the prefix at the specified index, if the index is not 0 and
     * not out of range.
     * @param index The index of the prefix to remove.
     */
    public void removePrefix(int index)
    {
        if (index >= 0 && index < this.messagePrefixes.size()) {
            this.messagePrefixes.remove(index);
        }
    }

    // Other processing

    /**
     * Removes all empty message prefixes, removes all non-persistent
     * notifications with empty primary triggers, removes all empty secondary
     * triggers, exclusion triggers, and response messages from the remaining
     * notifications, and disables all notifications that have no enabled
     * controls.
     */
    public void purge()
    {
        messagePrefixes.removeIf(String::isEmpty);
        messagePrefixes.sort(Comparator.comparingInt(String::length).reversed());

        Notification notif;
        Iterator<Notification> iter = notifications.iterator();
        iter.next(); // Skip the first notification.
        while (iter.hasNext()) {
            notif = iter.next();
            if (notif.getTrigger().isBlank() && !notif.persistent) {
                iter.remove();
            }
        }

        for (Notification notif2: notifications) {
            notif2.purgeTriggers();
            notif2.purgeExclusionTriggers();
            notif2.purgeResponseMessages();
            notif2.autoDisable();
        }
    }

    // Save and load

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    private static final Gson LEGACY_GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new ConfigDeserializer())
            .setPrettyPrinting()
            .create();

    public static Config load() {
        return load(DEFAULT_FILE_NAME);
    }

    public static Config load(String name) {
        Path path = getConfigPath(name);
        Config config;

        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(path.toFile())) {
                // Second reader because apparently FileReader.reset() isn't allowed.
                try (FileReader checkReader = new FileReader(path.toFile())) {
                    for (int i = 0; i < 5; i++) {
                        checkReader.read();
                    }
                    Gson formatGson;
                    if (checkReader.read() == 102) {
                        formatGson = GSON;
                    }
                    else {
                        formatGson = LEGACY_GSON;
                        Constants.LOG.info("Config file using legacy format, applying custom deserializer.");
                    }
                    config = formatGson.fromJson(reader, Config.class);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not parse config", e);
            }
        } else {
            config = new Config();
        }

        configPath = path;

        config.writeChanges();

        return config;
    }

    private static Path getConfigPath(String name) {
        return Path.of("config").resolve(name);
    }

    public void writeChanges() {
        Path dir = configPath.getParent();

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            } else if (!Files.isDirectory(dir)) {
                throw new IOException("Not a directory: " + dir);
            }

            // Use a temporary location next to the config's final destination
            Path tempPath = configPath.resolveSibling(configPath.getFileName() + ".tmp");

            // Write the file to our temporary location
            Files.writeString(tempPath, GSON.toJson(this));

            // Atomically replace the old config file (if it exists) with the temporary file
            Files.move(tempPath, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't update config file", e);
        }
    }
}
