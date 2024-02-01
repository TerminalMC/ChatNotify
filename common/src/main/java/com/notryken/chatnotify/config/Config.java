package com.notryken.chatnotify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.notryken.chatnotify.util.ListUtil;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import com.notryken.chatnotify.config.serialize.ConfigDeserializer;
import com.notryken.chatnotify.config.serialize.GhettoAsciiWriter;
import com.notryken.chatnotify.config.serialize.NotificationDeserializer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * ChatNotify configuration options class with default values and validation.
 * <p>
 * Includes derivative work of code used by
 * <a href="https://github.com/CaffeineMC/sodium-fabric/">Sodium</a>
 */
public class Config {
    // Constants
    public static final String DEFAULT_FILE_NAME = "chatnotify.json";
    public static final String DEFAULT_SOUND_STRING = "block.note_block.bell";
    public static final ResourceLocation DEFAULT_SOUND = ResourceLocation.tryParse(DEFAULT_SOUND_STRING);
    public static final SoundSource DEFAULT_SOUND_SOURCE = SoundSource.PLAYERS;
    public static final TextColor DEFAULT_COLOR = TextColor.fromRgb(16761856);
    public static final Notification DEFAULT_USERNAME_NOTIF = new Notification(
            true, new ArrayList<>(List.of(true, false, true)),
            new ArrayList<>(List.of("Profile name", "Display name")), false, DEFAULT_COLOR,
            new ArrayList<>(List.of(false, false, false, false, false)),
            1f, 1f, DEFAULT_SOUND, false,
            false, new ArrayList<>(), false, new ArrayList<>());
    public static final Notification DEFAULT_BLANK_NOTIF = new Notification(
            true, new ArrayList<>(List.of(true, false, true)), new ArrayList<>(List.of("")),
            false, DEFAULT_COLOR, new ArrayList<>(List.of(false, false, false, false, false)),
            1f, 1f, DEFAULT_SOUND, false,
            false, new ArrayList<>(), false, new ArrayList<>());
    public static final List<String> DEFAULT_PREFIXES = List.of("/shout", "!");

    public static final Gson CONFIG_GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new ConfigDeserializer())
            .setPrettyPrinting()
            .create();
    public static final Gson NOTIFICATION_GSON = new GsonBuilder()
            .registerTypeAdapter(Notification.class, new NotificationDeserializer())
            .setPrettyPrinting()
            .create();


    // Not saved, not modifiable by user
    private static Path configPath;

    // Saved, not modifiable by user
    // 001 is initial version
    // 002 adds display name trigger to username notification
    private final String version = "002";

    // Saved, modifiable by user
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
        notifications.add(DEFAULT_USERNAME_NOTIF);
    }

    /**
     * <b>Note:</b> Not validated.
     */
    public Config(boolean ignoreOwnMessages, SoundSource notifSoundSource,
                  ArrayList<String> messagePrefixes, ArrayList<Notification> notifications) {
        this.ignoreOwnMessages = ignoreOwnMessages;
        this.notifSoundSource = notifSoundSource;
        this.messagePrefixes = messagePrefixes;
        this.notifications = notifications;
    }

    // Username

    /**
     * Sets the first trigger of the username {@code Notification} to the
     * specified name.
     */
    public void setProfileName(String profileName) {
        notifications.get(0).setTrigger(profileName);
    }

    /**
     * Sets the second trigger of the username {@code Notification} to the
     * specified name.
     */
    public void setDisplayName(String displayName) {
        notifications.get(0).setTrigger(1, displayName);
    }

    // Message prefix get and set

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
     * Sets the prefix at the specified index to the specified value.
     * @param index the index of the prefix to set.
     * @param prefix the new prefix {@code String}.
     */
    public void setPrefix(int index, String prefix) {
        if (index >= 0 && index < messagePrefixes.size()) {
            messagePrefixes.set(index, prefix);
        }
    }

    /**
     * Adds the specified prefix.
     * @param prefix the prefix {@code String} to add.
     */
    public void addPrefix(String prefix) {
        messagePrefixes.add(prefix);
    }

    /**
     * Removes the prefix at the specified index, if one exists.
     * @param index the index of the prefix to remove.
     */
    public void removePrefix(int index) {
        if (index >= 0 && index < messagePrefixes.size()) {
            messagePrefixes.remove(index);
        }
    }

    // Notification get and set

    /**
     * @return the size of the {@code Notification} list.
     */
    public int getNumNotifs() {
        return notifications.size();
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

    /**
     * Adds a new {@code Notification} with default values.
     */
    public void addNotif() {
        notifications.add(DEFAULT_BLANK_NOTIF);
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
    public void decreasePriority(int index) {
        if (index > 0 && index < notifications.size() - 1) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index + 1));
            notifications.set(index + 1, temp);
        }
    }

    // Validation

    /**
     * Cleanup and validate all settings and notifications.
     */
    public void validate() {

        // Prefixes
        messagePrefixes.removeIf(String::isBlank);
        ListUtil.removeDuplicates(messagePrefixes);
        messagePrefixes.sort(Comparator.comparingInt(String::length).reversed());

        Notification notif;
        List<Notification> lowPriorityNotifList = new ArrayList<>();
        Iterator<Notification> iterNotifs = notifications.iterator();

        // Username notification
        notif = iterNotifs.next();
        notif.purgeTriggersFrom(2);
        notif.purgeResponseMessages();
        notif.purgeResponseMessages();
        notif.autoDisable();

        // All other notifications
        while (iterNotifs.hasNext()) {
            notif = iterNotifs.next();

            notif.purgeTriggers();
            notif.purgeExclusionTriggers();
            notif.purgeResponseMessages();

            if (notif.getTriggers().size() == 1 &&
                    notif.getTrigger().isBlank() &&
                    notif.getExclusionTriggers().isEmpty() &&
                    notif.getResponseMessages().isEmpty()) {
                iterNotifs.remove();
            }
            else {
                notif.fixKeyTriggerCase();
                notif.autoDisable();
                /*
                Move all notifications activated by the "." (any message) key
                trigger to the low-priority end of the list, so that they do not
                prevent more-specific notifications from activating.
                */
                if (notif.triggerIsKey && notif.getTrigger().equals(".")) {
                    lowPriorityNotifList.add(notif);
                    iterNotifs.remove();
                }
            }
        }
        notifications.addAll(lowPriorityNotifList);
    }

    // Load and save

    /**
     * Attempts to load configuration from the default file.
     */
    public static Config load() {
        return load(DEFAULT_FILE_NAME);
    }

    /**
     * Attempts to load configuration from the specified filename. If
     * unsuccessful, returns default configuration.
     * @param name the configuration file name.
     * @return the configuration loaded from the specified file if possible,
     * default configuration otherwise.
     */
    public static Config load(String name) {
        configPath = Path.of("config").resolve(name);
        Config config;

        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                config = CONFIG_GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse config", e);
            }
        } else {
            config = new Config();
        }

        config.writeChanges();
        return config;
    }

    /**
     * Writes the config to the global configPath.
     */
    public void writeChanges() {
        validate();

        Path dir = configPath.getParent();

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            } else if (!Files.isDirectory(dir)) {
                throw new IOException("Not a directory: " + dir);
            }

            // Use a temporary location next to the config's final destination
            Path tempPath = configPath.resolveSibling(configPath.getFileName() + ".tmp");

            // Write the file to the temporary location
            FileWriter out = new FileWriter(tempPath.toFile());
            CONFIG_GSON.toJson(this, new GhettoAsciiWriter(out));
            out.close();

            // Atomically replace the old config file (if it exists) with the temporary file
            Files.move(tempPath, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to update config file", e);
        }
    }
}
