/*
 * Copyright 2024 TerminalMC
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

package dev.terminalmc.chatnotify.gui.widget.slider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance.SliderableValueSet;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Modified slider for boosted chat height.
 */
public enum ChatHeightSlider implements SliderableValueSet<Double> {
    INSTANCE;

    private final double maxChatHeight = 3.0;

    @Override
    public @NotNull Optional<Double> validateValue(@NotNull Double d) {
        return d >= 0.0 && d <= maxChatHeight ?
                Optional.of(d) : Optional.empty();
    }

    @Override
    public double toSliderValue(@NotNull Double d) {
        return d / maxChatHeight;
    }

    @Override
    public @NotNull Double fromSliderValue(double d) {
        return d * maxChatHeight;
    }

    @Override
    public @NotNull Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(0, maxChatHeight),
                Codec.BOOL).xmap(either -> either.map(value -> value,
                value -> value ? 1.0 : 0.0), Either::left);
    }
}
