package notryken.chatnotify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import notryken.chatnotify.ChatNotifyFabric;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

import static notryken.chatnotify.ChatNotifyFabric.*;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler
{
    @Unique
    MinecraftClient client = MinecraftClient.getInstance();

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
        ChatNotifyFabric.config.setUsername(player.getName().getString());
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

        long oldTime = currentTime - 5000;
        removeOldMessages(oldTime);

        // Check for prefixes

        content = content.toLowerCase(Locale.ROOT);
        String plainMsg = "";

        for (String prefix : config.getPrefixes()) {
            if (content.startsWith(prefix)) {
                plainMsg = content.replaceFirst(prefix, "").strip();
                break;
            }
        }

        if (plainMsg.isEmpty()) {
            recentMessages.add(content);
            recentMessageTimes.add(currentTime);
        }
        else {
            recentMessages.add(plainMsg);
            recentMessageTimes.add(currentTime);
        }
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"))
    public void sendChatCommand(String command, CallbackInfo ci)
    {
        long currentTime = System.currentTimeMillis();

        long oldTime = currentTime - 5000;
        removeOldMessages(oldTime);

        // Check for prefixes

        command = "/" + command.toLowerCase(Locale.ROOT);

        for (String prefix : config.getPrefixes()) {
            if (command.startsWith(prefix)) {
                command = command.replaceFirst(prefix, "").strip();
                if (!command.isEmpty()) {
                    recentMessages.add(command.toLowerCase(Locale.ROOT));
                    recentMessageTimes.add(currentTime);
                }
                break;
            }
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    public void sendCommand(String command, CallbackInfoReturnable<Boolean> cir)
    {
        long currentTime = System.currentTimeMillis();

        long oldTime = currentTime - 5000;
        removeOldMessages(oldTime);

        // Check for prefixes

        command = "/" + command.toLowerCase(Locale.ROOT);

        for (String prefix : config.getPrefixes()) {
            if (command.startsWith(prefix)) {
                command = command.replaceFirst(prefix, "").strip();
                if (!command.isEmpty()) {
                    recentMessages.add(command);
                    recentMessageTimes.add(currentTime);
                }
                break;
            }
        }
    }

    @Unique
    private void removeOldMessages(long oldTime)
    {
        for (int i = 0; i < recentMessages.size();) {
            if (recentMessageTimes.get(i) < oldTime) {
                recentMessages.remove(i);
                recentMessageTimes.remove(i);
            }
            else {
                i++;
            }
        }
    }
}
