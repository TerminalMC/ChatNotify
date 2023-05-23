package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.sliderwidget.PitchSliderWidget;
import notryken.chatnotify.gui.sliderwidget.VolumeSliderWidget;
import org.jetbrains.annotations.NotNull;

public class SoundConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public SoundConfigListWidget(MinecraftClient client,
                                 int i, int j, int k, int l, int m,
                                 Screen parent, Text title, Notification notif)
    {
        super(client, i, j, k, l, m, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Sound ID")));
        this.addEntry(new Entry.SoundLink(width, notif, client, this));
        this.addEntry(new Entry.SoundField(width, notif, client, this));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Sound Options")));
        this.addEntry(new Entry.SoundConfigVolume(width, notif, this));
        this.addEntry(new Entry.SoundConfigPitch(width, notif, this));
        this.addEntry(new Entry.SoundTest(width, notif, this));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Quick Sounds")));

        String[] sounds = new String[]
                {
                        "entity.arrow.hit_player",
                        "entity.allay.item_thrown",
                        "item.trident.return",
                        "block.bell.use",
                        "block.amethyst_block.hit",
                        "block.amethyst_cluster.place",
                        "entity.iron_golem.repair",
                        "block.anvil.land",
                        "item.shield.block",
                        "item.shield.break",
                        "ui.button.click",
                        "entity.player.death",
                };
        for (String s : sounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Power/Portal Sounds")));

        String[] powerSounds = new String[]
                {
                        "block.beacon.activate",
                        "block.beacon.deactivate",
                        "block.beacon.power_select",
                        "block.conduit.activate",
                        "block.conduit.deactivate",
                        "block.end_portal_frame.fill",
                        "block.portal.travel",
                        "block.portal.trigger",
                        "entity.elder_guardian.curse",
                        "entity.evoker.prepare_summon",
                        "entity.zombie_villager.converted"
                };

        for (String s : powerSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Explosion Sounds")));

        String[] explosionSounds = new String[]
                {
                        "entity.tnt.primed",
                        "entity.generic.explode",
                        "entity.lightning_bolt.thunder",
                        "item.firecharge.use",
                        "block.fire.extinguish",
                        "entity.firework_rocket.blast",
                        "entity.firework_rocket.large_blast",
                        "entity.firework_rocket.twinkle"
                };

        for (String s : explosionSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Noteblock Sounds")));

        String[] noteblockSounds = new String[]
                {
                        "block.note_block.basedrum",
                        "block.note_block.bass",
                        "block.note_block.bell",
                        "block.note_block.chime",
                        "block.note_block.flute",
                        "block.note_block.guitar",
                        "block.note_block.harp",
                        "block.note_block.hat",
                        "block.note_block.pling",
                        "block.note_block.snare",
                        "block.note_block.xylophone",
                        "block.note_block.iron_xylophone",
                        "block.note_block.cow_bell",
                        "block.note_block.didgeridoo",
                        "block.note_block.bit",
                        "block.note_block.banjo"
                };

        for (String s : noteblockSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Villager Sounds")));

        String[] villagerSounds = new String[]
                {
                        "entity.villager.no",
                        "entity.villager.trade",
                        "entity.villager.yes"
                };

        for (String s : villagerSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s));
        }
    }

    @Override
    public SoundConfigListWidget resize(int width, int height,
                                        int top, int bottom)
    {
        SoundConfigListWidget listWidget =
                new SoundConfigListWidget(client, width, height, top, bottom,
                        itemHeight, parent, title, notif);
        listWidget.setScrollAmount(this.getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(this);
    }

    private void playNotifSound()
    {
        client.getSoundManager().play(
                new PositionedSoundInstance(notif.getSound(),
                        SoundCategory.PLAYERS,
                        notif.soundVolume, notif.soundPitch,
                        SoundInstance.createRandom(), false, 0,
                        SoundInstance.AttenuationType.NONE, 0, 0, 0, true));

    }

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif, SoundConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class SoundLink extends Entry
        {
            SoundLink(int width, Notification notif,
                      @NotNull MinecraftClient client,
                      SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(Text.literal("Sound List"),
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

        private static class SoundConfigVolume extends Entry
        {
            SoundConfigVolume(int width, Notification notif,
                              SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                options.add(new VolumeSliderWidget(width / 2 - 120, 0, 240, 20,
                        notif.soundVolume, notif));
            }
        }

        private static class SoundConfigPitch extends Entry
        {
            SoundConfigPitch(int width, Notification notif,
                             SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                options.add(new PitchSliderWidget(width / 2 - 120, 0, 240, 20,
                        PitchSliderWidget.sliderValue(notif.soundPitch),
                        notif));
            }
        }

        private static class SoundTest extends Entry
        {
            SoundTest(int width, Notification notif,
                      SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(Text.literal(
                        "> Click to Test Sound <"), (button) ->
                                listWidget.playNotifSound()).size(240, 20)
                        .position(width / 2 - 120, 0).build());
            }
        }

        private static class SoundField extends Entry
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

                options.add(triggerEdit);
            }

            private void setSound(String sound)
            {
                notif.setSound(notif.parseSound(sound));
            }
        }

        private static class SoundOption extends Entry
        {
            SoundOption(int width, Notification notif,
                        SoundConfigListWidget listWidget,
                        String sound)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(Text.literal(sound),
                        (button) ->
                {
                    notif.setSound(notif.parseSound(sound));
                    listWidget.refreshScreen();
                    listWidget.playNotifSound();
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}