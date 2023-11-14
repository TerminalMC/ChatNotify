package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.sliderwidget.PitchSliderWidget;
import notryken.chatnotify.gui.sliderwidget.VolumeSliderWidget;
import org.jetbrains.annotations.NotNull;

public class SoundConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public SoundConfigListWidget(Minecraft client, int width, int height,
                                 int top, int bottom, int itemHeight,
                                 Screen parent, Component title, Notification notif)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Sound ID")));
        this.addEntry(new Entry.SoundLink(width, notif, client, this));
        this.addEntry(new Entry.SoundField(width, notif, client, this));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Sound Options")));
        this.addEntry(new Entry.SoundConfigVolume(width, notif, this));
        this.addEntry(new Entry.SoundConfigPitch(width, notif, this));
        this.addEntry(new Entry.SoundTest(width, notif, this));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Noteblock Sounds")));
        String[][] noteblockSounds =
                {
                        {"block.note_block.banjo", "Banjo"},
                        {"block.note_block.bass", "Bass"},
                        {"block.note_block.basedrum", "Bass Drum"},
                        {"block.note_block.bell", "Bell"},
                        {"block.note_block.bit", "Bit"},
                        {"block.note_block.chime", "Chime"},
                        {"block.note_block.cow_bell", "Cow Bell"},
                        {"block.note_block.didgeridoo", "Didgeridoo"},
                        {"block.note_block.flute", "Flute"},
                        {"block.note_block.guitar", "Guitar"},
                        {"block.note_block.harp", "Harp"},
                        {"block.note_block.hat", "Hat"},
                        {"block.note_block.iron_xylophone", "Iron Xylophone"},
                        {"block.note_block.pling", "Pling"},
                        {"block.note_block.snare", "Snare"},
                        {"block.note_block.xylophone", "Xylophone"},
                };
        for (String[] s : noteblockSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s[0],
                    s[1]));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Power/Portal Sounds")));
        String[][] powerSounds = new String[][]
                {
                        {"block.beacon.activate", "Beacon Activate"},
                        {"block.beacon.deactivate", "Beacon Deactivate"},
                        {"block.beacon.power_select", "Beacon Power Select"},
                        {"block.conduit.activate", "Conduit Activate"},
                        {"block.conduit.deactivate", "Conduit Deactivate"},
                        {"block.end_portal_frame.fill", "End Portal Eye"},
                        {"block.portal.travel", "Portal Travel"},
                        {"block.portal.trigger", "Portal Trigger"},
                        {"entity.enderman.teleport", "Teleport"},
                        {"item.trident.return", "Trident Return"},
                        {"entity.elder_guardian.curse", "Elder Guardian Curse"},
                        {"entity.warden.sonic_boom", "Warden Sonic Boom"},
                        {"entity.evoker.cast_spell", "Evoker Cast Spell"},
                        {"entity.evoker.prepare_summon", "Evoker Summon"},
                        {"entity.evoker.prepare_attack", "Evoker Attack"},
                        {"entity.zombie_villager.converted", "Villager Cured"},
                };
        for (String[] s : powerSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s[0],
                    s[1]));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Explosion Sounds")));
        String[][] explosionSounds = new String[][]
                {
                        {"entity.tnt.primed", "TNT Ignite"},
                        {"entity.generic.explode", "TNT Explode"},
                        {"entity.lightning_bolt.thunder", "Thunder"},
                        {"item.firecharge.use", "Fire Charge"},
                        {"block.fire.extinguish", "Fire Extinguish"},
                        {"entity.firework_rocket.blast", "Firework 1"},
                        {"entity.firework_rocket.large_blast", "Firework 2"},
                        {"entity.firework_rocket.twinkle", "Firework 3"},
                };
        for (String[] s : explosionSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s[0],
                    s[1]));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Illager Sounds")));
        String[][] villagerSounds = new String[][]
                {
                        {"entity.villager.ambient", "Villager"},
                        {"entity.villager.yes", "Villager Yes"},
                        {"entity.villager.no", "Villager No"},
                        {"entity.villager.trade", "Villager Trade"},
                        {"entity.pillager.ambient", "Pillager"},
                        {"entity.vindicator.ambient", "Vindicator"},
                        {"entity.evoker.ambient", "Evoker"},

                };
        for (String[] s : villagerSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s[0],
                    s[1]));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Misc Sounds")));
        String[][] miscSounds = new String[][]
                {
                        {"entity.arrow.hit_player", "Arrow Hit"},
                        {"block.bell.use", "Bell"},
                        {"block.amethyst_block.hit", "Amethyst 1"},
                        {"block.amethyst_cluster.place", "Amethyst 2"},
                        {"entity.allay.item_thrown", "Allay Throw"},
                        {"entity.iron_golem.repair", "Iron Repair"},
                        {"block.anvil.land", "Anvil Land"},
                        {"item.shield.block", "Shield Block"},
                        {"item.shield.break", "Shield Break"},
                        {"entity.player.death", "Player Death"},
                        {"entity.goat.screaming.prepare_ram", "Screaming Goat"},
                        {"ui.button.click", "UI Button Click"},
                };
        for (String[] s : miscSounds) {
            this.addEntry(new Entry.SoundOption(width, notif, this, s[0],
                    s[1]));
        }
    }

    @Override
    public SoundConfigListWidget resize(int width, int height,
                                        int top, int bottom)
    {
        SoundConfigListWidget listWidget = new SoundConfigListWidget(client,
                width, height, top, bottom, itemHeight, parent, title, notif);
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
                new SimpleSoundInstance(notif.getSound(),
                        SoundSource.PLAYERS,
                        notif.soundVolume, notif.soundPitch,
                        SoundInstance.createUnseededRandom(), false, 0,
                        SoundInstance.Attenuation.NONE, 0, 0, 0, true));

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
                      @NotNull Minecraft client,
                      SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                Button linkButton = Button.builder(
                        Component.literal("Sound List"),
                                (button) -> openLink(client))
                        .size(80, 20)
                        .pos(width / 2 - 40, 0)
                        .build();
                linkButton.setTooltip(Tooltip.create(Component.literal("Probably " +
                        "opens a webpage with a list of Minecraft sounds.")));

                options.add(linkButton);
            }

            private void openLink(Minecraft client)
            {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getPlatform().openUri("https://github.com/" +
                                "NotRyken/ChatNotify/blob/master/src/main/" +
                                "resources/assets/chatnotify/SoundList.txt");
                    }
                    listWidget.refreshScreen();
                }, "https://github.com/NotRyken/ChatNotify/blob/master/src/" +
                        "main/resources/assets/chatnotify/SoundList.txt",
                        true));
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

                options.add(Button.builder(Component.literal(
                        "> Click to Test Sound <"), (button) ->
                                listWidget.playNotifSound()).size(240, 20)
                        .pos(width / 2 - 120, 0).build());
            }
        }

        private static class SoundField extends Entry
        {
            SoundField(int width, Notification notif,
                       @NotNull Minecraft client,
                       SoundConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                EditBox soundField = new EditBox(
                        client.font, this.width / 2 - 120, 0, 240, 20,
                        Component.literal("Notification Sound"));
                soundField.setMaxLength(120);
                soundField.setValue(this.notif.getSound().toString());
                soundField.setResponder(this::setSound);

                options.add(soundField);
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
                        String sound, String soundName)
            {
                super(width, notif, listWidget);

                options.add(Button.builder(Component.literal(soundName),
                        (button) -> {
                    notif.setSound(notif.parseSound(sound));
                    listWidget.refreshScreen();
                    listWidget.playNotifSound();
                })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }
    }
}