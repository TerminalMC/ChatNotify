package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.gui.component.listwidget.GlobalConfigListWidget;
import org.jetbrains.annotations.NotNull;

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
        ChatNotify.config().validateUsernameNotif();
        ChatNotify.config().purge();
        ChatNotify.config().writeChanges();
        super.onClose();
    }
}
