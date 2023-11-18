package notryken.chatnotify;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import notryken.chatnotify.gui.screen.ConfigMainScreen;

@Mod(Constants.MOD_ID)
public class ChatNotifyNeoForge {
    public ChatNotifyNeoForge() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> new ConfigMainScreen(parent))
                );

        ChatNotify.init();
    }
}