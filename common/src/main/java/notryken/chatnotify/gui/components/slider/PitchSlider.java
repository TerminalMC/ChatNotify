package notryken.chatnotify.gui.components.slider;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.config.Notification;

public class PitchSlider extends AbstractSliderButton {
    private final Notification notif;

    public PitchSlider(int x, int y, int width, int height, double value, Notification notif) {
        super(x, y, width, height, Component.empty(), value);
        this.notif = notif;
        this.updateMessage();
    }

    public static double sliderValue(double pitch) {
        return (pitch - 0.5) / 1.5;
    }

    @Override
    protected void updateMessage() {
        Component message = Component.literal("Pitch: ").append(
                Component.literal(String.valueOf(notif.soundPitch)));
        this.setMessage(message);
    }

    @Override
    protected void applyValue() {
        notif.soundPitch =
                (float) (Math.round(10 * (this.value * 1.5 + 0.5)) / 10.0D);
    }
}
