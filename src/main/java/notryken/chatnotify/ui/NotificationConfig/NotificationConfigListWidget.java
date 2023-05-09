package notryken.chatnotify.ui.NotificationConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.ui.ColorConfig.ColorConfigScreen;
import notryken.chatnotify.ui.SoundConfig.SoundConfigScreen;
import notryken.chatnotify.ui.KeyTriggerConfig.KeyTriggerConfigScreen;
import notryken.chatnotify.ui.TriggerVariationConfig.TriggerVariationConfigScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationConfigListWidget extends
        ElementListWidget<NotificationConfigListWidget.ConfigEntry>
{
    private final Notification notif;
    private final Screen parentScreen;

    public NotificationConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                        int m, Notification notif, Screen parentScreen)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);
        this.notif = notif;
        this.parentScreen = parentScreen;

        this.addEntry(new ConfigEntry.GenericConfigHeader(width, notif, client, this, "Notification Trigger"));
        this.addEntry(new ConfigEntry.TriggerConfigType(width, notif, this));

        if (notif.triggerIsKey) {
            this.addEntry(new ConfigEntry.KeyTriggerConfig(width, notif, this));
        }
        else {
            this.addEntry(new ConfigEntry.TriggerField(width, notif, client, this));
            this.addEntry(new ConfigEntry.TriggerVariationConfig(width, notif, this));
        }

        this.addEntry(new ConfigEntry.GenericConfigHeader(width, notif, client, this, "Message Color"));
        this.addEntry(new ConfigEntry.ColorConfig(width, notif, this));
        this.addEntry(new ConfigEntry.GenericConfigHeader(width, notif, client, this, "Message Format"));
        this.addEntry(new ConfigEntry.FormatConfig(width, notif, this, "Bold", 0));
        this.addEntry(new ConfigEntry.FormatConfig(width, notif, this, "Italic", 1));
        this.addEntry(new ConfigEntry.FormatConfig(width, notif, this, "Underlined", 2));
        this.addEntry(new ConfigEntry.FormatConfig(width, notif, this, "Strikethrough", 3));
        this.addEntry(new ConfigEntry.FormatConfig(width, notif, this, "Obfuscated", 4));
        this.addEntry(new ConfigEntry.ControlConfigHeader(width, notif, client, this, "Notification Sound", 2));
        this.addEntry(new ConfigEntry.SoundConfig(width, notif, this));
    }

    public void refreshScreen()
    {
        if (client != null) {
            client.setScreen(
                    new NotificationConfigScreen(this.parentScreen, this.notif));
        }
    }

    public void openKeyTriggerConfig()
    {
        if (client != null) {
            client.setScreen(
                    new KeyTriggerConfigScreen(client.currentScreen, this.notif));
        }
    }

    public void openTriggerVariationConfig()
    {
        if (client != null) {
            client.setScreen(
                    new TriggerVariationConfigScreen(client.currentScreen, this.notif));
        }
    }

    public void openColorConfig()
    {
        if (client != null) {
            client.setScreen(
                    new ColorConfigScreen(client.currentScreen, this.notif));
        }
    }

    public void openSoundConfig()
    {
        if (client != null) {
            client.setScreen(
                    new SoundConfigScreen(client.currentScreen, this.notif));
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

        public static class GenericConfigHeader extends ConfigEntry
        {
            GenericConfigHeader(int width, Notification notif,
                                @NotNull MinecraftClient client,
                                NotificationConfigListWidget listWidget,
                                String label)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal(label), client.textRenderer));
            }
        }

        public static class TriggerConfigType extends ConfigEntry
        {
            TriggerConfigType(int width, Notification notif,
                              NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder(
                        Text.literal("Event Key"), Text.literal("Word/Phrase"))
                                .initially(notif.triggerIsKey)
                                .build(this.width / 2 - 120, 0, 240, 20,
                                        Text.literal("Type"),
                                        (button, status) -> {
                                    notif.setTriggerIsKey(status);
                                    listWidget.refreshScreen();
                                }));

            }
        }

        public static class KeyTriggerConfig extends ConfigEntry
        {
            KeyTriggerConfig(int width, Notification notif,
                             NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                String label = notif.getTrigger();
                if (label.equals("")) {
                    label = "> Click to Set <";
                }
                else {
                    label = "[Key] " + label;
                }

                this.options.add(ButtonWidget.builder(Text.literal(label),
                                (button) -> listWidget.openKeyTriggerConfig())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        public static class TriggerField extends ConfigEntry {
            TriggerField(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                TextFieldWidget triggerEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Trigger"));
                triggerEdit.setMaxLength(120);
                triggerEdit.setText(this.notif.getTrigger());
                triggerEdit.setChangedListener(this::setTrigger);

                this.options.add(triggerEdit);
            }

            public void setTrigger(String trigger) {
                this.notif.setTrigger(trigger.strip());
            }
        }

        public static class TriggerVariationConfig extends ConfigEntry
        {
            TriggerVariationConfig(int width, Notification notif,
                                   NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(
                                Text.literal("Trigger Variations (" + (notif.getNumTriggers() - 1) + ")"),
                                (button) -> {
                                    if (notif.getTrigger() != null &&
                                            !notif.getTrigger().equals("")) {
                                        listWidget.openTriggerVariationConfig();
                                    }})
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        public static class ControlConfigHeader extends ConfigEntry
        {
            ControlConfigHeader(int width, Notification notif,
                                @NotNull MinecraftClient client,
                                NotificationConfigListWidget listWidget,
                                String label, int index)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 60, 0, 120, 20,
                        Text.literal(label),
                        client.textRenderer));
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .omitKeyText()
                        .initially(notif.getControl(index))
                        .build(this.width / 2 + 60, 0, 30, 20, Text.empty(),
                                (button, status) -> {
                                    notif.setControl(index, status);
                                    listWidget.refreshScreen();
                                }));
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
                        (button) -> listWidget.openColorConfig())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        public static class FormatConfig extends ConfigEntry
        {
            FormatConfig(int width, Notification notif,
                         NotificationConfigListWidget listWidget, String label,
                         int index)
            {
                super(width, notif, listWidget);
                this.options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getFormatControl(index))
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal(label), (button, status) -> {
                                    notif.setFormatControl(index, status);
                                    listWidget.refreshScreen();
                                }));
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
                        (button) -> listWidget.openSoundConfig())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}