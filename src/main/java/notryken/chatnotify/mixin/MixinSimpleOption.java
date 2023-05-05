package notryken.chatnotify.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import notryken.chatnotify.misc.ChatHeightSliderCallbacks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Increases the maximum value of the chat height focused slider.
 */
@Mixin(SimpleOption.class)
public class MixinSimpleOption
{
    @Shadow
    @Final
    Text text;

    @Shadow
    @Final
    @Mutable
    private
    SimpleOption.Callbacks<Double> callbacks;

    @Shadow
    @Final
    @Mutable
    private
    Codec<Double> codec;

    @Inject(at = @At("RETURN"), method = "<init>*")
    private void init(CallbackInfo ci)
    {
        TextContent content = this.text.getContent();
        if (!(content instanceof TranslatableTextContent))
            return;

        String key = ((TranslatableTextContent) content).getKey();
        if (!key.equals("options.chat.height.focused"))
            return;

        this.callbacks = ChatHeightSliderCallbacks.INSTANCE;
        this.codec = this.callbacks.codec();
    }
}
