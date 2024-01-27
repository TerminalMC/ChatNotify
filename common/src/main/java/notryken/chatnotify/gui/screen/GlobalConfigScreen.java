package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.gui.component.listwidget.GlobalConfigListWidget;

/**
 * <b>Note:</b> If creating a ChatNotify config screen (e.g. for ModMenu
 * integration), instantiate this class, not {@code ConfigScreen}.
 */
public class GlobalConfigScreen extends ConfigScreen {

    public GlobalConfigScreen(Screen lastScreen) {
        super(lastScreen, Component.translatable("screen.chatnotify.title.default"),
                new GlobalConfigListWidget(Minecraft.getInstance(), 0, 0, 0, 0,
                        0, -120, 240, 20, 300));
    }

    @Override
    public void onClose() {
        ChatNotify.config().writeChanges();
        super.onClose();
    }
}
