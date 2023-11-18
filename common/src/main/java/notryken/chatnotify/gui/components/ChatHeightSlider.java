package notryken.chatnotify.gui.components;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance.SliderableValueSet;

import java.util.Optional;

/**
 * Modified slider for boosted chat height.
 */
public enum ChatHeightSlider implements SliderableValueSet<Double> {
    INSTANCE;

    private final double maxChatHeight = 3.0;

    @Override
    public Optional<Double> validateValue(Double d) {
        return d >= 0.0 && d <= maxChatHeight ?
                Optional.of(d) : Optional.empty();
    }

    @Override
    public double toSliderValue(Double d) {
        return d * maxChatHeight;
    }

    @Override
    public Double fromSliderValue(double d) {
        return d / maxChatHeight;
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(0, maxChatHeight),
                Codec.BOOL).xmap(either -> either.map(value -> value,
                value -> value ? 1.0 : 0.0), Either::left);
    }
}
