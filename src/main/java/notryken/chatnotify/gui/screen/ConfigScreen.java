package notryken.chatnotify.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import notryken.chatnotify.gui.listwidget.ConfigListWidget;

public class ConfigScreen extends GameOptionsScreen
{
    private final ConfigListWidget listWidget;

    public ConfigScreen(Screen parent, GameOptions gameOptions, Text title,
                        ConfigListWidget listWidget)
    {
        super(parent, gameOptions, title);
        this.listWidget = listWidget;
    }

    @Override
    protected void init()
    {
        this.addSelectableChild(this.listWidget);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE,
                        (button) -> {
                            assert this.client != null;
                            this.client.setScreen(this.parent); })
                .size(240, 20).position(this.width / 2 - 120,
                        this.height - 27).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY,
                       float delta)
    {
        this.renderBackground(matrices);
        this.listWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title,
                this.width / 2, 5, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
