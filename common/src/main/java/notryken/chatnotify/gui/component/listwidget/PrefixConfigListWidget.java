package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * {@code ConfigListWidget} containing controls for ChatNotify message modifier
 * prefixes.
 */
public class PrefixConfigListWidget extends ConfigListWidget {
    public PrefixConfigListWidget(Minecraft client, int width, int height,
                                  int top, int bottom, int itemHeight,
                                  Screen parent, Component title) {
        super(client, width, height, top, bottom, itemHeight, parent, title);

        addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Message Modifier Prefixes ℹ"),
                Component.literal("A message prefix is a character or sequence of " +
                        "characters that you type before a message to modify it. For " +
                        "example, '!' or '/shout' may be used on some servers to " +
                        "communicate in global chat. This may be useful for preventing " +
                        "spurious notifications.")));

        for (int i = 0; i < ChatNotify.config().getPrefixes().size(); i++) {
            addEntry(new Entry.PrefixField(width, client, this, i));
        }
        addEntry(new Entry.PrefixField(width, client, this, -1));
    }

    @Override
    public PrefixConfigListWidget resize(int width, int height, int top, int bottom) {
        PrefixConfigListWidget listWidget = new PrefixConfigListWidget(
                client, width, height, top, bottom, itemHeight, parentScreen, title);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen() {
        refreshScreen(this);
    }


    private abstract static class Entry extends ConfigListWidget.Entry {
        Entry(int width, PrefixConfigListWidget listWidget)
        {
            super(width, listWidget);
        }

        private static class PrefixField extends Entry {
            final int index;

            PrefixField(int width, @NotNull Minecraft client,
                        PrefixConfigListWidget listWidget, int index) {
                super(width, listWidget);
                this.index = index;

                if (index == -1) {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        ChatNotify.config().addPrefix("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {
                    EditBox prefixEdit = new EditBox(
                            client.font, this.width / 2 - 120, 0, 240,
                            20, Component.literal("Message Prefix"));
                    prefixEdit.setMaxLength(20);
                    prefixEdit.setValue(ChatNotify.config().getPrefix(index));
                    prefixEdit.setResponder((prefix) ->
                            ChatNotify.config().setPrefix(index,
                                    prefix.strip().toLowerCase(Locale.ROOT)));
                    options.add(prefixEdit);

                    options.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        ChatNotify.config().removePrefix(index);
                                        listWidget.refreshScreen();
                                    })
                            .size(25, 20)
                            .pos(width / 2 + 120 + 5, 0)
                            .build());
                }
            }
        }
    }
}