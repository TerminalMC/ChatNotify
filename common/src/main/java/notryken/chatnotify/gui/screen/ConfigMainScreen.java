package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.gui.components.listwidget.GlobalConfigListWidget;

/**
 * Main options screen. Similar to {@code ConfigSubScreen} but required as a
 * standalone to allow override of {@code init()} and {@code onClose()} to clean
 * and save ChatNotify configuration.
 * <p>
 * <b>Note:</b> If creating a ChatNotify config screen (e.g. for ModMenu
 * integration), instantiate this class, not {@code ConfigSubScreen}.
 */
public class ConfigMainScreen extends OptionsSubScreen {
    private GlobalConfigListWidget listWidget;
    public ConfigMainScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("Chat Notify"));
    }

    @Override
    protected void init() {
        this.listWidget = new GlobalConfigListWidget(this.minecraft,
                this.width, this.height, 32, this.height - 32, 25,
                this.lastScreen, Component.literal("Chat Notify"));
        this.addWidget(this.listWidget);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            ChatNotify.config().refreshUsernameNotif();
            ChatNotify.config().purge();
            ChatNotify.config().writeChanges();
            assert this.minecraft != null;
            this.minecraft.setScreen(this.lastScreen);
        })
                .size(240, 20)
                .pos(this.width / 2 - 120, this.height - 27)
                .build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.listWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title,
                this.width / 2, 5, 0xffffff);
    }

    @Override
    public void onClose() {
        ChatNotify.config().refreshUsernameNotif();
        ChatNotify.config().purge();
        ChatNotify.config().writeChanges();
        assert this.minecraft != null;
        this.minecraft.setScreen(this.lastScreen);
    }
}
