package notryken.chatnotify.ui.TriggerVariationConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;

public class TriggerVariationConfigScreen extends GameOptionsScreen
{
    private final Notification notif;
    private TriggerVariationConfigListWidget options;

    public TriggerVariationConfigScreen(Screen parent, Notification notif)
    {
        super(parent, MinecraftClient.getInstance().options,
                Text.literal("Notification Trigger Variations"));
        this.notif = notif;
    }

    protected void init()
    {
        this.options = new TriggerVariationConfigListWidget(this.client, this.width,
                this.height, 32, this.height - 32, 25, this.notif);
        this.addSelectableChild(this.options);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE,
                        (button) ->
                        {
                            this.notif.purgeTriggers();
                            assert this.client != null;
                            this.client.setScreen(this.parent);
                        })
                .size(240, 20).position(this.width / 2 - 120,
                        this.height - 27).build());
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY,
                       float delta)
    {
        this.renderBackground(matrices);
        this.options.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title,
                this.width / 2, 5, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
}