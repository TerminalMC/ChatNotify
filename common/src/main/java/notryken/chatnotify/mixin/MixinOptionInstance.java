package notryken.chatnotify.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import notryken.chatnotify.gui.components.ChatHeightSlider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Boosts the chat height slider.
 */
@Mixin(OptionInstance.class)
public class MixinOptionInstance {
    @Shadow
    @Final
    Component caption;

    @Shadow
    @Final
    @Mutable
    private
    OptionInstance.ValueSet<Double> values;

    @Shadow
    @Final
    @Mutable
    private
    Codec<Double> codec;

    /**
     * Increases the maximum value of the chat height focused slider.
     */
    @Inject(at = @At("RETURN"), method = "<init>*")
    private void init(CallbackInfo ci) {
        ComponentContents content = this.caption.getContents();
        if (!(content instanceof TranslatableContents)) return;

        String key = ((TranslatableContents) content).getKey();
        if (!key.equals("options.chat.height.focused")) return;

        this.values = ChatHeightSlider.INSTANCE;
        this.codec = this.values.codec();
    }
}
