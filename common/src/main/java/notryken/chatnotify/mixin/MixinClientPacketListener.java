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

/**
 * Minecraft provides no reliable way to identify which player sent a given chat message, or
 * even determine whether the message came from a player at all.
 * <p>
 * As a workaround, ChatNotify uses mixins in all message and command-sending methods of
 * ClientPacketListener, to temporarily store all outgoing messages and commands, so that they
 * can be compared to incoming messages to determine whether a given incoming message was sent
 * by the user.
 * <p>
 * All messages and commands are converted to lowercase before being stored, as a workaround
 * for server-side caps filters.
 * <p>
 * Stored messages are normally removed by MessageProcessor when the matching message is
 * received, but in case of a message 'going missing', removeOldMessages is called in each
 * send mixin, and removes all messages that have been stored for more than 5 seconds.
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener
{
    /**
     * This is one of the earliest opportunities to get a non-null value of the Player.
     * Used to verify or correct the username notification.
     */
    @Inject(method = "handleLogin", at = @At("TAIL"))
    public void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci)
    {
        Player player = Minecraft.getInstance().player;
        assert player != null;
        ChatNotify.config().setUsername(player.getName().getString());
    }

    // Chat message and command storage mixins

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
