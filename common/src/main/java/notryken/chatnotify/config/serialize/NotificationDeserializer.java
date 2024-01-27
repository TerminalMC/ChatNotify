package notryken.chatnotify.config.serialize;

import com.google.gson.*;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import notryken.chatnotify.config.Config;
import notryken.chatnotify.config.Notification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code Notification} deserializer. Backwards-compatible to ChatNotify v1.0.2.
 */
public class NotificationDeserializer implements JsonDeserializer<Notification> {
    /**
     * Uses defaults on field error where possible, returns null otherwise.
     */
    @Override
    public Notification deserialize(JsonElement jsonGuiEventListener, Type type,
                                    JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonGuiEventListener.getAsJsonObject();

        boolean enabled;
        ArrayList<Boolean> controls = new ArrayList<>();
        ArrayList<String> triggers = new ArrayList<>();
        boolean triggerIsKey;
        TextColor color;
        ArrayList<Boolean> formatControls = new ArrayList<>();
        float soundVolume;
        float soundPitch;
        ResourceLocation sound;
        boolean regexEnabled;
        boolean exclusionEnabled;
        ArrayList<String> exclusionTriggers = new ArrayList<>();
        boolean responseEnabled;
        ArrayList<String> responseMessages = new ArrayList<>();

        try {
            enabled = jsonObject.get("enabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            enabled = true;
        }

        try {
            JsonArray controlArray = jsonObject.get("controls").getAsJsonArray();
            if (controlArray.size() != 3) {
                throw new JsonParseException("Invalid array size for controls.");
            }
            for (JsonElement je : controlArray) {
                controls.add(je.getAsBoolean());
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            controls = new ArrayList<>(List.of(true, false, true));
        }

        try {
            JsonArray triggerArray = jsonObject.get("triggers").getAsJsonArray();
            for (JsonElement je : triggerArray) {
                triggers.add(je.getAsString());
            }
            if (triggers.isEmpty()) {
                throw new JsonParseException("Empty trigger array.");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            return null;
        }

        try {
            triggerIsKey = jsonObject.get("triggerIsKey").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            return null;
        }

        try {
            JsonObject colorObj = jsonObject.get("color").getAsJsonObject();
            // NeoForge
            int colorInt = colorObj.get("value").getAsInt();
            if (colorInt < 0 || colorInt > 16777215) {
                throw new JsonParseException("Color int out of range.");
            }
            color = TextColor.fromRgb(colorInt);
        }
        catch (JsonParseException | NullPointerException | UnsupportedOperationException |
               NumberFormatException | IllegalStateException e) {
            try {
                JsonObject colorObj = jsonObject.get("color").getAsJsonObject();
                // Fabric and Quilt
                int colorInt = colorObj.get("field_24364").getAsInt();
                if (colorInt < 0 || colorInt > 16777215) {
                    throw new JsonParseException("Color int out of range.");
                }
                color = TextColor.fromRgb(colorInt);
            }
            catch (JsonParseException | NullPointerException | UnsupportedOperationException |
                   NumberFormatException | IllegalStateException e2) {
                try {
                    JsonObject colorObj = jsonObject.get("color").getAsJsonObject();
                    // Forge
                    int colorInt = colorObj.get("f_131257_").getAsInt();
                    if (colorInt < 0 || colorInt > 16777215) {
                        throw new JsonParseException("Color int out of range.");
                    }
                    color = TextColor.fromRgb(colorInt);
                }
                catch (JsonParseException | NullPointerException | UnsupportedOperationException |
                       NumberFormatException | IllegalStateException e3) {
                    color = Config.DEFAULT_COLOR;
                    controls.set(0, false);
                }
            }
        }

        try {
            JsonArray formatControlArray = jsonObject.get("formatControls").getAsJsonArray();
            if (formatControlArray.size() != 5) {
                throw new JsonParseException("Invalid array size for format controls.");
            }
            for (JsonElement je : formatControlArray) {
                formatControls.add(je.getAsBoolean());
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            formatControls = new ArrayList<>(List.of(false, false, false, false, false));
        }

        try {
            soundVolume = jsonObject.get("soundVolume").getAsFloat();
        }
        catch (JsonParseException | NullPointerException | UnsupportedOperationException |
               IllegalStateException | NumberFormatException e) {
            soundVolume = 1f;
        }

        try {
            soundPitch = jsonObject.get("soundPitch").getAsFloat();
        }
        catch (JsonParseException | NullPointerException | UnsupportedOperationException |
               IllegalStateException | NumberFormatException e) {
            soundPitch = 1f;
        }

        try {
            JsonObject soundObj = jsonObject.get("sound").getAsJsonObject();
            // NeoForge
            String namespace = soundObj.get("namespace").getAsString();
            String identifier = soundObj.get("path").getAsString();
            sound = ResourceLocation.tryParse(namespace + ":" + identifier);
            if (sound == null) {
                sound = ResourceLocation.tryParse("block.note_block.bell");
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            try {
                JsonObject soundObj = jsonObject.get("sound").getAsJsonObject();
                // Fabric and Quilt
                String namespace = soundObj.get("field_13353").getAsString();
                String identifier = soundObj.get("field_13355").getAsString();
                sound = ResourceLocation.tryParse(namespace + ":" + identifier);
                if (sound == null) {
                    sound = ResourceLocation.tryParse("block.note_block.bell");
                }
            }
            catch (JsonParseException | NullPointerException |
                   UnsupportedOperationException | IllegalStateException e2) {
                try {
                    JsonObject soundObj = jsonObject.get("sound").getAsJsonObject();
                    // Forge
                    String namespace = soundObj.get("f_135804_").getAsString();
                    String identifier = soundObj.get("f_135805_").getAsString();
                    sound = ResourceLocation.tryParse(namespace + ":" + identifier);
                    if (sound == null) {
                        sound = ResourceLocation.tryParse("block.note_block.bell");
                    }
                }
                catch (JsonParseException | NullPointerException |
                       UnsupportedOperationException | IllegalStateException e3) {
                    sound = ResourceLocation.tryParse("block.note_block.bell");
                    if (sound == null) {
                        return null;
                    }
                }
            }
        }

        try {
            regexEnabled = jsonObject.get("regexEnabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            return null;
        }

        try {
            JsonArray exclusionTriggerArray = jsonObject.get("exclusionTriggers").getAsJsonArray();
            for (JsonElement je : exclusionTriggerArray) {
                exclusionTriggers.add(je.getAsString());
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            // Pass
        }

        try {
            exclusionEnabled = jsonObject.get("exclusionEnabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            exclusionEnabled = !exclusionTriggers.isEmpty();
        }

        try {
            JsonArray responseMessageArray =
                    jsonObject.get("responseMessages").getAsJsonArray();
            for (JsonElement je : responseMessageArray) {
                responseMessages.add(je.getAsString());
            }
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            // Pass
        }

        try {
            responseEnabled = jsonObject.get("responseEnabled").getAsBoolean();
        }
        catch (JsonParseException | NullPointerException |
               UnsupportedOperationException | IllegalStateException e) {
            responseEnabled = !responseMessages.isEmpty();
        }

        controls.set(1, formatControls.contains(true));

        return new Notification(enabled, controls, triggers, triggerIsKey,
                color, formatControls, soundVolume, soundPitch, sound,
                regexEnabled, exclusionEnabled, exclusionTriggers,
                responseEnabled, responseMessages);
    }
}