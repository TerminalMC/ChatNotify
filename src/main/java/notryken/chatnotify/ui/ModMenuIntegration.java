package notryken.chatnotify.ui;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import notryken.chatnotify.client.ChatNotifyClient;

import static notryken.chatnotify.client.ChatNotifyClient.config;

public class ModMenuIntegration implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return ModMenuOptionsScreen::new;
    }

    public static class ModMenuOptionsScreen extends GameOptionsScreen
    {
        private NotificationListWidget list;
        public ModMenuOptionsScreen(Screen parent)
        {
            super(parent, MinecraftClient.getInstance().options,
                    Text.literal("Chat Notify Options"));
        }

        @Override
        protected void init()
        {
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder()
                    .initially(config.ignoreOwnMessages).build(
                            this.width / 2 - 120, 32, 240, 20,
                            Text.literal("Ignore Your Own Messages"),
                            (button, status) ->
                                    config.ignoreOwnMessages = status));

            this.list = new NotificationListWidget(this.client, this.parent,
                    this.width, this.height, 64, this.height - 32, 25);
            this.addSelectableChild(this.list);

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE,
                    (button) ->
                    {
                        config.refreshUsernameNotif();
                        config.purge();
                        ChatNotifyClient.saveConfig();
                        assert this.client != null;
                        this.client.setScreen(this.parent);
                    })
                    .size(240, 20).position(this.width / 2 - 120,
                            this.height - 27).build());
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY,
                           float delta)
        {
            this.renderBackground(matrices);
            this.list.render(matrices, mouseX, mouseY, delta);
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.title,
                    this.width / 2, 5, 0xffffff);
            super.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public void close()
        {
            config.refreshUsernameNotif();
            config.purge();
            ChatNotifyClient.saveConfig();
            assert this.client != null;
            this.client.setScreen(this.parent);
        }

    }
}