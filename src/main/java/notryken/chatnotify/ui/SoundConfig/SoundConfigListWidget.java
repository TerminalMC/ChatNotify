package notryken.chatnotify.ui.SoundConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SoundConfigListWidget extends
        ElementListWidget<SoundConfigListWidget.ConfigEntry>
{
    private final Notification notif;
    private final Screen parentScreen;

    public SoundConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                 int m, Notification notif, Screen parentScreen)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);
        this.notif = notif;
        this.parentScreen = parentScreen;

        this.addEntry(new ConfigEntry.Header(width, notif, client, this, "Sound ID"));
        this.addEntry(new ConfigEntry.SoundLink(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundField(width, notif, client, this));
        this.addEntry(new ConfigEntry.Header(width, notif, client, this, "Quick Sounds"));

        String[] sounds = new String[]
                {
                        "minecraft:entity.experience_orb.pickup",
                        "minecraft:block.anvil.land",
                        "minecraft:block.bell.use",
                        "minecraft:block.amethyst_block.hit",
                        "minecraft:ui.button.click",
                        "minecraft:block.beacon.activate",
                        "minecraft:block.beacon.deactivate",
                        "minecraft:entity.elder_guardian.curse",
                        "minecraft:entity.generic.explode",
                };
        for (String s : sounds) {
            this.addEntry(new ConfigEntry.SoundOption(width, notif, this, s));
        }

        this.addEntry(new ConfigEntry.SoundConfigHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundConfigVolume(width, notif, this));
        this.addEntry(new ConfigEntry.SoundConfigPitch(width, notif, this));

        this.addEntry(new ConfigEntry.SoundTestHeader(width, notif, client, this));
        this.addEntry(new ConfigEntry.SoundTest(width, notif, client, this));
    }

    public void refreshScreen()
    {
        if (client != null) {
            client.setScreen(
                    new SoundConfigScreen(this.parentScreen, this.notif));
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

        public static class Header extends ConfigEntry
        {
            Header(int width, Notification notif,
                   @NotNull MinecraftClient client,
                   SoundConfigListWidget listWidget,
                   String label)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal(label),
                        client.textRenderer));
            }
        }

        public static class SoundLink extends ConfigEntry
        {
            SoundLink(int width, Notification notif,
                      @NotNull MinecraftClient client,
                      SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(
                        Text.literal("Sound List"),
                        (button) -> openLink(client))
                        .size(80, 20).position(width / 2 - 40, 0).build());
            }

            private void openLink(MinecraftClient client)
            {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open("https://github.com/NotRyken/ChatNotify/blob/master/src/main/resources/assets/chatnotify/SoundList.txt");
                    }
                    listWidget.refreshScreen();
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

        public static class SoundOption extends ConfigEntry
        {
            SoundOption(int width, Notification notif,
                        SoundConfigListWidget listWidget,
                        String sound)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(Text.literal(sound),
                        (button) ->
                {
                    notif.setSound(notif.parseSound(sound));
                    listWidget.refreshScreen();
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        public static class SoundConfigHeader extends ConfigEntry
        {
            SoundConfigHeader(int width, Notification notif,
                              @NotNull MinecraftClient client,
                              SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Sound Options"),
                        client.textRenderer));
            }
        }

        public static class SoundConfigVolume extends ConfigEntry
        {
            SoundConfigVolume(int width, Notification notif,
                              SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new VolumeSliderWidget(width / 2 - 120, 0,
                        240, 20, notif.soundVolume, notif));
            }
        }

        public static class SoundConfigPitch extends ConfigEntry
        {
            SoundConfigPitch(int width, Notification notif,
                             SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new PitchSliderWidget(width / 2 - 120, 0,
                        240, 20,
                        PitchSliderWidget.sliderValue(notif.soundPitch),
                        notif));
            }
        }

        public static class SoundTestHeader extends ConfigEntry
        {
            SoundTestHeader(int width, Notification notif,
                              @NotNull MinecraftClient client,
                              SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal("Test Sound"),
                        client.textRenderer));
            }
        }

        public static class SoundTest extends ConfigEntry
        {
            SoundTest(int width, Notification notif, MinecraftClient client,
                      SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(Text.literal(
                        "Click to Play"), (button) ->
                        client.getSoundManager().play(
                                new PositionedSoundInstance(
                                        notif.getSound(),
                                        SoundCategory.PLAYERS,
                                        notif.soundVolume,
                                        notif.soundPitch,
                                        SoundInstance.createRandom(),
                                        false, 0,
                                        SoundInstance.AttenuationType.NONE,
                                        0, 0, 0, true))).size(240, 20)
                        .position(width / 2 - 120, 0).build());
            }
        }
    }
}