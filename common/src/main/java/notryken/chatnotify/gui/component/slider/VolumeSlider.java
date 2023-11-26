package notryken.chatnotify.gui.component.slider;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.config.Notification;

public class VolumeSlider extends AbstractSliderButton {
    private final Notification notif;

    public VolumeSlider(int x, int y, int width, int height, double value, Notification notif) {
        super(x, y, width, height, Component.empty(), value);
        this.notif = notif;
        this.updateMessage();
    }

    private double roundVolume() {
        return Math.round(this.value * 10) / 10D;
    }

    @Override
    protected void updateMessage() {
        double volume = roundVolume();
        Component message = Component.literal("Volume: ").append(
                volume == 0 ? Component.literal("OFF") :
                Component.literal(String.valueOf(volume)));
        this.setMessage(message);
    }

    @Override
    protected void applyValue() {
        notif.setSoundVolume((float) roundVolume());
    }
}
