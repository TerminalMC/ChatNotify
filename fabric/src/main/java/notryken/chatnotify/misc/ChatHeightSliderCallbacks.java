package notryken.chatnotify.misc;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.SimpleOption.SliderCallbacks;

import java.util.Optional;

/**
 * Modified slider for boosted chat height.
 */
public enum ChatHeightSliderCallbacks implements SliderCallbacks<Double>
{
    INSTANCE;

    private final double maxChatHeight = 3.0;

    @Override
    public double toSliderProgress(Double double_) {
        return double_ / maxChatHeight;
    }

    @Override
    public Double toValue(double d) {
        return d * maxChatHeight;
    }

    @Override
    public Optional<Double> validate(Double double_) {
        return double_ >= 0.0 && double_ <= maxChatHeight ?
                Optional.of(double_) : Optional.empty();
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(0, maxChatHeight),
                Codec.BOOL).xmap(either -> either.map(value -> value,
                value -> value ? 1.0 : 0.0), Either::left);
    }
}
