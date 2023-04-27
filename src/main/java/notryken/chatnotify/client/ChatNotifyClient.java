package notryken.chatnotify.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import notryken.chatnotify.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ChatNotifyClient implements ClientModInitializer
{
    public static Config config;
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static String username;
    private final File settingsFile = new File("config", "chatnotify.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient()
    {
        if(settingsFile.exists()) {
            try {
                config = gson.fromJson(Files.readString(settingsFile.toPath()),
                        Config.class);
            } catch (IOException | JsonSyntaxException e) {
                System.out.println("Error! Unable to load config. " +
                        "Creating new config.");
                e.printStackTrace();
            }
        }
        if(config == null) {
            config = new Config();
            writeConfig();
        }
    }

    public void writeConfig()
    {
        if(settingsFile.exists()) {
            settingsFile.delete();
        }
        try {
            Files.writeString(settingsFile.toPath(), gson.toJson(config));
        } catch (IOException | SecurityException e)
        {
            e.printStackTrace();
        }
    }
}