package notryken.chatnotify.config.serialize;

import com.google.gson.*;
import net.minecraft.sounds.SoundSource;
import notryken.chatnotify.config.Config;
import notryken.chatnotify.config.Notification;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * {@code Config} deserializer. Backwards-compatible to ChatNotify v1.0.2.
 */
public class ConfigDeserializer implements JsonDeserializer<Config> {
    @Override
    public Config deserialize(JsonElement jsonGuiEventListener, Type type,
                              JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonGuiEventListener.getAsJsonObject();

        boolean ignoreOwnMessages;
        SoundSource notifSoundSource;
        ArrayList<String> messagePrefixes = new ArrayList<>();
        ArrayList<Notification> notifications = new ArrayList<>();

        try {
            ignoreOwnMessages = jsonObject.get("ignoreOwnMessages").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            ignoreOwnMessages = false;
        }

        try {
            String sourceStr = jsonObject.get("notifSoundSource").getAsString();
            notifSoundSource = SoundSource.valueOf(sourceStr);
        }
        catch (JsonParseException | NullPointerException | UnsupportedOperationException |
               IllegalArgumentException | IllegalStateException e) {
            notifSoundSource = Config.DEFAULT_SOUND_SOURCE;
        }


        try {
            JsonArray prefixArray = jsonObject.get("messagePrefixes").getAsJsonArray();
            for (JsonElement je : prefixArray) {
                messagePrefixes.add(je.getAsString());
            }
        } catch (JsonParseException | NullPointerException |
                 UnsupportedOperationException | IllegalStateException e) {
            messagePrefixes = new ArrayList<>(Config.DEFAULT_PREFIXES);
        }

        try {
            JsonArray notifArray = jsonObject.get("notifications").getAsJsonArray();

            for (JsonElement je : notifArray) {
                Notification notif = Config.NOTIFICATION_GSON.fromJson(je, Notification.class);
                if (notif != null) {
                    notifications.add(notif);
                }
            }
            if (notifications.isEmpty()) {
                throw new JsonParseException("Empty notification array.");
            }
            // Display name added in v1.2.0
            if (notifications.get(0).getTriggers().size() == 1) {
                notifications.get(0).addTrigger("Display name");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            notifications = new ArrayList<>();
            notifications.add(Config.DEFAULT_USERNAME_NOTIF);
        }

        return new Config(ignoreOwnMessages, notifSoundSource, messagePrefixes, notifications);
    }
}
