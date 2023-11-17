package notryken.chatnotify.config.deserialize;

import com.google.gson.*;
import notryken.chatnotify.config.Config;
import notryken.chatnotify.config.Notification;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Legacy deserializer. Provides backwards-compatibility to ChatNotify v1.0.x
 * (prior to mojmap conversion).
 */
public class ConfigDeserializer implements JsonDeserializer<Config>
{
    @Override
    public Config deserialize(JsonElement jsonGuiEventListener, Type type,
                              JsonDeserializationContext context)
            throws JsonParseException
    {
        JsonObject jsonObject = jsonGuiEventListener.getAsJsonObject();

        boolean ignoreOwnMessages;
        ArrayList<Notification> notifications = new ArrayList<>();
        ArrayList<String> messagePrefixes = new ArrayList<>();

        try {
            ignoreOwnMessages =
                    jsonObject.get("ignoreOwnMessages").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            ignoreOwnMessages = false;
        }

        try {
            JsonArray notifArray
                    = jsonObject.get("notifications").getAsJsonArray();

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
               UnsupportedOperationException | IllegalStateException e)
        {
            notifications.add(Config.DEFAULT_NOTIF);
        }

        try {
            JsonArray prefixArray = jsonObject.get("messagePrefixes")
                    .getAsJsonArray();

            for (JsonElement je : prefixArray) {
                messagePrefixes.add(je.getAsString());
            }
        } catch (JsonParseException | NullPointerException |
                 UnsupportedOperationException | IllegalStateException e)
        {
            messagePrefixes = new ArrayList<>(Config.DEFAULT_PREFIXES);
        }

        return new Config(ignoreOwnMessages, Config.DEFAULT_SOUND_SOURCE, notifications, messagePrefixes);
    }
}