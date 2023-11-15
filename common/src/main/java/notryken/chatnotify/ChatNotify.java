package notryken.chatnotify;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import notryken.chatnotify.config.Config;

import java.util.ArrayList;
import java.util.List;

public class ChatNotify
{
    public static Minecraft client = Minecraft.getInstance();
    public static final List<Pair<Long, String>> recentMessages = new ArrayList<>();
    private static Config CONFIG;


    public static void init()
    {
        CONFIG = loadConfig();
    }

    public static Config config() {
        if (CONFIG == null) {
            throw new IllegalStateException("Config not yet available");
        }
        return CONFIG;
    }

    private static Config loadConfig() {
        try {
            return Config.load();
        } catch (Exception e) {
            Constants.LOG.error("Failed to load configuration file", e);
            Constants.LOG.error("Using default configuration file");

            return new Config();
        }
    }

    public static void restoreDefaultConfig() {
        CONFIG = new Config();
        CONFIG.writeChanges();
    }
}