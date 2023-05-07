package notryken.chatnotify.ui.SoundConfigScreen;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;

public class PitchSliderWidget extends SliderWidget
{
    private final Notification notif;

    public PitchSliderWidget(int x, int y, int width, int height, double value,
                              Notification notif)
    {
        super(x, y, width, height, Text.empty(), value);
        this.notif = notif;
        this.updateMessage();
    }

    public static double sliderValue(double pitch)
    {
        return (pitch - 0.5) / 1.5;
    }

    @Override
    protected void updateMessage()
    {
        Text message = Text.literal("Pitch: ").append(
                Text.literal(String.valueOf(notif.getSoundPitch())));
        this.setMessage(message);
    }

    @Override
    protected void applyValue()
    {
        notif.setSoundPitch(
                (float) (Math.round(10 * (this.value * 1.5 + 0.5)) / 10.0D));
    }
}
