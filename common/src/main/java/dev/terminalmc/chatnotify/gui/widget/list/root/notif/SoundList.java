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

package dev.terminalmc.chatnotify.gui.widget.list.root.notif;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Sound;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.SilentButton;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import dev.terminalmc.chatnotify.util.Unicode;
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
import org.jetbrains.annotations.Nullable;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class SoundList extends OptionList {

    public static final String[] NOTEBLOCK_SOUNDS = {
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
    public static final String[] POWER_SOUNDS = new String[]{
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
    public static final String[] EXPLOSION_SOUNDS = new String[]{
            "entity.tnt.primed",
            "entity.generic.explode",
            "entity.lightning_bolt.thunder",
            "item.firecharge.use",
            "block.fire.extinguish",
            "entity.firework_rocket.blast",
            "entity.firework_rocket.large_blast",
            "entity.firework_rocket.twinkle",
    };
    public static final String[] VILLAGER_SOUNDS = new String[]{
            "entity.villager.ambient",
            "entity.villager.yes",
            "entity.villager.no",
            "entity.villager.trade",
            "entity.pillager.ambient",
            "entity.vindicator.ambient",
            "entity.vindicator.celebrate",
            "entity.evoker.ambient",
    };
    public static final String[] MISC_SOUNDS = new String[]{
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
    private final Sound sound;
    private @Nullable SoundInstance lastSound;

    public SoundList(
            Minecraft mc,
            OptionScreen screen,
            int width,
            int height,
            int y,
            int entryWidth,
            int entryHeight,
            Sound sound
    ) {
        super(mc, screen, width, height, y, entryWidth, entryHeight, 1);
        this.sound = sound;
    }

    @Override
    protected void addEntries() {
        addEntry(new Entry.SoundField(entryX, entryWidth, entryHeight, sound, this));

        addEntry(new OptionList.Entry.DoubleSlider(
                entryX,
                entryWidth,
                entryHeight,
                0,
                1,
                2,
                localized("option", "notif.sound.volume").getString(),
                null,
                CommonComponents.OPTION_OFF.getString(),
                null,
                () -> (double) sound.getVolume(),
                (value) -> sound.setVolume(value.floatValue())
        ));

        addEntry(new OptionList.Entry.DoubleSlider(
                entryX,
                entryWidth,
                entryHeight,
                0.5,
                2,
                2,
                localized("option", "notif.sound.pitch").getString(),
                null,
                null,
                null,
                () -> (double) sound.getPitch(),
                (value) -> sound.setPitch(value.floatValue())
        ));

        addEntry(new OptionList.Entry.SilentActionButton(
                entryX,
                entryWidth,
                entryHeight,
                Component.literal("> ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(localized(
                                "option",
                                "notif.sound.test"
                        ).withStyle(ChatFormatting.WHITE))
                        .append(" <"),
                null,
                -1,
                button -> playNotifSound()
        ));

        addEntry(new Entry.SoundSource(entryX, entryWidth, entryHeight, this));

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("sound", "group.noteblock"),
                null,
                -1
        ));
        addSoundEntries(NOTEBLOCK_SOUNDS);

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("sound", "group.power"),
                null,
                -1
        ));
        addSoundEntries(POWER_SOUNDS);

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("sound", "group.explosion"),
                null,
                -1
        ));
        addSoundEntries(EXPLOSION_SOUNDS);

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("sound", "group.illager"),
                null,
                -1
        ));
        addSoundEntries(VILLAGER_SOUNDS);

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("sound", "group.misc"),
                null,
                -1
        ));
        addSoundEntries(MISC_SOUNDS);
    }

    private void addSoundEntries(String[] sounds) {
        for (int i = 0; i < sounds.length; i++) {
            addEntry(new Entry.SoundOption(
                    entryX,
                    entryWidth,
                    entryHeight,
                    this,
                    sound,
                    sounds[i],
                    i < sounds.length - 1 ? sounds[++i] : null
            ));
        }
    }

    // Utility methods

    private void refreshSoundField() {
        OptionList.Entry entry = getEntry(0);
        if (entry instanceof Entry.SoundField soundField) {
            soundField.updateValue();
        }
    }

    private void playNotifSound() {
        ResourceLocation location = sound.getResourceLocation();
        if (location != null) {
            if (lastSound != null)
                mc.getSoundManager().stop(lastSound);
            lastSound = new SimpleSoundInstance(
                    location,
                    Config.get().soundSource,
                    sound.getVolume(),
                    sound.getPitch(),
                    SoundInstance.createUnseededRandom(),
                    false,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0,
                    0,
                    0,
                    true
            );
            mc.getSoundManager().play(lastSound);
        }
    }

    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class SoundField extends Entry {

            private final Sound sound;
            private final TextField soundField;

            SoundField(int x, int width, int height, Sound sound, SoundList list) {
                super();
                this.sound = sound;
                int statusButtonWidth = 25;
                int fieldWidth = width - statusButtonWidth - SPACE_SMALL;

                // Sound preview field
                soundField = new FakeTextField(
                        x, 0, fieldWidth, height, () -> {
                    int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                    int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynWideEntryWidth);
                    int wX = x + (width / 2) - (wWidth / 2);
                    int wY = list.getY();
                    list.screen.setOverlayWidget(new DropdownTextField(
                            wX,
                            wY,
                            wWidth,
                            wHeight,
                            Component.empty(),
                            sound::getId,
                            sound::setId,
                            (widget) -> list.init(),
                            Minecraft.getInstance()
                                    .getSoundManager()
                                    .getAvailableSounds()
                                    .stream()
                                    .map(ResourceLocation::toString)
                                    .sorted()
                                    .toList()
                    ).withSoundDropType());
                }
                );
                soundField.soundValidator();
                soundField.setMaxLength(240);
                soundField.setValue(sound.getId());
                elements.add(soundField);

                // Status button
                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)
                        )
                        .displayOnlyValue()
                        .withInitialValue(sound.isEnabled())
                        .create(
                                x + width - statusButtonWidth,
                                0,
                                statusButtonWidth,
                                height,
                                Component.empty(),
                                (button, status) -> sound.setEnabled(status)
                        ));
            }

            public void updateValue() {
                soundField.setValue(sound.getId());
            }
        }

        private static class SoundSource extends Entry {

            SoundSource(int x, int width, int height, SoundList list) {
                super();
                int mainButtonWidth = width - list.smallWidgetWidth - 1;

                elements.add(CycleButton.<net.minecraft.sounds.SoundSource>builder(source -> Component.translatable(
                                "soundCategory." + source.getName()))
                        .withValues(net.minecraft.sounds.SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "notif.sound.source.tooltip"
                        )))
                        .create(
                                x,
                                0,
                                mainButtonWidth,
                                height,
                                localized("option", "notif.sound.source"),
                                (button, status) -> Config.get().soundSource = status
                        ));

                elements.add(Button.builder(
                                Component.literal(Unicode.SOUND.str),
                                (button) -> Minecraft.getInstance()
                                        .setScreen(new SoundOptionsScreen(
                                                list.screen,
                                                Minecraft.getInstance().options
                                        ))
                        )
                        .tooltip(Tooltip.create(localized(
                                "option",
                                "notif.sound.open.minecraft_volume.tooltip"
                        )))
                        .pos(x + width - list.smallWidgetWidth, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class SoundOption extends Entry {

            SoundOption(
                    int x,
                    int width,
                    int height,
                    SoundList list,
                    Sound sound,
                    String soundId1,
                    @Nullable String soundId2
            ) {
                super();
                int buttonWidth = (width - SPACE_TINY) / 2;

                elements.add(new SilentButton(
                        x,
                        0,
                        buttonWidth,
                        height,
                        localized("sound", "id." + soundId1),
                        (button) -> {
                            sound.setId(soundId1);
                            list.refreshSoundField();
                            list.playNotifSound();
                        }
                ));

                if (soundId2 != null) {
                    elements.add(new SilentButton(
                            x + width - buttonWidth,
                            0,
                            buttonWidth,
                            height,
                            localized("sound", "id." + soundId2),
                            (button) -> {
                                sound.setId(soundId2);
                                list.refreshSoundField();
                                list.playNotifSound();
                            }
                    ));
                }
            }
        }
    }
}
