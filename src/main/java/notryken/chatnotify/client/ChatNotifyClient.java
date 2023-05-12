package notryken.chatnotify.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import notryken.chatnotify.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Manages config file creation/loading.
 */
public class ChatNotifyClient implements ClientModInitializer
{
    public static Config config;
    public static String lastSentMessage;
    private static final File settingsFile =
            new File("config", "chatnotify.json");
    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

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
                config.validate();
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
     * Attempts to write the current config options to a file,overwriting if it
     * already exists.
     */
    public static void saveConfig()
    {
        assert !settingsFile.exists() || (settingsFile.delete());
        try {
            Files.writeString(settingsFile.toPath(), gson.toJson(config));
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }
}