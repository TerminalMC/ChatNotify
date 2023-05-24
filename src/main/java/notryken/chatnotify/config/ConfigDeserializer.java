package notryken.chatnotify.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConfigDeserializer implements JsonDeserializer<Config>
{
    /**
     * This deserializer supports serialized Config objects from and
     * including v1.2.0-beta.8. Any errors originating from changes to the
     * Notification object structure since then will cause a default return.
     */
    @Override
    public Config deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext context)
            throws JsonParseException
    {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        boolean ignoreOwnMessages;
        String username;
        ArrayList<Notification> notifications = new ArrayList<>();

        try {
            ignoreOwnMessages =
                    jsonObject.get("ignoreOwnMessages").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            ignoreOwnMessages = false;
        }

        try {
            username = jsonObject.get("username").getAsString();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            username = null;
        }

        try {
            JsonArray notifArray
                    = jsonObject.get("notifications").getAsJsonArray();

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Notification.class,
                    new NotificationDeserializer());
            Gson gson = gsonBuilder.create();

            for (JsonElement je : notifArray) {
                Notification notif = gson.fromJson(je, Notification.class);
                if (notif != null) {
                    notifications.add(notif);
                }
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            notifications.add(Config.DEFAULTNOTIF);
        }

        return new Config(ignoreOwnMessages, username, notifications);
    }
}
