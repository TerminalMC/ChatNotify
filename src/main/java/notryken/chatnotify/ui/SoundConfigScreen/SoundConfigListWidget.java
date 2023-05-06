package notryken.chatnotify.ui.SoundConfigScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SoundConfigListWidget extends
        ElementListWidget<SoundConfigListWidget.ConfigEntry>
{
    public SoundConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                 int m, Notification notif, Screen parent)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);

        this.addEntry(new ConfigEntry.SoundFieldHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundLink(width, notif, client, parent, this));
        this.addEntry(new ConfigEntry.SoundField(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundOptionHeader(width, notif, client, this));

        String[] sounds = new String[]{
                "minecraft:entity.experience_orb.pickup",
                "minecraft:block.anvil.land",
                "minecraft:ui.button.click"};
        for (String s : sounds) {
            this.addEntry(new ConfigEntry.SoundOption(width, notif, client, this, parent, s));
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
        public SoundConfigListWidget listWidget;
        public int width;

        ConfigEntry(int width, Notification notif, SoundConfigListWidget
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

        public static class SoundFieldHeader extends ConfigEntry
        {
            SoundFieldHeader(int width, Notification notif,
                             @NotNull MinecraftClient client,
                             SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Sound ID"),
                        client.textRenderer));
            }
        }

        public static class SoundLink extends ConfigEntry
        {
            SoundLink(int width, Notification notif,
                      @NotNull MinecraftClient client,
                      Screen parent, SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(new PressableTextWidget(width / 2 - 120, 0,
                        240, 12, Text.literal("Sound List")
                        .formatted(Formatting.BLUE)
                        .formatted(Formatting.UNDERLINE),
                        (button) -> openLink(client, parent),
                        client.textRenderer));
            }

            private void openLink(MinecraftClient client, Screen parent)
            {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open("https://github.com/NotRyken/ChatNotify/blob/master/src/main/resources/assets/chatnotify/SoundList.txt");
                    }
                    client.setScreen(new SoundConfigScreen(parent, notif));
                }, "https://github.com/NotRyken/ChatNotify/blob/master/src/main/resources/assets/chatnotify/SoundList.txt", true));
            }
        }

        public static class SoundField extends ConfigEntry
        {
            SoundField(int width, Notification notif,
                       @NotNull MinecraftClient client,
                       SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                TextFieldWidget triggerEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Sound"));
                triggerEdit.setMaxLength(120);
                triggerEdit.setText(this.notif.getSound().toString());
                triggerEdit.setChangedListener(this::setSound);

                this.options.add(triggerEdit);
            }

            public void setSound(String sound)
            {
                this.notif.setSound(this.notif.parseSound(sound));
            }
        }

        public static class SoundOptionHeader extends ConfigEntry
        {
            SoundOptionHeader(int width, Notification notif,
                              @NotNull MinecraftClient client,
                              SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Quick Sounds"),
                        client.textRenderer));
            }
        }

        public static class SoundOption extends ConfigEntry
        {
            SoundOption(int width, Notification notif, MinecraftClient client,
                        SoundConfigListWidget listWidget, Screen parent,
                        String sound)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(Text.literal(sound),
                        (button) ->
                {
                    notif.setSound(notif.parseSound(sound));
                    client.setScreen(new SoundConfigScreen(parent, notif));
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}