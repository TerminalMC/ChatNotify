package notryken.chatnotify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ChatHud.class)
public class MixinChatHud {

    private MinecraftClient client = null;
    private ClientPlayerEntity player = null;
    private List<String> playerNameVariations = null;
    private MutableText modifiedMessage = null;


    /**
     * Injected at net.minecraft.client.gui.hud.ChatHud.addMessage() (HEAD),
     * takes the 'message' parameter and passes it to notify().
     */
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"))
    private void addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci)
    {
        modifiedMessage = notify(message);
    }


    /**
     * If modifiedMessage is not null, passes it to
     * net.minecraft.client.util.chatmessages.breakRenderedChatMessageLines() in
     * net.minecraft.client.gui.hud.ChatHud.addMessage(),
     * replacing the existing message.
     * @param args Arguments to be modified.
     */
    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"))
    private void replaceMessage(Args args)
    {
        if(modifiedMessage != null)
        {
            args.set(0, modifiedMessage);
        }
        modifiedMessage = null;
    }

    /**
     * Returns true if 'message' contains the player's name, surrounded by
     * spaces. Also matches if the name is prefixed with "@", or both prefixed
     * and suffixed with single or double quotations.
     * @param message String to search in.
     * @return boolean result of search.
     */
    private boolean msgContainsPlayerName(String message)
    {
        if(playerNameVariations == null)
        {
            playerNameVariations = new ArrayList<>();
            String playerName = player.getName().getString();
            playerNameVariations.add(playerName);
            playerNameVariations.add("@" + playerName);
            playerNameVariations.add("'" + playerName + "'");
            playerNameVariations.add('"' + playerName + '"');
        }

        String[] splitMessage = message.split(" ");

        for(int i = 1; i < splitMessage.length; i++) // Ignore first part;
        {
            for(String name : playerNameVariations)
            {
                if(splitMessage[i].equalsIgnoreCase(name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calls msgContainsStr() to check if 'message' contains the player's name,
     * outside of the message prefix (typically the name of the player sending
     * the message). If it does, creates and returns a new MutableText object
     * from 'message', with added formatting. Also plays a sound to the player.
     * @param message The unmodified Text message received by the client.
     * @return The modified MutableText message to be displayed by the client.
     */
    private MutableText notify(Text message) {

        if(client == null)
        {
            client = MinecraftClient.getInstance();
            player = client.player;
        }
        if (player != null) {
            String strMessage = message.getString();

            if (msgContainsPlayerName(strMessage)) {
                MutableText newMessage = MutableText.of(message.getContent());
                Style style = Style.of(Optional.of(TextColor.fromRgb(16757761)), Optional.of(false), Optional.of(false), Optional.of(false), Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty());
                newMessage.setStyle(style);

                // Required for correct display on servers.
                List<Text> siblings = message.getSiblings();
                newMessage.siblings.addAll(siblings);

                SoundManager soundManager = client.getSoundManager();
                soundManager.play(new PositionedSoundInstance(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.getId(), SoundCategory.PLAYERS, 1f, 1f, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0, 0, 0, true));

                return newMessage;
            }
        }
        return null;
    }
}
