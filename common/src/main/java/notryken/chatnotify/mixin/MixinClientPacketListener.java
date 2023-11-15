package notryken.chatnotify.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.entity.player.Player;
import notryken.chatnotify.ChatNotify;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

import static notryken.chatnotify.ChatNotify.recentMessages;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener
{
    @Unique
    Minecraft chatNotify$client = Minecraft.getInstance();

    /**
     * This is one of the earliest opportunities to get a non-null value of the Player.
     */
    @Inject(method = "handleLogin", at = @At("TAIL"))
    public void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci)
    {
        Player player = chatNotify$client.player;
        assert player != null;
        ChatNotify.config().setUsername(player.getName().getString());
    }

    @Inject(method = "sendChat", at = @At("HEAD"))
    public void sendChatMessage(String content, CallbackInfo ci) {
        chatNotify$storeMessage(content);
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    public void sendChatCommand(String command, CallbackInfo ci) {
        chatNotify$storeCommand(command);
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"))
    public void sendCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        chatNotify$storeCommand(command);
    }

    @Unique
    private void chatNotify$storeMessage(String content) {
        long currentTime = System.currentTimeMillis();
        chatNotify$removeOldMessages(currentTime - 5000);

        // Check for prefixes
        content = content.toLowerCase(Locale.ROOT);
        String plainMsg = "";

        for (String prefix : ChatNotify.config().getPrefixes()) {
            if (content.startsWith(prefix)) {
                plainMsg = content.replaceFirst(prefix, "").strip();
                break;
            }
        }

        recentMessages.add(Pair.of(currentTime, plainMsg.isEmpty() ? content : plainMsg));
    }

    @Unique
    private void chatNotify$storeCommand(String command) {
        long currentTime = System.currentTimeMillis();
        chatNotify$removeOldMessages(currentTime - 5000);

        // Check for prefixes
        command = "/" + command.toLowerCase(Locale.ROOT);

        for (String prefix : ChatNotify.config().getPrefixes()) {
            if (command.startsWith(prefix)) {
                command = command.replaceFirst(prefix, "").strip();
                if (!command.isEmpty()) {
                    recentMessages.add(Pair.of(currentTime, command));
                }
                break;
            }
        }
    }

    @Unique
    private void chatNotify$removeOldMessages(long oldTime) {
        recentMessages.removeIf(pair -> pair.getFirst() < oldTime);
    }
}
