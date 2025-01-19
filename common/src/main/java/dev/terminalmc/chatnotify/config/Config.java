/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import dev.terminalmc.chatnotify.ChatNotify;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Root configuration options class.
 *
 * <p><b>Note:</b> The list of {@link Notification} instances is required to 
 * maintain an instance at index 0 for the user's name. This instance is handled
 * differently in several ways, but is kept in the list for iteration purposes.
 * </p>
 * 
 * <p>The {@code version} field of a config class must be incremented whenever 
 * the json structure of the class is changed, to facilitate correct 
 * deserialization.</p>
 * 
 * <p><b>Note:</b> For enum controls, the default value is the first value of
 * the enum.</p>
 */
public class Config {
    public final int version = 8;
    private static final Path DIR_PATH = Path.of("config");
    private static final String FILE_NAME = ChatNotify.MOD_ID + ".json";
    private static final String BACKUP_FILE_NAME = ChatNotify.MOD_ID + ".unreadable.json";
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new Deserializer())
            .registerTypeAdapter(Notification.class, new Notification.Deserializer())
            .registerTypeAdapter(Sound.class, new Sound.Deserializer())
            .registerTypeAdapter(TextStyle.class, new TextStyle.Deserializer())
            .registerTypeAdapter(Trigger.class, new Trigger.Deserializer())
            .registerTypeAdapter(StyleTarget.class, new StyleTarget.Deserializer())
            .registerTypeAdapter(ResponseMessage.class, new ResponseMessage.Deserializer())
            .setPrettyPrinting()
            .create();

    // Options

    /**
     * Controls how messages are intercepted.
     */
    public DetectionMode detectionMode;
    public enum DetectionMode {
        HUD_KNOWN_TAGS,
        HUD,
        PACKET,
    }

    /**
     * Controls debug logging.
     */
    public DebugMode debugMode;
    public enum DebugMode {
        NONE,
        ALL,
    }

    /**
     * Controls how many {@link Notification}s can be activated.
     */
    public NotifMode notifMode;
    public enum NotifMode {
        ALL_SINGLE_SOUND,
        ALL,
        SINGLE,
    }

    /**
     * Controls message restyling.
     */
    public RestyleMode restyleMode;
    public enum RestyleMode {
        ALL_INSTANCES,
        SINGLE,
    }

    /**
     * Controls how {@link ResponseMessage}s are sent.
     */
    public SendMode sendMode;
    public enum SendMode {
        PACKET,
        SCREEN,
    }

    /**
     * Whether messages identified as sent by the user should be able to 
     * activate {@link Notification}s.
     */
    public boolean checkOwnMessages;
    public static final boolean checkOwnMessagesDefault = true;

    /**
     * The sound source (and thus, volume control category) of 
     * {@link Notification} sounds.
     */
    public SoundSource soundSource;
    public static final SoundSource soundSourceDefault = SoundSource.PLAYERS;

    /**
     * The default restyle text color for new {@link Notification} instances.
     */
    public int defaultColor;
    public static final int defaultColorDefault = 0xffc400;

    /**
     * The default restyle text color for new {@link Notification} instances.
     */
    public Sound defaultSound;
    public static final Supplier<Sound> defaultSoundDefault = Sound::new;

    /**
     * The list of prefix strings to be checked when evaluating whether a 
     * message was sent by the user.
     */
    public final List<String> prefixes;
    public static final Supplier<List<String>> prefixesDefault = 
            () -> new ArrayList<>(List.of("/shout", "/me", "!"));

    /**
     * The list of all {@link Notification} instances, guaranteed to contain
     * at least one instance.
     */
    private final List<Notification> notifications;
    private static final Supplier<List<Notification>> notificationsDefault = 
            () -> new ArrayList<>(List.of(Notification.createUser()));

    /**
     * Initializes default configuration.
     */
    public Config() {
        this(
                DetectionMode.values()[0],
                DebugMode.values()[0],
                NotifMode.values()[0],
                RestyleMode.values()[0],
                SendMode.values()[0],
                checkOwnMessagesDefault,
                soundSourceDefault, 
                defaultColorDefault, 
                defaultSoundDefault.get(), 
                prefixesDefault.get(), 
                notificationsDefault.get()
        );
    }

    /**
     * Not validated.
     */
    Config(
            DetectionMode detectionMode, 
            DebugMode debugMode, 
            NotifMode notifMode, 
            RestyleMode restyleMode, 
            SendMode sendMode, 
            boolean checkOwnMessages, 
            SoundSource soundSource, 
            int defaultColor, 
            Sound defaultSound, 
            List<String> prefixes, 
            List<Notification> notifications
    ) {
        this.detectionMode = detectionMode;
        this.debugMode = debugMode;
        this.notifMode = notifMode;
        this.restyleMode = restyleMode;
        this.sendMode = sendMode;
        this.checkOwnMessages = checkOwnMessages;
        this.soundSource = soundSource;
        this.defaultColor = defaultColor;
        this.defaultSound = defaultSound;
        this.prefixes = prefixes;
        this.notifications = notifications;
    }

    // Username

    public Notification getUserNotif() {
        validateUserNotif();
        return notifications.getFirst();
    }

    public void setProfileName(String name) {
        getUserNotif().triggers.getFirst().string = name;
    }

    public void setDisplayName(String name) {
        getUserNotif().triggers.get(1).string = name;
    }

    // Notifications

    /**
     * @return an unmodifiable view of the {@link Notification} list.
     */
    public List<Notification> getNotifs() {
        return Collections.unmodifiableList(notifications);
    }

    /**
     * Adds a new {@link Notification} with default or blank values.
     */
    public void addNotif() {
        notifications.add(Notification.createBlank(
                new Sound(defaultSound), new TextStyle(defaultColor)
        ));
    }

    /**
     * Removes the {@link Notification} at the specified index in the list, if 
     * possible.
     *
     * <p><b>Note:</b> Will fail without error if the specified index is 0.</p>
     * @param index the index of the notification.
     * @return {@code true} if the list was modified, {@code false} otherwise.
     */
    public boolean removeNotif(int index) {
        if (index != 0) {
            notifications.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Removes the {@link Notification} at the source index to the destination 
     * index in the list, if possible, 
     *
     * <p><b>Note:</b> Will fail without error if either index is 0.</p>
     * @param sourceIndex the index of the element to move.
     * @param destIndex the desired final index of the element.
     * @return {@code true} if the list was modified, {@code false} otherwise.
     */
    public boolean changeNotifPriority(int sourceIndex, int destIndex) {
        if (sourceIndex > 0 && destIndex > 0 && sourceIndex != destIndex) {
            notifications.add(destIndex, notifications.remove(sourceIndex));
            return true;
        }
        return false;
    }

    // Instance management

    private static Config instance = null;

    public static Config get() {
        if (instance == null) {
            instance = Config.load();
        }
        return instance;
    }

    public static Config getAndSave() {
        get();
        save();
        return instance;
    }

    public static Config resetAndSave() {
        instance = new Config();
        save();
        return instance;
    }

    // Load and save

    public static @NotNull Config load() {
        Path file = DIR_PATH.resolve(FILE_NAME);
        Config config = null;
        if (Files.exists(file)) {
            config = load(file, GSON);
            if (config == null) {
                backup();
                ChatNotify.LOG.warn("Resetting config");
            }
        }
        return config != null ? config : new Config();
    }

    private static @Nullable Config load(Path file, Gson gson) {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Config.class);
        } catch (Exception e) {
            // Catch Exception as errors in deserialization may not fall under
            // IOException or JsonParseException, but should not crash the game.
            ChatNotify.LOG.error("Unable to load config", e);
            return null;
        }
    }

    private static void backup() {
        try {
            ChatNotify.LOG.warn("Copying {} to {}", FILE_NAME, BACKUP_FILE_NAME);
            if (!Files.isDirectory(DIR_PATH)) Files.createDirectories(DIR_PATH);
            Path file = DIR_PATH.resolve(FILE_NAME);
            Path backupFile = file.resolveSibling(BACKUP_FILE_NAME);
            Files.move(file, backupFile, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            ChatNotify.LOG.error("Unable to copy config file", e);
        }
    }

    public static void save() {
        if (instance == null) return;
        instance.cleanup();
        try {
            if (!Files.isDirectory(DIR_PATH)) Files.createDirectories(DIR_PATH);
            Path file = DIR_PATH.resolve(FILE_NAME);
            Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(tempFile.toFile()), StandardCharsets.UTF_8)) {
                writer.write(GSON.toJson(instance));
            } catch (IOException e) {
                throw new IOException(e);
            }
            Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            ChatNotify.onConfigSaved(instance);
        } catch (IOException e) {
            ChatNotify.LOG.error("Unable to save config", e);
        }
    }
    
    // Cleanup and validation

    /**
     * Cleanup and validation method to be called after config editing and 
     * before saving.
     */
    public void cleanup() {
        // Remove blank prefixes and sort by decreasing length
        prefixes.removeIf(String::isBlank);
        prefixes.sort(Comparator.comparingInt(String::length).reversed());
        
        // Validate username notification
        validateUserNotif();

        // Cleanup notifications and remove any blanks except first
        notifications.removeIf((n) -> {
            n.cleanup();
            return (
                    n != notifications.getFirst()
                    && n.triggers.isEmpty()
                    && n.exclusionTriggers.isEmpty()
                    && n.responseMessages.isEmpty()
            );
        });
    }

    /**
     * Validates the existence of a {@link Notification} at index 0 for the
     * user's name.
     */
    private void validateUserNotif() {
        if (notifications.isEmpty()) {
            ChatNotify.LOG.error("Username notification does not exist! Creating...");
            notifications.add(Notification.createUser());
        } else if (notifications.getFirst().triggers.size() < 2) {
            ChatNotify.LOG.error("Username notification missing triggers! Recreating...");
            notifications.add(Notification.createUser());
        }
    }

    // Deserialization
    
    public static class Deserializer implements JsonDeserializer<Config> {
        @Override
        public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            
            String f = "detectionMode";
            DetectionMode detectionMode = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(DetectionMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString()) 
                        ? DetectionMode.valueOf(obj.get(f).getAsString())
                        : DetectionMode.values()[0] 
                    : DetectionMode.values()[0];

            f = "debugMode";
            DebugMode debugMode = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(DebugMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? DebugMode.valueOf(obj.get(f).getAsString())
                        : DebugMode.values()[0]
                    : DebugMode.values()[0];

            f = "notifMode";
            NotifMode notifMode = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(NotifMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? NotifMode.valueOf(obj.get(f).getAsString())
                        : NotifMode.values()[0]
                    : NotifMode.values()[0];

            f = "restyleMode";
            RestyleMode restyleMode = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(RestyleMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? RestyleMode.valueOf(obj.get(f).getAsString())
                        : RestyleMode.values()[0]
                    : RestyleMode.values()[0];

            f = "sendMode";
            SendMode sendMode = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(SendMode.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? SendMode.valueOf(obj.get(f).getAsString())
                        : SendMode.values()[0]
                    : SendMode.values()[0];

            f = "checkOwnMessages";
            boolean checkOwnMessages = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isBoolean() 
                    ? obj.get(f).getAsBoolean()
                    : checkOwnMessagesDefault;

            f = "soundSource";
            SoundSource soundSource = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isString()
                    ? Arrays.stream(SoundSource.values()).map(Enum::name).toList().contains(obj.get(f).getAsString())
                        ? SoundSource.valueOf(obj.get(f).getAsString())
                        : soundSourceDefault
                    : soundSourceDefault;

            f = "defaultColor";
            int defaultColor = obj.has(f) && obj.get(f).isJsonPrimitive() && obj.get(f).getAsJsonPrimitive().isNumber()
                    ? obj.get(f).getAsNumber().intValue()
                    : defaultColorDefault;
            if (defaultColor < 0 || defaultColor > 16777215) defaultColor = defaultColorDefault;

            f = "defaultSound";
            Sound defaultSound = obj.has(f) && obj.get(f).isJsonObject()
                    ? ctx.deserialize(obj.get(f), Sound.class)
                    : defaultSoundDefault.get();

            f = "prefixes";
            List<String> prefixes = obj.has(f) && obj.get(f).isJsonArray()
                    ? new ArrayList<>(obj.getAsJsonArray(f).asList().stream()
                        .filter((je) -> (je.isJsonPrimitive() && je.getAsJsonPrimitive().isString()))
                        .map(JsonElement::getAsString).toList())
                    : prefixesDefault.get();

            f = "notifications";
            List<Notification> notifications = obj.has(f) && obj.get(f).isJsonArray()
                    ? obj.getAsJsonArray(f).asList().stream()
                        .filter(JsonElement::isJsonObject)
                        .map((je) -> (Notification)ctx.deserialize(je, Notification.class)).toList()
                        .stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new))
                    : notificationsDefault.get();
            if (notifications.isEmpty()) notifications.add(Notification.createUser());
            else if (notifications.getFirst().triggers.size() < 2) notifications.set(0, Notification.createUser());
            
            
            return new Config(detectionMode, debugMode, notifMode, restyleMode, sendMode, 
                    checkOwnMessages, soundSource, defaultColor, defaultSound, prefixes, notifications);
        }
    }
}
