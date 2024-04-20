/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.notryken.chatnotify.ChatNotify;
import com.notryken.chatnotify.config.serialize.ConfigDeserializer;
import com.notryken.chatnotify.config.serialize.GhettoAsciiWriter;
import com.notryken.chatnotify.config.serialize.LegacyConfigDeserializer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * <b>Note:</b> The list of {@code Notification}s is required to maintain a
 * {@code Notification} at index 0 for the user's name. This
 * {@code Notification} is handled differently in several ways.
 * <p>
 * Includes derivative work of code used by
 * <a href="https://github.com/CaffeineMC/sodium-fabric/">Sodium</a>
 */
public class Config {
    // Constants
    public static final String DEFAULT_FILE_NAME = "chatnotify.json";
    public static final SoundSource DEFAULT_SOUND_SOURCE = SoundSource.PLAYERS;
    public static final List<String> DEFAULT_PREFIXES = List.of("/shout", "!");

    public static final Gson CONFIG_GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new ConfigDeserializer())
            .setPrettyPrinting()
            .create();
    public static final Gson LEGACY_CONFIG_GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new LegacyConfigDeserializer())
            .setPrettyPrinting()
            .create();


    // Not saved, not modifiable by user
    private static Path configPath;

    // Saved, not modifiable by user
    /*
     * Versions prior to 1 are strings "001", "002" and "003".
     */
    private final int version = 2;

    // Saved, modifiable by user
    public boolean mixinEarly;
    public boolean checkOwnMessages;
    public boolean debugShowKey;
    public SoundSource soundSource;
    public final ArrayList<String> prefixes;
    private final ArrayList<Notification> notifications;


    public Config() {
        mixinEarly = false;
        checkOwnMessages = true;
        debugShowKey = false;
        soundSource = DEFAULT_SOUND_SOURCE;
        prefixes = new ArrayList<>(DEFAULT_PREFIXES);
        notifications = new ArrayList<>();
        notifications.add(Notification.createUserNotification());
    }

    public Config(boolean mixinEarly, boolean checkOwnMessages, boolean debugShowKey,
                  SoundSource soundSource, ArrayList<String> prefixes, ArrayList<Notification> notifications) {
        this.mixinEarly = mixinEarly;
        this.checkOwnMessages = checkOwnMessages;
        this.debugShowKey = debugShowKey;
        this.soundSource = soundSource;
        this.prefixes = prefixes;
        this.notifications = notifications;
    }


    // Username

    public Notification getUserNotification() {
        return notifications.get(0);
    }

    public void setProfileName(String name) {
       getUserNotification().triggers.get(0).string = name;
    }

    public void setDisplayName(String name) {
        getUserNotification().triggers.get(1).string = name;
    }

    // Notifications

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
        notifications.add(Notification.createBlankNotification());
    }

    /**
     * Removes the {@code Notification} at the specified index.
     * <p>
     * <b>Note:</b> Will not remove the {@code Notification} at index 0.
     * @param index the index of the notification.
     * @return {@code true} if a {@code Notification} was removed, {@code false}
     * otherwise.
     */
    public boolean removeNotif(int index) {
        if (index != 0) {
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
        if (index > 1) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index - 1));
            notifications.set(index - 1, temp);
        }
    }

    public void toMaxPriority(int index) {
        if (index > 1) {
            notifications.add(1, notifications.get(index));
            notifications.remove(index + 1);
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
        if (index < notifications.size() - 1) {
            Notification temp = notifications.get(index);
            notifications.set(index, notifications.get(index + 1));
            notifications.set(index + 1, temp);
        }
    }

    public void toMinPriority(int index) {
        if (index < notifications.size() - 1) {
            notifications.add(notifications.get(index));
            notifications.remove(index);
        }
    }

    // Validation

    /**
     * Cleanup and validate all settings and {@code Notification}s.
     */
    public void validate() {

        // Prefixes
        prefixes.removeIf(String::isBlank);
        prefixes.sort(Comparator.comparingInt(String::length).reversed());

        Notification notif;
        Iterator<Notification> iterNotifs = notifications.iterator();

        // Username notification
        notif = iterNotifs.next();
        notif.purgeTriggers();
        notif.purgeExclusionTriggers();
        notif.purgeResponseMessages();
        notif.autoDisable();

        // All other notifications
        while (iterNotifs.hasNext()) {
            notif = iterNotifs.next();

            notif.purgeTriggers();
            notif.purgeExclusionTriggers();
            notif.purgeResponseMessages();

            if (notif.triggers.isEmpty() &&
                    notif.exclusionTriggers.isEmpty() &&
                    notif.responseMessages.isEmpty()) {
                iterNotifs.remove();
            }
            else {
                notif.autoDisable();
            }
        }
    }

    // Load and save

    public static @NotNull Config load() {
        long time = System.currentTimeMillis();
        ChatNotify.LOG.info("ChatNotify: Loading config from file...");

        Config config = load(DEFAULT_FILE_NAME, CONFIG_GSON);

        if (config == null) {
            ChatNotify.LOG.info("ChatNotify: Loading legacy config from file...");
            config = load(DEFAULT_FILE_NAME, LEGACY_CONFIG_GSON);
        }

        if (config == null) {
            ChatNotify.LOG.info("ChatNotify: Using default configuration");
            config = new Config();
        }

        config.writeToFile();
        ChatNotify.LOG.info("ChatNotify: Configuration loaded in {} ms",
                System.currentTimeMillis() - time);
        return config;
    }

    public static @Nullable Config load(String name, Gson gson) {
        configPath = Path.of("config").resolve(name);
        Config config = null;

        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                config = gson.fromJson(reader, Config.class);
            } catch (Exception e) {
                ChatNotify.LOG.warn("ChatNotify: Unable to load config from file '{}'. Reason:", name, e);
            }
        }
        else {
            ChatNotify.LOG.warn("ChatNotify: Unable to locate config file '{}'", name);
        }
        return config;
    }

    /**
     * Writes the config to the global configPath.
     */
    public void writeToFile() {
        long time = System.currentTimeMillis();
        ChatNotify.LOG.info("ChatNotify: Saving config to file...");

        validate();

        Path dir = configPath.getParent();

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            else if (!Files.isDirectory(dir)) {
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

            ChatNotify.LOG.info("ChatNotify: Configuration saved in {} ms",
                    System.currentTimeMillis() - time);
        }
        catch (IOException e) {
            throw new RuntimeException("ChatNotify: Unable to update config file. Reason:", e);
        }
    }
}
