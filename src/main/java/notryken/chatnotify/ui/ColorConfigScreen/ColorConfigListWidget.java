package notryken.chatnotify.ui.ColorConfigScreen;

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
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColorConfigListWidget extends
        ElementListWidget<ColorConfigListWidget.ConfigEntry>
{
    public ColorConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                 int m, Notification notif, Screen parent)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);

        this.addEntry(new ConfigEntry.ColorFieldHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.HexColorLink(width, notif, client, parent, this));
        this.addEntry(new ConfigEntry.ColorField(width, notif, client, this));
        this.addEntry(new ConfigEntry.ColorOptionHeader(width, notif, client, this));

        int[] colors = new int[]
                {16711680, 16753920, 16776960, 32768, 255, 8388736};
        for (int c : colors) {
            this.addEntry(new ConfigEntry.ColorOption(width, notif, client, this, parent, c));
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

    public abstract static class ConfigEntry extends
            Entry<ConfigEntry>
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

        public static class ColorFieldHeader extends ConfigEntry
        {
            ColorFieldHeader(int width, Notification notif,
                              @NotNull MinecraftClient client,
                              ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Hex Color"),
                        client.textRenderer));
            }
        }

        public static class HexColorLink extends ConfigEntry
        {
            HexColorLink(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         Screen parent, ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(new PressableTextWidget(width / 2 - 120, 0,
                        240, 12, Text.literal("color-hex.com")
                        .formatted(Formatting.BLUE)
                        .formatted(Formatting.UNDERLINE),
                        (button) -> openLink(client, parent),
                        client.textRenderer));
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
            ColorField(int width, Notification notif,
                       @NotNull MinecraftClient client,
                       ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

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
            }
        }

        public static class ColorOptionHeader extends ConfigEntry
        {
            ColorOptionHeader(int width, Notification notif,
                               @NotNull MinecraftClient client,
                               ColorConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Quick Colors"),
                        client.textRenderer));
            }
        }

        public static class ColorOption extends ConfigEntry
        {
            ColorOption(int width, Notification notif, MinecraftClient client,
                        ColorConfigListWidget listWidget, Screen parent, int color)
            {
                super(width, notif, listWidget);

                String strColor = switch (color) {
                    case 16711680 -> "Red";
                    case 16753920 -> "Orange";
                    case 16776960 -> "Yellow";
                    case 32768 -> "Green";
                    case 255 -> "Blue";
                    case 8388736 -> "Purple";
                    default -> "Super unique color (real)";
                };

                MutableText message = MutableText.of(Text.literal(strColor).
                        getContent());

                Style style = Style.of(
                        Optional.of(TextColor.fromRgb(color)),
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
                    notif.setColor(color);
                    client.setScreen(new ColorConfigScreen(parent, notif));
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}