package notryken.chatnotify.ui.ColorConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Util;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColorConfigListWidget extends
        ElementListWidget<ColorConfigListWidget.ConfigEntry>
{
    private final Notification notif;
    private final Screen parentScreen;

    public ColorConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                 int m, Notification notif, Screen parentScreen)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);
        this.notif = notif;
        this.parentScreen = parentScreen;

        this.addEntry(new ConfigEntry.Header(width, notif, client, this, "Hex Color"));
        this.addEntry(new ConfigEntry.HexColorLink(width, notif, client, parentScreen, this));
        this.addEntry(new ConfigEntry.ColorField(width, notif, client, this));
        this.addEntry(new ConfigEntry.Header(width, notif, client, this, "Quick Colors"));

        // These arrays match 1:1 for the color and its name.
        int[] intColors = new int[]
                {
                        16711680,
                        16753920,
                        16776960,
                        65280,
                        32768,
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
                        "Red",
                        "Orange",
                        "Yellow",
                        "Lime",
                        "Green",
                        "Aqua",
                        "Blue",
                        "Purple",
                        "Magenta",
                        "White",
                        "Gray",
                        "Black"
                };

        for (int idx = 0; idx < intColors.length; idx++) {
            this.addEntry(new ConfigEntry.ColorOption(width, notif,
                    this, intColors[idx], strColors[idx]));
        }
    }

    public void refreshScreen()
    {
        if (client != null) {
            client.setScreen(
                    new ColorConfigScreen(this.parentScreen, this.notif));
        }
    }

    public int getRowWidth()
    {
        return 300;
    }

    protected int getScrollbarPositionX()
    {
        return super.getScrollbarPositionX() + 32;
    }

    public abstract static class ConfigEntry extends Entry<ConfigEntry>
    {
        public List<ClickableWidget> options;
        public Notification notif;
        public ColorConfigListWidget listWidget;
        public int width;

        ConfigEntry(int width, Notification notif, ColorConfigListWidget
                listWidget)
        {
            this.options = new ArrayList<>();
            this.notif = notif;
            this.listWidget = listWidget;
            this.width = width;
        }

        public void render(MatrixStack matrices, int index, int y, int x,
                           int entryWidth, int entryHeight,
                           int mouseX, int mouseY,
                           boolean hovered, float tickDelta)
        {
            this.options.forEach((button) -> {
                button.setY(y);
                button.render(matrices, mouseX, mouseY, tickDelta);
            });
        }

        public List<? extends Element> children()
        {
            return this.options;
        }

        public List<? extends Selectable> selectableChildren()
        {
            return this.options;
        }

        public static class Header extends ConfigEntry
        {
            Header(int width, Notification notif,
                   @NotNull MinecraftClient client,
                   ColorConfigListWidget listWidget,
                   String label)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal(label),
                        client.textRenderer));
            }
        }

        public static class HexColorLink extends ConfigEntry
        {
            HexColorLink(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         Screen parentScreen, ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(
                                Text.literal("color-hex.com"),
                                (button) -> openLink(client, parentScreen))
                        .size(80, 20).position(width / 2 - 40, 0).build());
            }

            private void openLink(MinecraftClient client, Screen parent)
            {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open("https://www.color-hex.com/");
                    }
                    client.setScreen(new ColorConfigScreen(parent, notif));
                }, "https://www.color-hex.com/", true));
            }
        }

        public static class ColorField extends ConfigEntry
        {
            MinecraftClient client;

            ColorField(int width, Notification notif,
                       @NotNull MinecraftClient client,
                       ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.client = client;

                TextFieldWidget colorEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Hex Color"));
                colorEdit.setMaxLength(120);
                colorEdit.setText(Integer.toHexString(this.notif.getColor()));
                colorEdit.setChangedListener(this::setColor);

                this.options.add(colorEdit);
            }

            public void setColor(String color)
            {
                this.notif.setColor(this.notif.parseHexInt(color));
                listWidget.refreshScreen();
            }
        }

        public static class ColorOption extends ConfigEntry
        {
            ColorOption(int width, Notification notif,
                        ColorConfigListWidget listWidget,
                        int intColor, String strColor)
            {
                super(width, notif, listWidget);

                MutableText message = MutableText.of(Text.literal(strColor).
                        getContent());

                Style style = Style.of(
                        Optional.of(TextColor.fromRgb(intColor)),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.empty(),
                        Optional.empty());
                message.setStyle(style);

                this.options.add(ButtonWidget.builder(message, (button) ->
                {
                    notif.setColor(intColor);
                    listWidget.refreshScreen();
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}