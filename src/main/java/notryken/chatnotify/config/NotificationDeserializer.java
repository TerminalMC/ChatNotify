package notryken.chatnotify.config;

import com.google.gson.*;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class NotificationDeserializer implements JsonDeserializer<Notification>
{
    /**
     * This deserializer supports serialized Notification objects from and
     * including v1.2.0-beta.8. Any errors originating from changes to the
     * Notification object structure since then will cause a null return.
     */
    @Override
    public Notification deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext context)
            throws JsonParseException
    {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        boolean enabled;
        ArrayList<Boolean> controls = new ArrayList<>();
        ArrayList<String> triggers = new ArrayList<>();
        boolean triggerIsKey;
        TextColor color;
        ArrayList<Boolean> formatControls = new ArrayList<>();
        float soundVolume;
        float soundPitch;
        Identifier sound;
        boolean persistent;
        boolean regexEnabled;
        boolean exclusionEnabled;
        ArrayList<String> exclusionTriggers = new ArrayList<>();
        boolean responseEnabled;
        ArrayList<String> responseMessages = new ArrayList<>();

        // Unchanged since v1.2.0-beta.8

        try {
            enabled = jsonObject.get("enabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            return null;
        }

        try {
            JsonArray controlArray = jsonObject.get("controls").getAsJsonArray();
            if (controlArray.size() != 3) {
                throw new JsonParseException("Invalid array size for controls");
            }
            for (JsonElement je : controlArray) {
                controls.add(je.getAsBoolean());
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            return null;
        }

        try {
            JsonArray triggerArray = jsonObject.get("triggers").getAsJsonArray();
            for (JsonElement je : triggerArray) {
                triggers.add(je.getAsString());
            }
            if (triggers.size() == 0) {
                throw new JsonParseException("Empty trigger array.");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            return null;
        }

        try {
            triggerIsKey = jsonObject.get("triggerIsKey").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            return null;
        }

        try {
            JsonArray formatControlArray =
                    jsonObject.get("formatControls").getAsJsonArray();
            if (formatControlArray.size() != 5) {
                throw new JsonParseException("Invalid array size for format " +
                        "controls");
            }
            for (JsonElement je : formatControlArray) {
                formatControls.add(je.getAsBoolean());
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            return null;
        }

        try {
            soundVolume = jsonObject.get("soundVolume").getAsFloat();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException |
               NumberFormatException e)
        {
            return null;
        }

        try {
            soundPitch = jsonObject.get("soundPitch").getAsFloat();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException |
               NumberFormatException e)
        {
            return null;
        }

        try {
            JsonObject colorObj = jsonObject.get("sound").getAsJsonObject();
            String namespace = colorObj.get("field_13353").getAsString();
            String identifier = colorObj.get("field_13355").getAsString();
            sound = Identifier.tryParse(namespace + ":" + identifier);
            if (sound == null)
            {
                sound = Identifier.tryParse("entity.arrow.hit_player");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            return null;
        }

        try {
            persistent = jsonObject.get("persistent").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            return null;
        }

        // Changed since v1.2.0-beta.8

        try {
            JsonObject colorObj = jsonObject.get("color").getAsJsonObject();
            int colorInt = colorObj.get("field_24364").getAsInt();
            if (colorInt < 0 || colorInt > 16777215) {
                throw new JsonParseException("Color int out of range");
            }
            color = TextColor.fromRgb(colorInt);
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            try {
                int colorInt = jsonObject.get("color").getAsInt();
                if (colorInt < 0 || colorInt > 16777215) {
                    throw new JsonParseException("Color int out of range");
                }
                color = TextColor.fromRgb(colorInt);
            }
            catch (JsonParseException | NullPointerException |
                   UnsupportedOperationException | IllegalStateException |
                   NumberFormatException e2)
            {
                color = null;
                formatControls.set(0, false); // Turn color off.
            }
        }

        try {
            regexEnabled = jsonObject.get("regexEnabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            regexEnabled = false;
        }

        try {
            JsonArray exclusionTriggerArray =
                    jsonObject.get("exclusionTriggers").getAsJsonArray();
            for (JsonElement je : exclusionTriggerArray) {
                exclusionTriggers.add(je.getAsString());
            }
            if (exclusionTriggers.size() == 0) {
                throw new JsonParseException("Empty exclusion trigger array.");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            // Default to empty array.
        }

        try {
            exclusionEnabled = jsonObject.get("exclusionEnabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            exclusionEnabled = !exclusionTriggers.isEmpty();
        }

        try {
            JsonArray responseMessageArray =
                    jsonObject.get("responseMessages").getAsJsonArray();
            for (JsonElement je : responseMessageArray) {
                responseMessages.add(je.getAsString());
            }
            if (responseMessages.size() == 0) {
                throw new JsonParseException("Empty response message array.");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            // Default to empty array.
        }

        try {
            responseEnabled = jsonObject.get("responseEnabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e)
        {
            responseEnabled = !responseMessages.isEmpty();
        }


        return new Notification(enabled, controls, triggers, triggerIsKey,
                color, formatControls, soundVolume, soundPitch, sound,
                persistent, regexEnabled, exclusionEnabled, exclusionTriggers,
                responseEnabled, responseMessages);
    }
}