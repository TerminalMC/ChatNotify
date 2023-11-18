package notryken.chatnotify;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import notryken.chatnotify.gui.screen.ConfigMainScreen;

@Mod(Constants.MOD_ID)
public class ChatNotifyForge {
    public ChatNotifyForge() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> new ConfigMainScreen(parent))
                );

        ChatNotify.init();
    }
}