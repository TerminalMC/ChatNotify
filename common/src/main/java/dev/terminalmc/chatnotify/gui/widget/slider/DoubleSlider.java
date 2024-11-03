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

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleSlider extends AbstractSliderButton {
    protected final double min;
    protected final double max;
    protected final double range;
    protected final int precision;
    protected final String messagePrefix;
    protected final String messageSuffix;
    protected final String valueNameMin;
    protected final String valueNameMax;
    protected final Supplier<Double> source; // Slider value source function
    protected final Consumer<Double> dest; // Slider value destination function

    public DoubleSlider(int x, int y, int width, int height, double min, double max, int precision,
                        @Nullable String messagePrefix, @Nullable String messageSuffix,
                        @Nullable String valueNameMin, @Nullable String valueNameMax,
                        Supplier<Double> source, Consumer<Double> dest) {
        super(x, y, width, height, Component.empty(), (source.get() - min) / (max - min));
        this.min = min;
        this.max = max;
        this.range = max - min;
        this.precision = precision;
        this.messagePrefix = messagePrefix;
        this.messageSuffix = messageSuffix;
        this.valueNameMin = valueNameMin;
        this.valueNameMax = valueNameMax;
        this.source = source;
        this.dest = dest;
        updateMessage();
    }

    public void refresh() {
        value = (source.get() - min) / range;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double messageValue = round(value * range + min, precision);
        String valueStr = String.valueOf(messageValue);
        if (value == 0 && valueNameMin != null) {
            valueStr = valueNameMin;
        }
        else if (value == 1 && valueNameMax != null) {
            valueStr = valueNameMax;
        }
        StringBuilder messageBuilder = new StringBuilder(valueStr);
        if (messagePrefix != null) {
            messageBuilder.insert(0, messagePrefix);
        }
        if (messageSuffix != null) {
            messageBuilder.append(messageSuffix);
        }
        setMessage(Component.literal(messageBuilder.toString()));
    }

    @Override
    protected void applyValue() {
        dest.accept(round(value * range + min, precision));
    }

    protected double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
