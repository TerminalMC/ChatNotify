package notryken.chatnotify;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import notryken.chatnotify.gui.screen.ScreenLauncher;

@Mod(Constants.MOD_ID)
public class ChatNotifyNeoForge
{
    public ChatNotifyNeoForge() {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> new ScreenLauncher.MainOptionsScreen(parent))
                );

        ChatNotify.init();
    }
}