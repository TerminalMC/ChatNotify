package notryken.chatnotify.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static notryken.chatnotify.ChatNotify.saveConfig;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    /**
     * Save config on close.
     */
    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        saveConfig();
    }
}
