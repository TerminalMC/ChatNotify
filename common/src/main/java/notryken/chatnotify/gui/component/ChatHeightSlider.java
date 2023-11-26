package notryken.chatnotify.gui.component;

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
