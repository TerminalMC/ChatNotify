package notryken.chatnotify.gui.screen;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.gui.component.listwidget.ConfigListWidget;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigScreen extends OptionsSubScreen {

    protected ConfigListWidget listWidget;

    public ConfigScreen(Screen parent, Options options, Component title, ConfigListWidget listWidget) {
        super(parent, options, title);
        this.listWidget = listWidget;
    }

    @Override
    protected void init() {
        /*
        The resize method builds a new listWidget based on the old one, so we
        call it on every initialization to make reloadScreen calls work.
         */
        listWidget = listWidget.resize(width, height, 32, height - 32);
        addWidget(listWidget);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> onClose())
                .size(240, 20)
                .pos(width / 2 - 120, height - 27)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        listWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(font, title, width / 2, 5, 0xffffff);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(context);
    }
}
