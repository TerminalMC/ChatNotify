package notryken.chatnotify.ui.NotificationConfigScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.ui.ColorConfigScreen.ColorConfigScreen;
import notryken.chatnotify.ui.SoundConfigScreen.SoundConfigScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationConfigListWidget extends
        ElementListWidget<NotificationConfigListWidget.ConfigEntry>
{
    public NotificationConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                        int m, Notification notif)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);

        this.addEntry(new ConfigEntry.TriggerFieldHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.TriggerField(width, notif, client, this));
        this.addEntry(new ConfigEntry.ColorFieldHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.ColorConfig(width, notif, this));
        this.addEntry(new ConfigEntry.FormatConfigHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.FormatConfigBold(width, notif, this));
        this.addEntry(new ConfigEntry.FormatConfigItalic(width, notif, this));
        this.addEntry(new ConfigEntry.FormatConfigUnderlined(width, notif, this));
        this.addEntry(new ConfigEntry.FormatConfigStrikethrough(width, notif, this));
        this.addEntry(new ConfigEntry.FormatConfigObfuscated(width, notif, this));
        this.addEntry(new ConfigEntry.SoundConfigHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundConfigPlay(width, notif, this));
        this.addEntry(new ConfigEntry.SoundFieldHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundConfig(width, notif, this));
    }

    public void openColorConfig(Notification notif)
    {
        if (client != null) {
            client.setScreen(
                    new ColorConfigScreen(client.currentScreen, notif));
        }
    }

    public void openSoundConfig(Notification notif)
    {
        if (client != null) {
            client.setScreen(
                    new SoundConfigScreen(client.currentScreen, notif));
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
            ElementListWidget.Entry<ConfigEntry>
    {
        public List<ClickableWidget> options;
        public Notification notif;
        public NotificationConfigListWidget listWidget;
        public int width;

        ConfigEntry(int width, Notification notif, NotificationConfigListWidget
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

        public static class TriggerFieldHeader extends ConfigEntry
        {
            TriggerFieldHeader(int width, Notification notif,
                               @NotNull MinecraftClient client,
                               NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Trigger"),
                        client.textRenderer));
            }
        }

        public static class TriggerField extends ConfigEntry
        {
            TriggerField(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                TextFieldWidget triggerEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Trigger"));
                triggerEdit.setMaxLength(120);
                triggerEdit.setText(this.notif.getTrigger());
                triggerEdit.setChangedListener(this::setTrigger);

                this.options.add(triggerEdit);
            }

            public void setTrigger(String trigger)
            {
                this.notif.setTrigger(trigger);
            }
        }

        public static class ColorFieldHeader extends ConfigEntry
        {
            ColorFieldHeader(int width, Notification notif,
                             @NotNull MinecraftClient client,
                             NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Message Color"),
                        client.textRenderer));
            }
        }

        public static class ColorConfig extends ConfigEntry
        {
            ColorConfig(int width, Notification notif,
                        NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                MutableText message = MutableText.of(Text.literal(
                        Integer.toHexString(notif.getColor())).getContent());

                Style style = Style.of(
                        Optional.of(TextColor.fromRgb(notif.getColor())),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.empty(),
                        Optional.empty());
                message.setStyle(style);


                this.options.add(ButtonWidget.builder(message,
                        (button) -> listWidget.openColorConfig(notif))
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        public static class FormatConfigHeader extends ConfigEntry
        {
            FormatConfigHeader(int width, Notification notif,
                               @NotNull MinecraftClient client,
                               NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Message Format"),
                        client.textRenderer));
            }
        }

        public static class FormatConfigBold extends ConfigEntry
        {
            FormatConfigBold(int width, Notification notif,
                             NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getBold())
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal("Bold"), (button, status) ->
                                        notif.setBold(status)));
            }
        }

        public static class FormatConfigItalic extends ConfigEntry
        {
            FormatConfigItalic(int width, Notification notif,
                               NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getItalic())
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal("Italic"), (button, status) ->
                                        notif.setItalic(status)));
            }
        }

        public static class FormatConfigUnderlined extends ConfigEntry
        {
            FormatConfigUnderlined(int width, Notification notif,
                                   NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getUnderlined())
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal("Underlined"), (button, status) ->
                                        notif.setUnderlined(status)));
            }
        }

        public static class FormatConfigStrikethrough extends ConfigEntry
        {
            FormatConfigStrikethrough(int width, Notification notif,
                                      NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getStrikethrough())
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal("Strikethrough"), (button, status) ->
                                        notif.setStrikethrough(status)));
            }
        }

        public static class FormatConfigObfuscated extends ConfigEntry
        {
            FormatConfigObfuscated(int width, Notification notif,
                                   NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getObfuscated())
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal("Obfuscated"), (button, status) ->
                                        notif.setObfuscated(status)));
            }
        }

        public static class SoundConfigHeader extends ConfigEntry
        {
            SoundConfigHeader(int width, Notification notif,
                              @NotNull MinecraftClient client,
                              NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Sound Options"),
                        client.textRenderer));
            }
        }

        public static class SoundConfigPlay extends ConfigEntry
        {
            SoundConfigPlay(int width, Notification notif,
                            NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getPlaySound())
                        .build(this.width / 2 - 120, 288, 240, 20,
                                Text.literal("Sound"), (button, status) ->
                                        notif.setPlaySound(status)));
            }
        }

        public static class SoundFieldHeader extends ConfigEntry
        {
            SoundFieldHeader(int width, Notification notif,
                             @NotNull MinecraftClient client,
                             NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Sound"),
                        client.textRenderer));
            }
        }

        public static class SoundConfig extends ConfigEntry
        {
            SoundConfig(int width, Notification notif,
                        NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(
                        Text.literal(notif.getSound().toString()),
                        (button) -> listWidget.openSoundConfig(notif))
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}