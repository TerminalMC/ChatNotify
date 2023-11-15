package notryken.chatnotify;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import notryken.chatnotify.gui.screen.ScreenLauncher;

@Mod(Constants.MOD_ID)
public class ChatNotifyForge
{
    public ChatNotifyForge() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> new ScreenLauncher.MainOptionsScreen(parent))
                );

        ChatNotify.init();
    }
}