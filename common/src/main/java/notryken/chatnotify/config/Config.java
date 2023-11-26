package notryken.chatnotify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import notryken.chatnotify.config.deserialize.ConfigDeserializer;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Configuration options class with defaults. Loosely based on the design used
 * by <a href="https://github.com/CaffeineMC/sodium-fabric">Sodium</a>.
 */
public class Config {
    // Constants
    public static final String DEFAULT_FILE_NAME = "chatnotify.json";
    public static final String DEFAULT_SOUND_STRING = "block.note_block.bell";
    public static final ResourceLocation DEFAULT_SOUND = ResourceLocation.tryParse(DEFAULT_SOUND_STRING);
    public static final SoundSource DEFAULT_SOUND_SOURCE = SoundSource.PLAYERS;
    public static final TextColor DEFAULT_COLOR = TextColor.fromRgb(16761856);
    public static final Notification DEFAULT_NOTIF = new Notification(
            true, new ArrayList<>(List.of(true, false, true)), new ArrayList<>(List.of("username")),
            false, DEFAULT_COLOR, new ArrayList<>(List.of(false, false, false, false, false)),
            1f, 1f, DEFAULT_SOUND, true, false,
            false, new ArrayList<>(), false, new ArrayList<>());
    public static final List<String> DEFAULT_PREFIXES = List.of("/shout", "!");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new ConfigDeserializer())
            .setPrettyPrinting().create();

    // Not saved, not user-accessible
    private static String username;
    private static Path configPath;

    // Saved, not user-accessible
    private final String version = "001";

    // Saved, user-accessible
    public boolean ignoreOwnMessages;
    public SoundSource notifSoundSource;
    private final ArrayList<String> messagePrefixes;
    private final ArrayList<Notification> notifications;

    /**
     * Default constructor. Creates one {@code Notification} for the user's
     * in-game name.
     */
    public Config() {
        ignoreOwnMessages = false;
        notifSoundSource = DEFAULT_SOUND_SOURCE;
        messagePrefixes = new ArrayList<>(DEFAULT_PREFIXES);
        notifications = new ArrayList<>();
        notifications.add(DEFAULT_NOTIF);
    }

    /**
     * Parameterized constructor.
     */
    public Config(boolean ignoreOwnMessages, SoundSource notifSoundSource,
                  ArrayList<String> messagePrefixes, ArrayList<Notification> notifications) {
        this.ignoreOwnMessages = ignoreOwnMessages;
        this.notifSoundSource = notifSoundSource;
        this.messagePrefixes = messagePrefixes;
        this.notifications = notifications;
    }

    // Config load and save

    public static Config load() {
        return load(DEFAULT_FILE_NAME);
    }

    public static Config load(String name) {
        Path path = Path.of("config").resolve(name);
        Config config;

        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(path.toFile())) {
                config = GSON.fromJson(reader, Config.class);
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

    // Accessors

    /**
     * @return the prefix at the specified index, or null if none exists.
     */
    public String getPrefix(int index) {
        return (index >= 0 && index < messagePrefixes.size()) ? messagePrefixes.get(index) : null;
    }

    /**
     * @return an unmodifiable view of the prefix list.
     */
    public List<String> getPrefixes() {
        return Collections.unmodifiableList(messagePrefixes);
    }

    /**
     * @return the size of the {@code Notification} list.
     */
    public int getNumNotifs() {
        return this.notifications.size();
    }

    /**
     * @return the {@code Notification} at the specified index, or {@code null}
     * if none exists.
     * <p>
     * <b>Note:</b> Username {@code Notification} is located at index 0.
     */
    public Notification getNotif(int index) {
        return (index >= 0 && index < notifications.size()) ? notifications.get(index) : null;
    }

    /**
     * @return an unmodifiable view of the {@code Notification} list.
     */
    public List<Notification> getNotifs() {
        return Collections.unmodifiableList(notifications);
    }

    // Mutators

    /**
     * Setter, also sets the first trigger of the username {@code Notification}
     * to the specified username.
     * <b>Note:</b> Will fail without error if the specified username is
     * {@code null} or blank.
     * @param pUsername the user's in-game name.
     */
    public void setUsername(String pUsername) {
        if (pUsername != null && !pUsername.isBlank()) {
            username = pUsername;
            validateUsernameNotif();
        }
    }

    /**
     * Validates the username {@code Notification} and sets its first trigger
     * to the value of the stored username, if any.
     */
    public void validateUsernameNotif() {
        Notification notif = notifications.get(0);
        notif.triggerIsKey = false;
        if (username != null) {
            notif.setTrigger(username);
        }
    }

    /**
     * Adds a new {@code Notification} with default values.
     */
    public void addNotif() {
        notifications.add(new Notification(
                true, new ArrayList<>(List.of(true, false, true)), new ArrayList<>(List.of("")),
                false, DEFAULT_COLOR, new ArrayList<>(List.of(false, false, false, false, false)),
                1f, 1f, DEFAULT_SOUND, false, false,
                false, new ArrayList<>(), false, new ArrayList<>()));
    }

    /**
     * Removes the {@code Notification} at the specified index, if it exists.
     * <p>
     * <b>Note:</b> Will not remove the {@code Notification} at index 0.
     * @param index the index of the notification.
     * @return {@code true} if a {@code Notification} was removed, {@code false}
     * otherwise.
     */
    public boolean removeNotif(int index) {
        if (index > 0 && index < notifications.size()) {
            notifications.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Swaps the {@code Notification} at the specified index with the one at
     * the specified index minus one, if possible.
     * <p>
     * This represents moving the {@code Notification} to a higher priority.
     * @param index the current index of the {@code Notification}.
     */
    public void increasePriority(int index) {
        if (index > 1 && index < notifications.size()) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index - 1));
            notifications.set(index - 1, temp);
        }
    }

    /**
     * Swaps the {@code Notification} at the specified index with the one at
     * the specified index plus one, if possible.
     * <p>
     * This represents moving the {@code Notification} to a lower priority.
     * @param index the current index of the {@code Notification}.
     */
    public void reducePriority(int index) {
        if (index > 0 && index < notifications.size() - 1) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index + 1));
            notifications.set(index + 1, temp);
        }
    }

    /**
     * Sets the prefix at the specified index to the specified value, if the
     * index is valid and the prefix does not already exist.
     * @param index the index of the prefix to set.
     * @param prefix the new prefix {@code String}.
     */
    public void setPrefix(int index, String prefix) {
        if (index >= 0 && index < this.messagePrefixes.size() &&
                !this.messagePrefixes.contains(prefix)) {
            this.messagePrefixes.set(index, prefix);
        }
    }

    /**
     * Adds the specified prefix, if it does not already exist.
     * @param prefix the prefix {@code String} to add.
     */
    public void addPrefix(String prefix) {
        if (!this.messagePrefixes.contains(prefix)) {
            this.messagePrefixes.add(prefix);
        }
    }

    /**
     * Removes the prefix at the specified index, if one exists.
     * @param index the index of the prefix to remove.
     */
    public void removePrefix(int index) {
        if (index >= 0 && index < this.messagePrefixes.size()) {
            this.messagePrefixes.remove(index);
        }
    }

    // Other processing

    /**
     * Removes all blank message prefixes and sorts the remainder by decreasing
     * order of length, removes all non-persistent {@code Notification}s with
     * empty primary triggers, removes all empty secondary triggers, exclusion
     * triggers, and response messages from the remaining {@code Notification}s,
     * and disables all {@code Notification}s that have no enabled controls.
     */
    public void purge() {
        messagePrefixes.removeIf(String::isBlank);
        messagePrefixes.sort(Comparator.comparingInt(String::length).reversed());

        Notification notif;
        Iterator<Notification> iter = notifications.iterator();
        iter.next(); // Skip the username notification.
        while (iter.hasNext()) {
            notif = iter.next();
            if (notif.getTrigger().isBlank() && !notif.persistent) {
                iter.remove();
            }
        }

        for (Notification notif2 : notifications) {
            notif2.purgeTriggers();
            notif2.purgeExclusionTriggers();
            notif2.purgeResponseMessages();
            notif2.autoDisable();
        }
    }
}
