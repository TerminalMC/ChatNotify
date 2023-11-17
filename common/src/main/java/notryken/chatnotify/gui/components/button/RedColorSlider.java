package notryken.chatnotify.gui.components.button;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.components.listwidget.ColorConfigListWidget;

public class RedColorSlider extends AbstractSliderButton
{
    private final Notification notif;
    private final ColorConfigListWidget listWidget;

    public RedColorSlider(int x, int y, int width, int height, double value, Notification notif,
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
        Component message = Component.literal("Red: ")
                .append(Component.literal(String.valueOf(notif.getRed())))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(65536 * notif.getRed())));
        this.setMessage(message);
    }

    @Override
    protected void applyValue() {
        notif.setRed((int) (this.value * 255 + 0.5));
        listWidget.refreshColorIndicator();
    }
}
