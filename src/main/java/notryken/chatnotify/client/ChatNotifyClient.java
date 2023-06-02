package notryken.chatnotify.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import notryken.chatnotify.config.Config;
import notryken.chatnotify.config.ConfigDeserializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages config file creation/loading.
 */
public class ChatNotifyClient implements ClientModInitializer
{
    public static Config config;
    public static final List<String> recentMessages = new ArrayList<>();
    public static final List<Long> recentMessageTimes = new ArrayList<>();
    private static final File settingsFile =
            new File("config", "chatnotify.json");

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Config.class, new ConfigDeserializer())
            .setPrettyPrinting().create();

    @Override
    public void onInitializeClient()
    {
        loadConfig();
    }

    /**
     * If the config file exists and is readable, loads the config from it,
     * correcting any invalid fields. If it exists but is unreadable, creates a
     * new config with defaults. If it does not exist, creates it with defaults.
     * Finally, saves the validated config to the file.
     */
    public static void loadConfig()
    {
        if (settingsFile.exists()) {
            try {
                config = gson.fromJson(Files.readString(settingsFile.toPath()),
                        Config.class);
            } catch (Exception e) { // Catching Exception to cover all bases.
                System.err.println(e.getMessage());
                config = null;
            }
        }
        if (config == null) {
            config = new Config();
        }
        saveConfig();
    }

    /**
     * Deletes the existing config file if it exists. Then, writes the current
     * config to a new config file.
     */
    public static void saveConfig()
    {
        try {
            Files.writeString(settingsFile.toPath(), gson.toJson(config));
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the existing config file if it exists.
     */
    public static void deleteConfigFile()
    {
        if (settingsFile.exists()) {
            try {
                if (!settingsFile.delete()) {
                    throw new IOException("Unable to delete config file.");
                }
            }
            catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}