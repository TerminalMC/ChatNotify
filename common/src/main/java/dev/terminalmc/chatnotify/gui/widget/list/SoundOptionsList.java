/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Sound;
import dev.terminalmc.chatnotify.gui.widget.SilentButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for a {@link Sound}.
 */
public class SoundOptionsList extends OptionsList {
    private final Sound sound;

    public SoundOptionsList(Minecraft mc, int width, int height, int y, int rowWidth,
                            int itemHeight, int entryWidth, int entryHeight, Sound sound) {
        super(mc, width, height, y, rowWidth, itemHeight, entryWidth, entryHeight);
        this.sound = sound;

        addEntry(new Entry.SoundFieldEntry(entryX, entryWidth, entryHeight, sound));

        addEntry(new OptionsList.Entry.DoubleSliderEntry(entryX, entryWidth, entryHeight, 0, 1, 2,
                localized("option", "sound.volume").getString(), null,
                CommonComponents.OPTION_OFF.getString(), null,
                () -> (double)sound.getVolume(), (value) -> sound.setVolume(value.floatValue())));

        addEntry(new OptionsList.Entry.DoubleSliderEntry(entryX, entryWidth, entryHeight, 0.5, 2, 2,
                localized("option", "sound.pitch").getString(), null, null, null,
                () -> (double)sound.getPitch(), (value) -> sound.setPitch(value.floatValue())));

        addEntry(new OptionsList.Entry.SilentActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.test"), null, -1,
                button -> playNotifSound()));

        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight, this));

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.group.noteblock"), null, -1));
        String[] noteblockSounds = {
                        "block.note_block.banjo",
                        "block.note_block.bass",
                        "block.note_block.basedrum",
                        "block.note_block.bell",
                        "block.note_block.bit",
                        "block.note_block.chime",
                        "block.note_block.cow_bell",
                        "block.note_block.didgeridoo",
                        "block.note_block.flute",
                        "block.note_block.guitar",
                        "block.note_block.harp",
                        "block.note_block.hat",
                        "block.note_block.iron_xylophone",
                        "block.note_block.pling",
                        "block.note_block.snare",
                        "block.note_block.xylophone",
                };
        for (String s : noteblockSounds) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound, s));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.group.power"), null, -1));
        String[] powerSounds = new String[]{
                        "block.beacon.activate",
                        "block.beacon.deactivate",
                        "block.beacon.power_select",
                        "block.conduit.activate",
                        "block.conduit.deactivate",
                        "block.end_portal_frame.fill",
                        "block.portal.travel",
                        "block.portal.trigger",
                        "entity.enderman.teleport",
                        "item.trident.return",
                        "entity.elder_guardian.curse",
                        "entity.warden.sonic_boom",
                        "entity.evoker.cast_spell",
                        "entity.evoker.prepare_summon",
                        "entity.evoker.prepare_attack",
                        "entity.zombie_villager.converted",
                };
        for (String s : powerSounds) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound, s));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.group.explosion"), null, -1));
        String[] explosionSounds = new String[]{
                        "entity.tnt.primed",
                        "entity.generic.explode",
                        "entity.lightning_bolt.thunder",
                        "item.firecharge.use",
                        "block.fire.extinguish",
                        "entity.firework_rocket.blast",
                        "entity.firework_rocket.large_blast",
                        "entity.firework_rocket.twinkle",
                };
        for (String s : explosionSounds) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound, s));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.group.illager"), null, -1));
        String[] villagerSounds = new String[]{
                        "entity.villager.ambient",
                        "entity.villager.yes",
                        "entity.villager.no",
                        "entity.villager.trade",
                        "entity.pillager.ambient",
                        "entity.vindicator.ambient",
                        "entity.evoker.ambient",

                };
        for (String s : villagerSounds) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound, s));
        }

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.group.misc"), null, -1));
        String[] miscSounds = new String[]{
                        "entity.arrow.hit_player",
                        "block.bell.use",
                        "block.amethyst_block.hit",
                        "block.amethyst_cluster.place",
                        "entity.allay.item_thrown",
                        "entity.iron_golem.repair",
                        "block.anvil.land",
                        "item.shield.block",
                        "item.shield.break",
                        "entity.player.death",
                        "entity.goat.screaming.prepare_ram",
                        "ui.button.click",
                };
        for (String s : miscSounds) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound, s));
        }
    }

    @Override
    public SoundOptionsList reload(int width, int height, double scrollAmount) {
        SoundOptionsList newListWidget = new SoundOptionsList(minecraft, width, height,
                getY(), getRowWidth(), itemHeight, entryWidth, entryHeight, sound);
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
            SoundSourceEntry(int x, int width, int height, SoundOptionsList listWidget) {
                super();
                int volumeButtonWidth = 20;
                int mainButtonWidth = width - volumeButtonWidth - SPACING;

                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                                "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.sound_source.tooltip")))
                        .create(x, 0, mainButtonWidth, height,
                                localized("option", "global.sound_source"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(
                                Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        listWidget.screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(
                                localized("option", "global.sound_source.minecraft_volume")))
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
                        Component.empty());
                soundField.setMaxLength(120);
                soundField.setValue(sound.getId());
                soundField.setResponder((soundId) -> {
                    if (sound.setId(soundId.strip())) {
                        soundField.setTextColor(16777215);
                    } else {
                        soundField.setTextColor(16711680);
                    }
                });
                elements.add(soundField);
            }

            public void updateValue() {
                soundField.setValue(sound.getId());
            }
        }

        private static class SoundOption extends Entry {
            SoundOption(int x, int width, int height, SoundOptionsList listWidget, Sound sound,
                        String soundId) {
                super();
                elements.add(new SilentButton(x, 0, width, height,
                        localized("option", "sound.id." + soundId),
                        (button) -> {
                            sound.setId(soundId);
                            listWidget.refreshSoundField();
                            listWidget.playNotifSound();
                        }));
            }
        }
    }
}
