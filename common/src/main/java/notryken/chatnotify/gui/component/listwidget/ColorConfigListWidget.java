package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.component.slider.RgbChannelSlider;

import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/**
 * {@code ConfigListWidget} containing controls for text color of the
 * specified {@code Notification}.
 */
public class ColorConfigListWidget extends ConfigListWidget {
    public final Notification notif;

    public ColorConfigListWidget(Minecraft client, int width, int height,
                                 int top, int bottom, int itemHeight, Screen parent,
                                 Component title, Notification notif) {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.literal("Notification Text Color")
                        .setStyle(Style.EMPTY.withColor(this.notif.getColor()))));

        Supplier<Integer> colorSource = notif::getColorInt;
        Consumer<Integer> colorDest = notif::setColorInt;


        addEntry(new Entry.RedSlider(this.width, this.notif, this));
        addEntry(new Entry.GreenSlider(this.width, this.notif, this));
        addEntry(new Entry.BlueSlider(this.width, this.notif, this));

        addEntry(new Entry.ColorOption(this.width, this.notif, this));
    }

    @Override
    public ColorConfigListWidget resize(int width, int height, int top, int bottom) {
        ColorConfigListWidget listWidget = new ColorConfigListWidget(
                client, width, height, top, bottom, itemHeight, parent, title, notif);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reload() {
        reload(this);
    }

    public void refreshColorIndicator() {
        remove(0);
        addEntryToTop(new ConfigListWidget.Entry.Header(width, client,
                Component.literal("Notification Text Color")
                        .setStyle(Style.EMPTY.withColor(notif.getColor()))));
    }

    private abstract static class Entry extends ConfigListWidget.Entry {

        protected static class RgbSliderEntry extends ColorConfigListWidget.Entry {
            RgbChannelSlider rgbaSlider;
            public RgbSliderEntry(ColorConfigListWidget list, int x, int y, int width, int height, String label,
                                   Supplier<Integer>source, Consumer<Integer> dest,
                                   IntUnaryOperator toChannel, IntUnaryOperator fromChannel) {
                super();
                rgbaSlider = new RgbChannelSlider(x, y, width, height, label,
                        source, dest, toChannel, fromChannel);
                elements.add(rgbaSlider);
            }

            public void refresh() {
                rgbaSlider.refresh();
            }
        }

        private static class RedSlider extends ColorConfigListWidget.Entry {
            RedSlider(int width, Notification notif, ColorConfigListWidget listWidget) {
                super();
                elements.add(new RedColorSlider(width / 2 - 120, 0, 240, 20,
                        RedColorSlider.sliderValue(notif.getRed()), notif, listWidget));
            }
        }

        private static class GreenSlider extends ColorConfigListWidget.Entry {
            GreenSlider(int width, Notification notif, ColorConfigListWidget listWidget) {
                super();
                elements.add(new GreenColorSlider(width / 2 - 120, 0, 240, 20,
                        GreenColorSlider.sliderValue(notif.getGreen()), notif,listWidget));
            }
        }

        private static class BlueSlider extends ColorConfigListWidget.Entry {
            BlueSlider(int width, Notification notif, ColorConfigListWidget listWidget) {
                super();
                elements.add(new BlueColorSlider(width / 2 - 120, 0, 240, 20,
                        BlueColorSlider.sliderValue(notif.getBlue()), notif, listWidget));
            }
        }

        private static class ColorOption extends Entry {
            ColorOption(int width, Notification notif, ColorConfigListWidget listWidget) {
                super();

                // Minecraft's 16 default colors represented in integer form.
                int[] quickColors = new int[] {
                        10027008,
                        16711680,
                        16753920,
                        16761856,
                        16776960,
                        65280,
                        32768,
                        19456,
                        2142890,
                        65535,
                        255,
                        8388736,
                        16711935,
                        16777215,
                        8421504,
                        0};

                int offset = -120;
                int usableWidth = 240;
                int buttonWidth = usableWidth / quickColors.length;

                for (int i = 0; i < quickColors.length; i++) {
                    TextColor color = TextColor.fromRgb(quickColors[i]);
                    elements.add(Button.builder(Component.literal("\u2588")
                                            .setStyle(Style.EMPTY.withColor(color)), (button) -> {
                        notif.setColor(color);
                        listWidget.reload();
                    })
                            .size(buttonWidth, 15)
                            .pos((width / 2) + offset + (buttonWidth * i), 0)
                            .build());
                }
            }
        }
    }
}