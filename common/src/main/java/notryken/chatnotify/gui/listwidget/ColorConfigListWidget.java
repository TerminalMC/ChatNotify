package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.Util;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import org.jetbrains.annotations.NotNull;

public class ColorConfigListWidget extends ConfigListWidget
{
    public final Notification notif;

    public ColorConfigListWidget(Minecraft client, int width, int height,
                                 int top, int bottom, int itemHeight,
                                 Screen parent, Component title, Notification notif)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Hex Color")));
        this.addEntry(new Entry.ColorLink(width, notif, client, parent, this));
        this.addEntry(new Entry.ColorField(width, notif, client, this));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Quick Colors")));

        // These arrays match 1:1 for the color and its name.
        int[] intColors = new int[]
                {
                        -1,
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
                        "White",
                        "Gray",
                        "Black"
                };

        for (int idx = 0; idx < intColors.length; idx++) {
            this.addEntry(new Entry.ColorOption(width, notif, this,
                    (intColors[idx] == -1 ? null :
                            TextColor.fromRgb(intColors[idx])),
                    strColors[idx]));
        }

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

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif, ColorConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class ColorLink extends Entry
        {
            ColorLink(int width, Notification notif,
                      @NotNull Minecraft client,
                      Screen parentScreen, ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                Button linkButton = Button.builder(
                        Component.literal("color-hex.com"), (button) -> openLink(
                                client, parentScreen, listWidget))
                        .size(80, 20)
                        .pos(width / 2 - 40, 0)
                        .build();
                linkButton.setTooltip(Tooltip.create(Component.literal("Probably " +
                        "opens a webpage with hex color selection.")));

                this.options.add(linkButton);
            }

            private void openLink(Minecraft client, Screen parent,
                                  ColorConfigListWidget listWidget)
            {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getPlatform().openUri(
                                "https://www.color-hex.com/");
                    }
                    client.setScreen(new ConfigScreen(parent, client.options,
                            Component.literal("Notification Highlight Color"),
                            listWidget));
                }, "https://www.color-hex.com/", true));
            }
        }

        private static class ColorField extends Entry
        {
            ColorField(int width, Notification notif,
                       @NotNull Minecraft client,
                       ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                EditBox colorEdit = new EditBox(
                        client.font, this.width / 2 - 120, 0, 240, 20,
                        Component.literal("Hex Color"));
                colorEdit.setMaxLength(7);

                if (this.notif.getColor() != null) {
                    colorEdit.setValue(this.notif.getColor().formatValue());
                }

                colorEdit.setResponder(this::setColor);

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

                MutableComponent message = Component.literal(colorName);

                message.setStyle(Style.EMPTY.withColor(color));

                this.options.add(Button.builder(message, (button) -> {
                    notif.setColor(color);
                    listWidget.refreshScreen();
                })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }
    }
}