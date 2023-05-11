package notryken.chatnotify.ui.SoundConfig;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;

public class VolumeSliderWidget extends SliderWidget
{
    private final Notification notif;

    public VolumeSliderWidget(int x, int y, int width, int height, double value,
                              Notification notif)
    {
        super(x, y, width, height, Text.empty(), value);
        this.notif = notif;
        this.updateMessage();
    }

    private double roundVolume()
    {
        return Math.round(this.value * 10) / 10D;
    }

    @Override
    protected void updateMessage()
    {
        double volume = roundVolume();
        Text message = Text.literal("Volume: ").append(
                volume == 0 ? Text.literal("OFF") :
                Text.literal(String.valueOf(volume)));
        this.setMessage(message);
    }

    @Override
    protected void applyValue()
    {
        notif.setSoundVolume((float) roundVolume());
    }
}
