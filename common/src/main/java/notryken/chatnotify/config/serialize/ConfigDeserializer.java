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

            Gson gson = new GsonBuilder().registerTypeAdapter(Notification.class,
                    new NotificationDeserializer()).create();

            for (JsonElement je : notifArray) {
                Notification notif = gson.fromJson(je, Notification.class);
                if (notif != null) {
                    notifications.add(notif);
                }
            }
            if (notifications.isEmpty()) {
                throw new JsonParseException("Empty notification array.");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            notifications = new ArrayList<>();
            notifications.add(Config.DEFAULT_NOTIF);
        }

        return new Config(ignoreOwnMessages, notifSoundSource, messagePrefixes, notifications);
    }
}
