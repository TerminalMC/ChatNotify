package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.gui.component.listwidget.ConfigListWidget;
import org.jetbrains.annotations.NotNull;

/**
 * <b>Note:</b> If creating a ChatNotify config screen (e.g. for ModMenu
 * integration), instantiate {@code GlobalConfigScreen}, not this class.
 */
public class NotifConfigScreen extends ConfigScreen {

    public NotifConfigScreen(Screen parent, Component title, ConfigListWidget listWidget) {
        super(parent, Minecraft.getInstance().options, title, listWidget);
    }
}
