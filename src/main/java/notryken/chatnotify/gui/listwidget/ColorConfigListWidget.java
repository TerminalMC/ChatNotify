package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Util;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ColorConfigListWidget extends ConfigListWidget
{
    public Notification notif;

    public ColorConfigListWidget(MinecraftClient client,
                                 int i, int j, int k, int l, int m,
                                 Screen parent, Text title, Notification notif)
    {
        super(client, i, j, k, l, m, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Hex Color")));
        this.addEntry(new Entry.HexColorLink(
                width, notif, client, parent, this));
        this.addEntry(new Entry.ColorField(
                width, notif, client, this));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Quick Colors")));

        // These arrays match 1:1 for the color and its name.
        int[] intColors = new int[]
                {
                        16777215,
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
                        8421504,
                        0
                };
        String[] strColors = new String[]
                {
                        "[No Color]",
                        "Dark Red",
                        "Red",
                        "Orange",
                        "Gold",
                        "Yellow",
                        "Lime",
                        "Green",
                        "Dark Green",
                        "Aqua",
                        "Cyan",
                        "Blue",
                        "Purple",
                        "Magenta",
                        "Gray",
                        "Black"
                };

        for (int idx = 0; idx < intColors.length; idx++) {
            this.addEntry(new Entry.ColorOption(width, notif,
                    this, TextColor.fromRgb(intColors[idx]), strColors[idx]));
        }

    }

    @Override
    public ColorConfigListWidget resize(int width, int height,
                                        int top, int bottom)
    {
        ColorConfigListWidget listWidget =
                new ColorConfigListWidget(client, width, height, top, bottom,
                        itemHeight, parent, title, notif);
        listWidget.setScrollAmount(this.getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(this);
    }

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif, ColorConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class HexColorLink extends Entry
        {
            HexColorLink(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         Screen parentScreen, ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(
                                Text.literal("color-hex.com"),
                                (button) -> openLink(client, parentScreen,
                                        listWidget))
                        .size(80, 20).position(width / 2 - 40, 0).build());
            }

            private void openLink(MinecraftClient client, Screen parent,
                                  ColorConfigListWidget listWidget)
            {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open(
                                "https://www.color-hex.com/");
                    }
                    client.setScreen(new ConfigScreen(parent, client.options,
                            Text.literal("Notification Highlight Color"),
                            listWidget));
                }, "https://www.color-hex.com/", true));
            }
        }

        private static class ColorField extends Entry
        {

            ColorField(int width, Notification notif,
                       @NotNull MinecraftClient client,
                       ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                TextFieldWidget colorEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Hex Color"));
                colorEdit.setMaxLength(7);
                colorEdit.setText(this.notif.getColor().getHexCode());
                colorEdit.setChangedListener(this::setColor);

                this.options.add(colorEdit);
            }

            private void setColor(String color)
            {
                this.notif.setColor(this.notif.parseColor(color));
            }
        }

        private static class ColorOption extends Entry
        {
            ColorOption(int width, Notification notif,
                        ColorConfigListWidget listWidget,
                        TextColor color, String colorName)
            {
                super(width, notif, listWidget);

                MutableText message = Text.literal(colorName);

                message.setStyle(Style.of(
                        Optional.of(color),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.empty(),
                        Optional.empty()));

                this.options.add(ButtonWidget.builder(message, (button) ->
                {
                    notif.setColor(color);
                    listWidget.refreshScreen();

                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}