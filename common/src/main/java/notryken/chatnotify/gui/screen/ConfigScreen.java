package notryken.chatnotify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.gui.component.listwidget.ConfigListWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ConfigScreen extends OptionsSubScreen {

    protected ConfigListWidget listWidget;
    public final int listTop = 32;
    public final Supplier<Integer> listBottom = () -> height - 32;
    public final int listItemHeight = 25;

    public ConfigScreen(Screen parent, Component title, @Nullable ConfigListWidget listWidget) {
        super(parent, Minecraft.getInstance().options, title);
        this.listWidget = listWidget;
    }

    @Override
    protected void init() {
        reloadListWidget();
        addRenderableWidget(listWidget);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> onClose())
                .pos(width / 2 - 120, height - 27)
                .size(240, 20)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderDirtBackground(context);
        context.drawCenteredString(font, title, width / 2, 5, 0xffffff);
        super.render(context, mouseX, mouseY, delta);
    }

    public void reloadListWidget() {
        ConfigListWidget newListWidget = listWidget.resize(
                width, height, listTop, listBottom.get(), listItemHeight);
        newListWidget.setScreen(this);
        listWidget = newListWidget;
    }
}
