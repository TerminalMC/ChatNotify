package notryken.chatnotify.gui.components.slider;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.components.listwidget.ColorConfigListWidget;

public class BlueColorSlider extends AbstractSliderButton
{
    private final Notification notif;
    private final ColorConfigListWidget listWidget;

    public BlueColorSlider(int x, int y, int width, int height, double value, Notification notif,
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
        Component message = Component.literal("Blue: ")
                .append(Component.literal(String.valueOf(notif.getBlue())))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(notif.getBlue())));
        this.setMessage(message);
    }

    @Override
    protected void applyValue() {
        notif.setBlue((int) (this.value * 255 + 0.5));
        listWidget.refreshColorIndicator();
    }
}
