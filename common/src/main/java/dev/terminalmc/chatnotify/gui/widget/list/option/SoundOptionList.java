/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.gui.widget.list.option;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Sound;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.SilentButton;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for a {@link Sound}.
 */
public class SoundOptionList extends OptionList {
    private final Sound sound;
    private final Runnable closeRunnable;

    public SoundOptionList(Minecraft mc, int width, int height, int y, 
                           int entryWidth, int entryHeight, Sound sound, Runnable closeRunnable) {
        super(mc, width, height, y, entryHeight + 1, entryWidth, entryHeight);
        this.sound = sound;
        this.closeRunnable = closeRunnable;

        addEntry(new Entry.SoundFieldEntry(entryX, entryWidth, entryHeight, sound, this));

        addEntry(new OptionList.Entry.DoubleSliderEntry(entryX, entryWidth, entryHeight, 0, 1, 2,
                localized("option", "sound.volume").getString(), null,
                CommonComponents.OPTION_OFF.getString(), null,
                () -> (double)sound.getVolume(), (value) -> sound.setVolume(value.floatValue())));

        addEntry(new OptionList.Entry.DoubleSliderEntry(entryX, entryWidth, entryHeight, 0.5, 2, 2,
                localized("option", "sound.pitch").getString(), null, null, null,
                () -> (double)sound.getPitch(), (value) -> sound.setPitch(value.floatValue())));

        addEntry(new OptionList.Entry.SilentActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("> ").withStyle(ChatFormatting.YELLOW)
                        .append(localized("option", "sound.test").withStyle(ChatFormatting.WHITE))
                        .append(" <"), null, -1,
                button -> playNotifSound()));

        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight, this));

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
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
        for (int i = 0; i < noteblockSounds.length; i++) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound, 
                    noteblockSounds[i], i < noteblockSounds.length - 1 ? noteblockSounds[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
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
        for (int i = 0; i < powerSounds.length; i++) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound,
                    powerSounds[i], i < powerSounds.length - 1 ? powerSounds[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
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
        for (int i = 0; i < explosionSounds.length; i++) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound,
                    explosionSounds[i], i < explosionSounds.length - 1 ? explosionSounds[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "sound.group.illager"), null, -1));
        String[] villagerSounds = new String[]{
                "entity.villager.ambient",
                "entity.villager.yes",
                "entity.villager.no",
                "entity.villager.trade",
                "entity.pillager.ambient",
                "entity.vindicator.ambient",
                "entity.vindicator.celebrate",
                "entity.evoker.ambient",
        };
        for (int i = 0; i < villagerSounds.length; i++) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound,
                    villagerSounds[i], i < villagerSounds.length - 1 ? villagerSounds[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
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
        for (int i = 0; i < miscSounds.length; i++) {
            addEntry(new Entry.SoundOption(entryX, entryWidth, entryHeight, this, sound,
                    miscSounds[i], i < miscSounds.length - 1 ? miscSounds[++i] : null));
        }
    }

    @Override
    public SoundOptionList reload(int width, int height, double scrollAmount) {
        SoundOptionList newList = new SoundOptionList(minecraft, width, height,
                getY(), entryWidth, entryHeight, sound, closeRunnable);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    @Override
    public void onClose() {
        closeRunnable.run();
    }

    private void refreshSoundField() {
        OptionList.Entry entry = getEntry(0);
        if (entry instanceof Entry.SoundFieldEntry soundFieldEntry) {
            soundFieldEntry.updateValue();
        }
    }

    private void playNotifSound() {
        minecraft.getSoundManager().stop();
        ResourceLocation location = sound.getResourceLocation();
        if (location != null) {
            minecraft.getSoundManager().play(new SimpleSoundInstance(
                    location,
                    Config.get().soundSource,
                    sound.getVolume(), sound.getPitch(),
                    SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
    }

    private abstract static class Entry extends OptionList.Entry {

        private static class SoundFieldEntry extends Entry {
            private final Sound sound;
            private final TextField soundField;

            SoundFieldEntry(int x, int width, int height, Sound sound, SoundOptionList list) {
                super();
                this.sound = sound;

                soundField = new FakeTextField(x, 0, width, height,
                        () -> {
                            int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                            int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynEntryWidth);
                            int wX = x + (width / 2) - (wWidth / 2);
                            int wY = list.getY();
                            list.screen.setOverlayWidget(new DropdownTextField(
                                    wX, wY, wWidth, wHeight, Component.empty(),
                                    sound::getId, sound::setId,
                                    (widget) -> {
                                        Minecraft.getInstance().getSoundManager().stop();
                                        list.screen.removeOverlayWidget();
                                        list.reload();
                                    }, Minecraft.getInstance().getSoundManager().getAvailableSounds()
                                    .stream().map(ResourceLocation::toString).sorted().toList())
                                    .withSoundDropType());
                        });
                soundField.soundValidator();
                soundField.setMaxLength(240);
                soundField.setValue(sound.getId());
                elements.add(soundField);
            }

            public void updateValue() {
                soundField.setValue(sound.getId());
            }
        }

        private static class SoundSourceEntry extends MainOptionList.Entry {
            SoundSourceEntry(int x, int width, int height, SoundOptionList list) {
                super();
                int mainButtonWidth = width - list.smallWidgetWidth - 1;

                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                                "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "sound.source.tooltip")))
                        .create(x, 0, mainButtonWidth, height,
                                localized("option", "sound.source"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(
                                Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        list.screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(
                                localized("option", "sound.source.minecraft_volume")))
                        .pos(x + width - list.smallWidgetWidth, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class SoundOption extends Entry {
            SoundOption(int x, int width, int height, SoundOptionList list, Sound sound,
                        String soundId1, @Nullable String soundId2) {
                super();
                int buttonWidth = (width - 1) / 2;
                
                elements.add(new SilentButton(x, 0, buttonWidth, height,
                        localized("option", "sound.id." + soundId1),
                        (button) -> {
                            sound.setId(soundId1);
                            list.refreshSoundField();
                            list.playNotifSound();
                        }));

                if (soundId2 != null) {
                    elements.add(new SilentButton(x + width - buttonWidth, 0, buttonWidth, height,
                            localized("option", "sound.id." + soundId2),
                            (button) -> {
                                sound.setId(soundId2);
                                list.refreshSoundField();
                                list.playNotifSound();
                            }));
                }
            }
        }
    }
}
