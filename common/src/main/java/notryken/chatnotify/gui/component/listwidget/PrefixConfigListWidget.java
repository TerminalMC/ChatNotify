package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;

import java.util.Locale;

/**
 * {@code ConfigListWidget} containing controls for ChatNotify message modifier
 * prefixes.
 */
public class PrefixConfigListWidget extends ConfigListWidget {
    public PrefixConfigListWidget(Minecraft minecraft, int width, int height,
                                  int top, int bottom, int itemHeight, Screen parentScreen) {
        super(minecraft, width, height, top, bottom, itemHeight, parentScreen);

        int eX = width / 2 - 120;
        int eW = 240;
        int eH = 20;

        addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                Component.literal("Message Modifier Prefixes \u2139"),
                Tooltip.create(Component.literal("A message prefix is a character or " +
                        "sequence of characters that you type before a message to modify it. " +
                        "For example, '!' or '/shout' may be used on some servers to communicate " +
                        "in global chat. This may be useful for preventing spurious notifications.")), -1));

        int max = ChatNotify.config().getPrefixes().size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(eX, eW, eH, this, i));
        }
        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(eX, 0, eW, eH,
                Component.literal("+"), null, -1,
                (button) -> {
                    ChatNotify.config().addPrefix("");
                    reloadScreen();
                }));
    }

    @Override
    public PrefixConfigListWidget resize(int width, int height, int top, int bottom) {
        PrefixConfigListWidget listWidget = new PrefixConfigListWidget(
                minecraft, width, height, top, bottom, itemHeight, parentScreen);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reloadScreen() {
        reloadScreen(this);
    }


    private abstract static class Entry extends ConfigListWidget.Entry {

        private static class PrefixFieldEntry extends Entry {
            PrefixFieldEntry(int x, int width, int height, PrefixConfigListWidget listWidget, int index) {
                super();

                int spacing = 5;
                int removeButtonWidth = 24;

                EditBox prefixEditBox = new EditBox(
                        Minecraft.getInstance().font, x, 0, width, height,
                        Component.literal("Message Prefix"));
                prefixEditBox.setMaxLength(20);
                prefixEditBox.setValue(ChatNotify.config().getPrefix(index));
                prefixEditBox.setResponder(
                        (prefix) -> ChatNotify.config().setPrefix(
                                index, prefix.strip().toLowerCase(Locale.ROOT)));
                elements.add(prefixEditBox);

                elements.add(Button.builder(Component.literal("X"),
                                (button) -> {
                                    ChatNotify.config().removePrefix(index);
                                    listWidget.reloadScreen();
                                })
                        .pos(x + width + spacing, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }
    }
}