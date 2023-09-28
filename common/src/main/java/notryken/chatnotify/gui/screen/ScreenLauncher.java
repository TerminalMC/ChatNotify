package notryken.chatnotify.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.gui.listwidget.ModConfigListWidget;

import static notryken.chatnotify.ChatNotify.config;

public class ScreenLauncher
{
    public static class MainOptionsScreen extends GameOptionsScreen
    {
        private ModConfigListWidget list;
        public MainOptionsScreen(Screen parent)
        {
            super(parent, MinecraftClient.getInstance().options,
                    Text.literal("Chat Notify"));
        }

        @Override
        protected void init()
        {
            this.list = new ModConfigListWidget(this.client,
                    this.width, this.height, 32, this.height - 32, 25,
                    this.parent, Text.literal("Chat Notify"));
            this.addSelectableChild(this.list);

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE,
                            (button) -> {
                config.refreshUsernameNotif();
                config.purge();
                ChatNotify.saveConfig();
                assert this.client != null;
                this.client.setScreen(this.parent);
            })
                    .size(240, 20)
                    .position(this.width / 2 - 120, this.height - 27)
                    .build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY,
                           float delta)
        {
            this.renderBackgroundTexture(context);
            super.render(context, mouseX, mouseY, delta);
            this.list.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                    this.width / 2, 5, 0xffffff);
        }

        @Override
        public void close()
        {
            config.refreshUsernameNotif();
            config.purge();
            ChatNotify.saveConfig();
            assert this.client != null;
            this.client.setScreen(this.parent);
        }
    }
}