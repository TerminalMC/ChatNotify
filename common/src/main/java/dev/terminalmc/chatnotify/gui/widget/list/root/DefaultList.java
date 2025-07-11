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

package dev.terminalmc.chatnotify.gui.widget.list.root;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import dev.terminalmc.chatnotify.mixin.accessor.TextColorAccessor;
import dev.terminalmc.chatnotify.util.Unicode;
import dev.terminalmc.chatnotify.util.text.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.awt.*;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class DefaultList extends OptionList {

    public DefaultList(
            Minecraft mc,
            OptionScreen screen,
            int width,
            int height,
            int y,
            int entryWidth,
            int entryHeight,
            int entrySpacing
    ) {
        super(mc, screen, width, height, y, entryWidth, entryHeight, entrySpacing);
    }

    @Override
    protected void addEntries() {
        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("option", "default.list", Unicode.INFO.str),
                Tooltip.create(localized("option", "default.list.tooltip")),
                -1
        ));

        addEntry(new Entry.DefaultColor(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.DefaultSound(entryX, entryWidth, entryHeight, this));
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
                () -> (double) Config.get().defaultSound.getVolume(),
                (value) -> Config.get().defaultSound.setVolume(value.floatValue())
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
                () -> (double) Config.get().defaultSound.getPitch(),
                (value) -> Config.get().defaultSound.setPitch(value.floatValue())
        ));
        addEntry(new Entry.SoundSource(entryX, entryWidth, entryHeight, this));
    }

    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class DefaultColor extends Entry {

            DefaultColor(int x, int width, int height, DefaultList list) {
                super();
                int colorFieldWidth = Minecraft.getInstance().font.width("#FFAAFF+++");

                Button mainButton = Button.builder(
                                localized(
                                        "option",
                                        "default.color"
                                ).setStyle(Style.EMPTY.withColor(Config.get().defaultColor)), (button) -> {
                                    int cpHeight = HsvColorPicker.MIN_HEIGHT;
                                    int cpWidth = HsvColorPicker.MIN_WIDTH;
                                    list.screen.setOverlayWidget(new HsvColorPicker(
                                            x + width / 2 - cpWidth / 2,
                                            list.screen.height / 2 - cpHeight / 2,
                                            cpWidth,
                                            cpHeight,
                                            () -> Config.get().defaultColor,
                                            (val) -> Config.get().defaultColor = val,
                                            (widget) -> list.init()
                                    ));
                                }
                        )
                        .pos(x, 0)
                        .size(width - colorFieldWidth - SPACE, height)
                        .build();
                elements.add(mainButton);

                TextField colorField =
                        new TextField(x + width - colorFieldWidth, 0, colorFieldWidth, height);
                colorField.hexColorValidator().strict();
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = ColorUtil.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        Config.get().defaultColor = color;
                        // Update color of main button and field
                        mainButton.setMessage(localized(
                                "option",
                                "default.color"
                        ).setStyle(Style.EMPTY.withColor(textColor)));
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(
                                ARGB.red(color),
                                ARGB.green(color),
                                ARGB.blue(color),
                                hsv
                        );
                        if (hsv[2] < 0.1)
                            colorField.setTextColor(0xFFFFFFFF);
                        else
                            colorField.setTextColor(color);
                    }
                });
                colorField.setValue(((TextColorAccessor) (Object) TextColor.fromRgb(Config.get().defaultColor)).chatnotify$formatValue());
                elements.add(colorField);
            }
        }

        private static class DefaultSound extends Entry {

            DefaultSound(int x, int width, int height, DefaultList list) {
                super();
                elements.add(Button.builder(
                                localized("option", "default.sound", Config.get().defaultSound.getId()),
                                (button) -> {
                                    int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                                    int wWidth =
                                            Math.max(DropdownTextField.MIN_WIDTH, list.dynWideEntryWidth);
                                    int wX = x + (width / 2) - (wWidth / 2);
                                    int wY = list.getY();
                                    list.screen.setOverlayWidget(new DropdownTextField(
                                            wX,
                                            wY,
                                            wWidth,
                                            wHeight,
                                            Component.empty(),
                                            Config.get().defaultSound::getId,
                                            Config.get().defaultSound::setId,
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
                        )
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }

        private static class SoundSource extends Entry {

            SoundSource(int x, int width, int height, DefaultList list) {
                super();

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
                                width - list.smallWidgetWidth - SPACE,
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
                                "notif.sound.open.minecraft_volume"
                        )))
                        .pos(x + width - list.smallWidgetWidth, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
