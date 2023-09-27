package notryken.chatnotify.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static notryken.chatnotify.ChatNotifyForge.saveConfig;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    /**
     * Save config on close.
     */
    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        saveConfig();
    }
}
