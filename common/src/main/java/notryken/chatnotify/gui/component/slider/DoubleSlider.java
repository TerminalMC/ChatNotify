package notryken.chatnotify.gui.component.slider;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleSlider extends AbstractSliderButton {
    protected final double min;
    protected final double max;
    protected final double range;
    protected final int precision;
    protected final String labelPrefix;
    protected final String labelSuffix;
    protected final String valueNameMin;
    protected final String valueNameMax;
    protected final Supplier<Double> source; // Slider value source function
    protected final Consumer<Double> dest; // Slider value destination function

    public DoubleSlider(int x, int y, int width, int height, double min, double max, int precision,
                        @Nullable String labelPrefix, @Nullable String labelSuffix,
                        @Nullable String valueNameMin, @Nullable String valueNameMax,
                        Supplier<Double> source, Consumer<Double> dest) {
        super(x, y, width, height, Component.empty(), (source.get() - min) / (max - min));
        this.min = min;
        this.max = max;
        this.range = max - min;
        this.precision = precision;
        this.labelPrefix = labelPrefix;
        this.labelSuffix = labelSuffix;
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
        double labelValue = round(value * range + min, precision);
        String valueStr = String.valueOf(labelValue);
        if (value == 0 && !valueNameMin.isEmpty()) {
            valueStr = valueNameMin;
        }
        else if (value == 1 && !valueNameMax.isEmpty()) {
            valueStr = valueNameMax;
        }
        StringBuilder labelBuilder = new StringBuilder(valueStr);
        if (!labelPrefix.isEmpty()) {
            labelBuilder.insert(0, labelPrefix);
        }
        if (!labelSuffix.isEmpty()) {
            labelBuilder.append(labelSuffix);
        }
        setMessage(Component.literal(labelBuilder.toString()));
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
