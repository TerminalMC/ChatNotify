package notryken.chatnotify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import notryken.chatnotify.client.ChatNotifyClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static notryken.chatnotify.client.ChatNotifyClient.recentMessageTimes;
import static notryken.chatnotify.client.ChatNotifyClient.recentMessages;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler
{
    @Final
    @Shadow
    private MinecraftClient client;

    /**
     * Injects into ClientPlayNetworkHandler.onGameJoin(), which handles
     * initialization of various client-related fields. This is one of the
     * earliest opportunities to get a non-null value of MinecraftClient.player.
     */
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        PlayerEntity player = client.player;
        /* This can only be null if Minecraft's internal onGameJoin() method
        breaks completely, which will crash the game anyway.*/
        assert player != null;
        ChatNotifyClient.config.setUsername(player.getName().getString());
    }

    /**
     * Injects into ClientPlayNetworkHandler.sendChatMessage(), which handles
     * outgoing messages from the client. This allows the mod to track which
     * messages are sent by the user.
     */
    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    public void sendChatMessage(String content, CallbackInfo ci)
    {
        long currentTime = System.currentTimeMillis();

        // Remove old messages if sent more than 5 seconds ago.

        for (int i = 0; i < recentMessages.size();) {
            if (recentMessageTimes.get(i) + 5000 > currentTime) {
                recentMessages.remove(i);
                recentMessageTimes.remove(i);
            }
            else {
                i++;
            }
        }
        recentMessages.add(content);
        recentMessageTimes.add(currentTime);
    }
}
