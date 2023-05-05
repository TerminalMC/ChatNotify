package notryken.chatnotify.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
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
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static String username;
    private static final File settingsFile =
            new File("config", "chatnotify.json");
    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    /**
     * Loads options from the config file, or creates it with default options.
     */
    @Override
    public void onInitializeClient()
    {
        loadConfig();
    }

    /**
     * Loads config from file, or creates the file and loads default values if
     * it does not exist.
     */
    public static void loadConfig()
    {
        if (settingsFile.exists()) {
            try {
                config = gson.fromJson(Files.readString(settingsFile.toPath()),
                        Config.class);
                config.validateOptions(); // Make sure all options are valid.
                if (username != null) {
                    config.getNotification(0).setTrigger(username);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println(e.getMessage());

                if (client.player != null) {
                    String msg = "ChatNotify Error! @" + username +
                            "\n\nSomething went wrong reading the " +
                            "config file, so it was replaced by the current " +
                            "loaded config. This is typically caused by " +
                            "incorrect formatting, so have a look at the " +
                            "error message below in case it helps.\n\n" +
                            e.getMessage() +
                            "\n\nIf you didn't try to modify the config " +
                            "file, you can safely ignore this message.";
                    client.player.sendMessage(Text.literal(msg));
                }
            }
        }
        if (config == null) {
            config = new Config();
        }
        saveConfig();
    }

    /**
     * Saves the current config options to a file, overwriting if it exists.
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