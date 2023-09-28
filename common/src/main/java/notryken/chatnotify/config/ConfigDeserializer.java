package notryken.chatnotify.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConfigDeserializer implements JsonDeserializer<Config>
{
    @Override
    public Config deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext context)
            throws JsonParseException
    {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        boolean ignoreOwnMessages;
        String username;
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
            username = jsonObject.get("username").getAsString();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            username = null;
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
            notifications.add(Config.DEFAULTNOTIF);
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
            messagePrefixes = new ArrayList<>(Config.DEFAULTPREFIXES);
        }

        return new Config(ignoreOwnMessages, username, notifications,
                messagePrefixes);
    }
}
