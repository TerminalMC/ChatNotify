package notryken.chatnotify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import notryken.chatnotify.config.NotifyOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static notryken.chatnotify.client.ChatNotifyClient.config;
import static notryken.chatnotify.client.ChatNotifyClient.username;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler
{
    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        PlayerEntity player = client.player;

        assert player != null;
        username = player.getName().getString();

        NotifyOption playerNameOption = config.getOption(1);

        if(playerNameOption != null)
        {
            playerNameOption.setWord(username);
        }
    }
}
