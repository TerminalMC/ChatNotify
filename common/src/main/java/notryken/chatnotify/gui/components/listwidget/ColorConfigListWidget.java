package notryken.chatnotify.gui.components.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.components.button.BlueColorSlider;
import notryken.chatnotify.gui.components.button.GreenColorSlider;
import notryken.chatnotify.gui.components.button.RedColorSlider;

public class ColorConfigListWidget extends ConfigListWidget
{
    public final Notification notif;

    public ColorConfigListWidget(Minecraft client, int width, int height,
                                 int top, int bottom, int itemHeight,
                                 Screen parent, Component title, Notification notif) {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Notification Text Color")
                        .setStyle(Style.EMPTY.withColor(notif.getColor()))));
        this.addEntry(new Entry.RedSlider(width, notif, this));
        this.addEntry(new Entry.GreenSlider(width, notif, this));
        this.addEntry(new Entry.BlueSlider(width, notif, this));

        this.addEntry(new Entry.ColorOption(width, notif, this));

    }

    @Override
    public ColorConfigListWidget resize(int width, int height,
                                        int top, int bottom)
    {
        ColorConfigListWidget listWidget = new ColorConfigListWidget(client,
                width, height, top, bottom, itemHeight, parent, title, notif);
        listWidget.setScrollAmount(this.getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(this);
    }

    public void refreshColorIndicator() {
        this.remove(0);
        this.addEntryToTop(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Notification Text Color")
                        .setStyle(Style.EMPTY.withColor(notif.getColor()))));
    }

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif, ColorConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class RedSlider extends ColorConfigListWidget.Entry {
            RedSlider(int width, Notification notif, ColorConfigListWidget listWidget) {
                super(width, notif, listWidget);
                options.add(new RedColorSlider(width / 2 - 120, 0, 240, 20,
                        RedColorSlider.sliderValue(notif.getRed()), notif, listWidget));
            }
        }

        private static class GreenSlider extends ColorConfigListWidget.Entry {
            GreenSlider(int width, Notification notif, ColorConfigListWidget listWidget) {
                super(width, notif, listWidget);
                options.add(new GreenColorSlider(width / 2 - 120, 0, 240, 20,
                        GreenColorSlider.sliderValue(notif.getGreen()), notif,listWidget));
            }
        }

        private static class BlueSlider extends ColorConfigListWidget.Entry {
            BlueSlider(int width, Notification notif, ColorConfigListWidget listWidget) {
                super(width, notif, listWidget);
                options.add(new BlueColorSlider(width / 2 - 120, 0, 240, 20,
                        BlueColorSlider.sliderValue(notif.getBlue()), notif, listWidget));
            }
        }

        private static class ColorOption extends Entry {
            ColorOption(int width, Notification notif, ColorConfigListWidget listWidget) {
                super(width, notif, listWidget);

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
                    this.options.add(Button.builder(Component.literal("\u2588")
                                            .setStyle(Style.EMPTY.withColor(color)), (button) -> {
                        notif.setColor(color);
                        listWidget.refreshScreen();
                    })
                            .size(buttonWidth, 15)
                            .pos((width / 2) + offset + (buttonWidth * i), 0)
                            .build());
                }
            }
        }
    }
}