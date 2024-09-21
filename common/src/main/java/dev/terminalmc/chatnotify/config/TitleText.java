package dev.terminalmc.chatnotify.config;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class TitleText {
    public final int version = 1;

    public boolean enabled;
    public int color;
    public @NotNull String text;

    /**
     * Creates a default instance.
     */
    public TitleText() {
        this.enabled = false;
        this.color = 16777215;
        this.text = "";
    }

    /**
     * Not validated, only for use by self-validating deserializer.
     */
    TitleText(boolean enabled, int color, @NotNull String text) {
        this.enabled = enabled;
        this.color = color;
        this.text = text;
    }

    public boolean isEnabled() {
        return enabled && !text.isBlank();
    }

    public static class Deserializer implements JsonDeserializer<TitleText> {
        @Override
        public @Nullable TitleText deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            boolean enabled = obj.get("enabled").getAsBoolean();
            int color = obj.get("color").getAsInt();
            String text = obj.get("text").getAsString();

            // Validation
            if (color < 0 || color > 16777215) throw new JsonParseException("TitleText #1");

            return new TitleText(enabled, color, text);
        }
    }
}
