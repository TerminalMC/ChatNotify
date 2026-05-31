/*
 * Copyright 2026 TerminalMC
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
import dev.terminalmc.chatnotify.config.util.JsonUtil;
import dev.terminalmc.chatnotify.platform.services.PlatformServices;
import dev.terminalmc.chatnotify.util.ResponseUtil;
import dev.terminalmc.chatnotify.util.TimingUtil;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * The root configuration options class. Consists of:
 * <p>
 * - A range of mostly-enum controls for global behavior.
 * <p>
 * - A set of default values for new {@link Notification} instances.
 * <p>
 * - A list of prefix strings for use in message sender detection.
 * <p>
 * - A list of {@link Notification} instances.
 * <p>
 * Note: The {@link Notification} list is required to maintain an instance at index {@code 0} for
 * the user's name. This instance is handled differently in several ways, but is kept in the list
 * for ease of iteration.
 * <p>
 * Note: The {@code VERSION} constant of a config class (e.g. {@link Config#VERSION}) must be
 * incremented whenever the JSON structure of the class is changed, to facilitate correct
 * conditional deserialization.
 * <p>
 * Note: For enum controls without a specified default value, the first value of the enum should be
 * used as the default.
 */
public class Config {

    public static final int VERSION = 9;
    public final int version = VERSION;
    private static final Path CONFIG_DIR = PlatformServices.getInstance().getConfigDir();
    public static final String FILE_NAME = ChatNotify.MOD_ID + ".json";
    public static final String UNREADABLE_FILE_NAME = ChatNotify.MOD_ID + ".unreadable.json";
    public static final String OLD_FILE_NAME = ChatNotify.MOD_ID + ".old.json";
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Config.class, new Deserializer())
            .registerTypeAdapter(Notification.class, new Notification.Deserializer())
            .registerTypeAdapter(Sound.class, new Sound.Deserializer())
            .registerTypeAdapter(TextStyle.class, new TextStyle.Deserializer())
            .registerTypeAdapter(Trigger.class, new Trigger.Deserializer())
            .registerTypeAdapter(StyleTarget.class, new StyleTarget.Deserializer())
            .registerTypeAdapter(Response.class, new Response.Deserializer())
            .setPrettyPrinting()
            .create();

    // Controls

    /**
     * Controls debug logging.
     */
    public DebugMode debugMode;

    public enum DebugMode {
        NONE,
        ALL,
    }

    /**
     * Controls how many notifications can be triggered by a single message.
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
     * Controls how response messages are sent.
     */
    public SendMode sendMode;

    public enum SendMode {
        PACKET,
        SCREEN,
    }

    /**
     * The sound source (and thus, volume control category) of notification sounds.
     */
    public SoundSource soundSource;
    public static final SoundSource soundSourceDefault = SoundSource.PLAYERS;

    // Defaults

    /**
     * The default {@link TextStyle} color for new {@link Notification} instances.
     */
    public int defaultColor;
    public static final int defaultColorDefault = 0xFFffc400;

    /**
     * The default {@link Sound} identifier for new {@link Notification} instances.
     */
    public final Sound defaultSound;
    public static final Supplier<Sound> defaultSoundDefault = Sound::new;

    // Detection

    /**
     * Controls how chat messages are intercepted.
     */
    public ChatDetectionMode detectionMode;

    public enum ChatDetectionMode {
        HUD_KNOWN_TAGS,
        HUD,
        PACKET,
    }

    /**
     * Controls how action bar messages are intercepted.
     */
    public CommonDetectionMode actionBarDetectionMode;

    public enum CommonDetectionMode {
        NONE,
        HUD,
        PACKET,
    }

    /**
     * Controls how title messages are intercepted.
     */
    public CommonDetectionMode titleDetectionMode;

    /**
     * Controls how subtitle messages are intercepted.
     */
    public CommonDetectionMode subtitleDetectionMode;

    /**
     * Controls how messages are identified as sent by the user.
     */
    public SenderDetectionMode senderDetectionMode;

    public enum SenderDetectionMode {
        COMBINED,
        SENT_MATCH,
    }

    /**
     * Whether messages identified as sent by the user should be able to trigger notifications.
     */
    public boolean checkOwnMessages;
    public static final boolean checkOwnMessagesDefault = true;

    /**
     * The list of prefix strings to be checked when evaluating whether a message was sent by the
     * user using the sent-match heuristic.
     */
    public final List<String> prefixes;
    public static final Supplier<List<String>> prefixesDefault =
            () -> new ArrayList<>(List.of("/shout", "/say", "/me", "!"));

    // Notifications

    /**
     * The list of all {@link Notification} instances, guaranteed to contain at least {@code 1}
     * instance.
     */
    private final List<Notification> notifications;
    private static final Supplier<List<Notification>> notificationsDefault =
            () -> new ArrayList<>(List.of(Notification.createUser()));

    /**
     * Initializes default configuration.
     */
    public Config() {
        this(
                DebugMode.values()[0],
                NotifMode.values()[0],
                RestyleMode.values()[0],
                SendMode.values()[0],
                soundSourceDefault,
                defaultColorDefault,
                defaultSoundDefault.get(),
                ChatDetectionMode.values()[0],
                CommonDetectionMode.values()[0],
                CommonDetectionMode.values()[0],
                CommonDetectionMode.values()[0],
                SenderDetectionMode.values()[0],
                checkOwnMessagesDefault,
                prefixesDefault.get(),
                notificationsDefault.get()
        );
    }

    /**
     * Not validated.
     */
    Config(
            DebugMode debugMode,
            NotifMode notifMode,
            RestyleMode restyleMode,
            SendMode sendMode,
            SoundSource soundSource,
            int defaultColor,
            Sound defaultSound,
            ChatDetectionMode detectionMode,
            CommonDetectionMode actionBarDetectionMode,
            CommonDetectionMode titleDetectionMode,
            CommonDetectionMode subtitleDetectionMode,
            SenderDetectionMode senderDetectionMode,
            boolean checkOwnMessages,
            List<String> prefixes,
            List<Notification> notifications
    ) {
        this.debugMode = debugMode;
        this.notifMode = notifMode;
        this.restyleMode = restyleMode;
        this.sendMode = sendMode;
        this.soundSource = soundSource;
        this.defaultColor = defaultColor;
        this.defaultSound = defaultSound;
        this.detectionMode = detectionMode;
        this.actionBarDetectionMode = actionBarDetectionMode;
        this.titleDetectionMode = titleDetectionMode;
        this.subtitleDetectionMode = subtitleDetectionMode;
        this.senderDetectionMode = senderDetectionMode;
        this.checkOwnMessages = checkOwnMessages;
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
                new Sound(defaultSound),
                new TextStyle(defaultColor)
        ));
    }

    /**
     * Removes the {@link Notification} at the specified index in the list, if possible
     * <p>
     * Note: Will fail without error if the index is {@code 0}.
     *
     * @param index the index of the notification.
     * @return {@code true} if the list was modified.
     */
    public boolean removeNotif(int index) {
        if (index != 0) {
            notifications.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Removes the {@link Notification} at the source index to the destination index in the list, if
     * possible.
     * <p>
     * Note: Will fail without error if either index is {@code 0}.
     *
     * @param srcIdx the index of the element to move.
     * @param dstIdx the desired final index of the element.
     * @return {@code true} if the list was modified.
     */
    public boolean moveNotif(int srcIdx, int dstIdx) {
        if (srcIdx > 0 && dstIdx > 0 && srcIdx != dstIdx) {
            notifications.add(dstIdx, notifications.remove(srcIdx));
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

    @SuppressWarnings("UnusedReturnValue")
    public static Config getAndSave() {
        get();
        save();
        return instance;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Config resetAndSave() {
        instance = new Config();
        save();
        return instance;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Config reload() {
        instance = null;
        get();
        ResponseUtil.clear();
        TimingUtil.clear();
        ChatNotify.updateUsernameNotif(instance);
        return instance;
    }

    // Load and save

    public static @NotNull Config load() {
        Path file = CONFIG_DIR.resolve(FILE_NAME);
        Config config = null;
        if (Files.exists(file)) {
            JsonUtil.reset();
            config = load(file, GSON);
            if (config == null) {
                backup(UNREADABLE_FILE_NAME);
                ChatNotify.LOG.warn("Resetting config");
                ChatNotify.hasResetConfig = true;
            } else if (JsonUtil.hasChanged) {
                backup(OLD_FILE_NAME);
            }
        }
        return config != null ? config : new Config();
    }

    @SuppressWarnings("SameParameterValue")
    private static @Nullable Config load(Path file, Gson gson) {
        try (
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(file.toFile()),
                        StandardCharsets.UTF_8
                )
        ) {
            return gson.fromJson(reader, Config.class);
        } catch (Exception e) {
            // Catch Exception as errors in deserialization may not fall under
            // IOException or JsonParseException, but should not crash the game.
            ChatNotify.LOG.error("Unable to load config", e);
            return null;
        }
    }

    private static void backup(String path) {
        try {
            ChatNotify.LOG.warn("Copying {} to {}", FILE_NAME, path);
            if (!Files.isDirectory(CONFIG_DIR))
                Files.createDirectories(CONFIG_DIR);
            Path file = CONFIG_DIR.resolve(FILE_NAME);
            Path backupFile = file.resolveSibling(path);
            Files.move(
                    file,
                    backupFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            ChatNotify.LOG.error("Unable to copy config file", e);
        }
    }

    public static void save() {
        if (instance == null)
            return;
        instance.validate();
        try {
            if (!Files.isDirectory(CONFIG_DIR))
                Files.createDirectories(CONFIG_DIR);
            Path file = CONFIG_DIR.resolve(FILE_NAME);
            Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
            try (
                    OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(tempFile.toFile()),
                            StandardCharsets.UTF_8
                    )
            ) {
                writer.write(GSON.toJson(instance));
            } catch (IOException e) {
                throw new IOException(e);
            }
            Files.move(
                    tempFile,
                    file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
            ChatNotify.onConfigSaved(instance);
        } catch (IOException e) {
            ChatNotify.LOG.error("Unable to save config", e);
        }
    }

    // Validation

    /**
     * Validates this instance. Called after deserialization and before saving.
     */
    private Config validate() {
        // Validate defaults
        defaultColor = validateColor(defaultColor);
        defaultSound.validate();

        // Remove blank prefixes and sort by decreasing length
        prefixes.removeIf(String::isBlank);
        prefixes.sort(Comparator.comparingInt(String::length).reversed());

        // Validate username notification
        validateUserNotif();

        // Validate notifications and remove any blank instances except first
        notifications.removeIf((n) -> {
            n.validate();
            return (n != notifications.getFirst() && n.triggers.isEmpty()
                    && n.exclusionTriggers.isEmpty() && n.responses.isEmpty());
        });

        return this;
    }

    /**
     * Validates the existence of a {@link Notification} at index 0 for the user's name.
     */
    private void validateUserNotif() {
        if (notifications.isEmpty()) {
            ChatNotify.LOG.error("Username notification does not exist! Creating...");
            notifications.add(Notification.createUser());
        } else if (notifications.getFirst().triggers.size() < 2) {
            ChatNotify.LOG.error("Username notification missing triggers! Recreating...");
            notifications.set(0, Notification.createUser());
        }
    }

    /**
     * Converts the color to RGB, if it is ARGB.
     */
    static int validateColor(int color) {
        if (color >= 0 && color <= 0xFFFFFF) {
            // In RGB range; add to ARGB
            return color | 0xFF000000;
        } else {
            // Out of RGB range; return value
            return color;
        }
    }

    // Deserialization

    public static class Deserializer implements JsonDeserializer<Config> {

        @Override
        public Config deserialize(
                JsonElement json,
                java.lang.reflect.Type typeOfT,
                JsonDeserializationContext ctx
        ) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int version = obj.get("version").getAsInt();
            boolean silent = version != VERSION;

            DebugMode debugMode = JsonUtil.getOrDefault(
                    obj,
                    "debugMode",
                    DebugMode.class,
                    DebugMode.values()[0],
                    silent
            );

            NotifMode notifMode = JsonUtil.getOrDefault(
                    obj,
                    "notifMode",
                    NotifMode.class,
                    NotifMode.values()[0],
                    silent
            );

            RestyleMode restyleMode = JsonUtil.getOrDefault(
                    obj,
                    "restyleMode",
                    RestyleMode.class,
                    RestyleMode.values()[0],
                    silent
            );

            SendMode sendMode = JsonUtil.getOrDefault(
                    obj,
                    "sendMode",
                    SendMode.class,
                    SendMode.values()[0],
                    silent
            );

            SoundSource soundSource = JsonUtil.getOrDefault(
                    obj,
                    "soundSource",
                    SoundSource.class,
                    soundSourceDefault,
                    silent
            );

            int defaultColor = JsonUtil.getOrDefault(
                    obj,
                    "defaultColor",
                    defaultColorDefault,
                    silent
            );

            Sound defaultSound = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "defaultSound",
                    Sound.class,
                    defaultSoundDefault.get(),
                    silent
            );

            ChatDetectionMode detectionMode = JsonUtil.getOrDefault(
                    obj,
                    "detectionMode",
                    ChatDetectionMode.class,
                    ChatDetectionMode.values()[0],
                    silent
            );

            CommonDetectionMode actionBarDetectionMode = JsonUtil.getOrDefault(
                    obj,
                    "actionBarDetectionMode",
                    CommonDetectionMode.class,
                    CommonDetectionMode.values()[0],
                    silent
            );

            CommonDetectionMode titleDetectionMode = JsonUtil.getOrDefault(
                    obj,
                    "titleDetectionMode",
                    CommonDetectionMode.class,
                    CommonDetectionMode.values()[0],
                    silent
            );

            CommonDetectionMode subtitleDetectionMode = JsonUtil.getOrDefault(
                    obj,
                    "subtitleDetectionMode",
                    CommonDetectionMode.class,
                    CommonDetectionMode.values()[0],
                    silent
            );

            SenderDetectionMode senderDetectionMode = JsonUtil.getOrDefault(
                    obj,
                    "senderDetectionMode",
                    SenderDetectionMode.class,
                    SenderDetectionMode.values()[0],
                    silent
            );

            boolean checkOwnMessages = JsonUtil.getOrDefault(
                    obj,
                    "checkOwnMessages",
                    checkOwnMessagesDefault,
                    silent
            );

            List<String> prefixes = JsonUtil.getOrDefault(
                    obj,
                    "prefixes",
                    prefixesDefault.get(),
                    silent
            );

            List<Notification> notifications = JsonUtil.getOrDefault(
                    ctx,
                    obj,
                    "notifications",
                    Notification.class,
                    notificationsDefault.get(),
                    silent
            );

            return new Config(
                    debugMode,
                    notifMode,
                    restyleMode,
                    sendMode,
                    soundSource,
                    defaultColor,
                    defaultSound,
                    detectionMode,
                    actionBarDetectionMode,
                    titleDetectionMode,
                    subtitleDetectionMode,
                    senderDetectionMode,
                    checkOwnMessages,
                    prefixes,
                    notifications
            ).validate();
        }
    }
}
