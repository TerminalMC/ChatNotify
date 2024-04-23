/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.widget.list;

import com.notryken.chatnotify.config.Config;
import com.notryken.chatnotify.config.Sound;
import com.notryken.chatnotify.gui.widget.button.SilentButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

/**
 * Contains controls for a {@link Sound}.
 */
public class SoundOptionsList extends OptionsList {
    private final Sound sound;

    public SoundOptionsList(Minecraft mc, int width, int height, int top, int bottom,
                            int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                            int scrollWidth, Sound sound) {
        super(mc, width, height, top, bottom, itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth);
        this.sound = sound;

        addEntry(new Entry.SoundFieldEntry(entryX, entryWidth, entryHeight, sound));

        addEntry(new OptionsList.Entry.DoubleSliderEntry(entryX, 0, entryWidth, entryHeight, 0, 1, 2,
                "Volume: ", null, "OFF", null,
                () -> (double)sound.getVolume(),
                (value) -> sound.setVolume(value.floatValue())));

        addEntry(new OptionsList.Entry.DoubleSliderEntry(entryX, 0, entryWidth, entryHeight, 0.5, 2, 2,
                "Pitch: ", null, null, null,
                () -> (double)sound.getPitch(),
                (value) -> sound.setPitch(value.floatValue())));

        addEntry(new OptionsList.Entry.SilentActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("> Click to Test Sound <"), null, -1,
                button -> playNotifSound()));

        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight));

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Noteblock Sounds"), null, -1));
        String[][] noteblockSounds = {
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
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, sound, this, s[0], s[1]));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Power/Portal Sounds"), null, -1));
        String[][] powerSounds = new String[][]{
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
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, sound, this, s[0], s[1]));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Explosion Sounds"), null, -1));
        String[][] explosionSounds = new String[][]{
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
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, sound, this, s[0], s[1]));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Illager Sounds"), null, -1));
        String[][] villagerSounds = new String[][]{
                        {"entity.villager.ambient", "Villager"},
                        {"entity.villager.yes", "Villager Yes"},
                        {"entity.villager.no", "Villager No"},
                        {"entity.villager.trade", "Villager Trade"},
                        {"entity.pillager.ambient", "Pillager"},
                        {"entity.vindicator.ambient", "Vindicator"},
                        {"entity.evoker.ambient", "Evoker"},

                };
        for (String[] s : villagerSounds) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, sound, this, s[0], s[1]));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Misc Sounds"), null, -1));
        String[][] miscSounds = new String[][]{
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
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, sound, this, s[0], s[1]));
        }
    }

    @Override
    public SoundOptionsList resize(int width, int height, int top, int bottom,
                                   int itemHeight, double scrollAmount) {
        SoundOptionsList newListWidget = new SoundOptionsList(
                minecraft, width, height, top, bottom, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth, sound);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private void refreshSoundField() {
        OptionsList.Entry entry = getEntry(0);
        if (entry instanceof Entry.SoundFieldEntry soundFieldEntry) {
            soundFieldEntry.updateValue();
        }
    }

    private void playNotifSound() {
        minecraft.getSoundManager().play(new SimpleSoundInstance(
                sound.getResourceLocation(),
                Config.get().soundSource,
                sound.getVolume(), sound.getPitch(),
                SoundInstance.createUnseededRandom(), false, 0,
                SoundInstance.Attenuation.NONE, 0, 0, 0, true));
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class SoundSourceEntry extends MainOptionsList.Entry {
            SoundSourceEntry(int x, int width, int height) {
                super();
                int spacing = 5;
                int volumeButtonWidth = 20;
                int mainButtonWidth = width - volumeButtonWidth - spacing;

                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                                "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "The sound category determines which of Minecraft's volume control " +
                                        "sliders will affect the notification sound.")))
                        .create(x, 0, mainButtonWidth, height, Component.literal("Sound Category"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(
                                Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        Minecraft.getInstance().screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(Component.literal("Open Minecraft's volume settings")))
                        .pos(x + width - volumeButtonWidth, 0)
                        .size(volumeButtonWidth, height)
                        .build());
            }
        }

        private static class SoundFieldEntry extends Entry {
            private final Sound sound;
            private final EditBox soundField;

            SoundFieldEntry(int x, int width, int height, Sound sound) {
                super();
                this.sound = sound;
                soundField = new EditBox(Minecraft.getInstance().font, x, 0, width, height,
                        Component.literal("Notification Sound"));
                soundField.setMaxLength(120);
                soundField.setValue(sound.getId());
                soundField.setResponder((soundId) -> sound.setId(soundId.strip()));
                elements.add(soundField);
            }

            public void updateValue() {
                soundField.setValue(sound.getId());
            }
        }

        private static class SoundOption extends Entry {
            SoundOption(int x, int width, int height, Sound sound, SoundOptionsList listWidget,
                        String soundId, String soundName) {
                super();
                elements.add(new SilentButton(x, 0, width, height, Component.literal(soundName),
                        (button) -> {
                            sound.setId(soundId);
                            listWidget.refreshSoundField();
                            listWidget.playNotifSound();
                        }));
            }
        }
    }
}