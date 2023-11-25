package notryken.chatnotify.gui.component.slider;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.component.listwidget.ColorConfigListWidget;

public class GreenColorSlider extends AbstractSliderButton
{
    private final Notification notif;
    private final ColorConfigListWidget listWidget;

    public GreenColorSlider(int x, int y, int width, int height, double value, Notification notif,
                            ColorConfigListWidget listWidget) {
        super(x, y, width, height, Component.empty(), value);
        this.notif = notif;
        this.listWidget = listWidget;
        this.updateMessage();
    }

    public static double sliderValue(double value) {
        return (value / 255d);
    }

    @Override
    protected void updateMessage() {
        Component message = Component.literal("Green: ")
                .append(Component.literal(String.valueOf(notif.getGreen())))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(256 * notif.getGreen())));
        this.setMessage(message);
    }

    @Override
    protected void applyValue() {
        notif.setGreen((int) (this.value * 255 + 0.5));
        listWidget.refreshColorIndicator();
    }
}
