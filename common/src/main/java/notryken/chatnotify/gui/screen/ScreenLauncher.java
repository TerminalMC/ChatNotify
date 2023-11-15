package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.gui.listwidget.ModConfigListWidget;

import java.io.IOException;

public class ScreenLauncher
{
    public static class MainOptionsScreen extends OptionsSubScreen
    {
        private ModConfigListWidget list;
        public MainOptionsScreen(Screen parent)
        {
            super(parent, Minecraft.getInstance().options,
                    Component.literal("Chat Notify"));
        }

        @Override
        protected void init()
        {
            this.list = new ModConfigListWidget(this.minecraft,
                    this.width, this.height, 32, this.height - 32, 25,
                    this.lastScreen, Component.literal("Chat Notify"));
            this.addWidget(this.list);

            this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                            (button) -> {
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
        public void render(GuiGraphics context, int mouseX, int mouseY,
                           float delta)
        {
            this.renderDirtBackground(context);
            super.render(context, mouseX, mouseY, delta);
            this.list.render(context, mouseX, mouseY, delta);
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
}