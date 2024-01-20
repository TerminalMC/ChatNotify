package notryken.chatnotify.gui.component.slider;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/**
 * 0-255 slider for a channel (red, green or blue) of an RGB color. Slider label
 * color will track the slider value.
 */
public class RgbChannelSlider extends DoubleSlider {
    private final IntUnaryOperator toChannel;
    private final IntUnaryOperator fromChannel;

    /**
     * @param source rgb color int source.
     * @param dest rgb color int destination.
     * @param toChannel operator to get the value (0-255) of the slider's
     *                  channel from an RGB int.
     * @param fromChannel operator convert the value (0-255) of the slider's
     *                    channel to an RGB int.
     */
    public RgbChannelSlider(int x, int y, int width, int height,
                            @Nullable String labelPrefix, @Nullable String labelSuffix,
                            Supplier<Integer> source, Consumer<Integer> dest,
                            IntUnaryOperator toChannel, IntUnaryOperator fromChannel) {
        super(x, y, width, height, 0, 255, 0, labelPrefix, labelSuffix,
                null, null,
                () -> (double) toChannel.applyAsInt(source.get()),
                (value) -> dest.accept(fromChannel.applyAsInt(value.intValue())));
        this.toChannel = toChannel;
        this.fromChannel = fromChannel;
    }

    @Override
    protected void updateMessage() {
        double labelValue = round(value * 255, 0);
        String valueStr = String.valueOf(labelValue);
        StringBuilder labelBuilder = new StringBuilder(valueStr);
        if (!labelPrefix.isEmpty()) {
            labelBuilder.insert(0, labelPrefix);
        }
        if (!labelSuffix.isEmpty()) {
            labelBuilder.append(labelSuffix);
        }
        setMessage(Component.literal(labelBuilder.toString()).withStyle(
                Style.EMPTY.withColor(fromChannel.applyAsInt((int) labelValue))));
    }
}
