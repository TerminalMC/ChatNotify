package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.gui.component.listwidget.GlobalConfigListWidget;

/**
 * <b>Note:</b> If creating a ChatNotify config screen (e.g. for ModMenu
 * integration), instantiate this class, not {@code NotifConfigScreen}.
 */
public class GlobalConfigScreen extends ConfigScreen {

    public GlobalConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options,
                Component.translatable("screen.chatnotify.title.default"), null);
    }

    @Override
    protected void init() {
        // This screen only ever carries a GlobalConfigListWidget.
        listWidget = new GlobalConfigListWidget(minecraft, width, height,
                32, height - 32, 25, lastScreen, title);
        super.init();
    }

    @Override
    public void onClose() {
        ChatNotify.config().validate();
        ChatNotify.config().writeChanges();
        super.onClose();
    }
}
