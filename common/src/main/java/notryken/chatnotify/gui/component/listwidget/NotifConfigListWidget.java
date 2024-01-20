package notryken.chatnotify.gui.component.listwidget;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import notryken.chatnotify.gui.screen.NotifConfigScreen;
import notryken.chatnotify.util.ColorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@code ConfigListWidget} containing controls for the specified
 * {@code Notification}, including references to other screens.
 */
public class NotifConfigListWidget extends ConfigListWidget {
    private final Notification notif;

    public NotifConfigListWidget(Minecraft minecraft, int width, int height,
                                 int top, int bottom, int itemHeight, ConfigScreen parent,
                                 Notification notif) {
        super(minecraft, width, height, top, bottom, itemHeight, parent);
        this.notif = notif;

        int eX = width / 2 - 120;
        int eY = 0;
        int eW = 240;
        int eH = 20;

        Entry notifTriggerHeaderEntry = new Entry();
        StringWidget notifTriggerHeader = new StringWidget(eX, eY, eW, eH,
                Component.literal("Notification Trigger \u2139"), minecraft.font);
        notifTriggerHeader.setTooltip(Tooltip.create(Component.literal(
                "A trigger is a word or series of words that, if detected in a chat " +
                        "message, will activate the notification. NOT case-sensitive.")));
        notifTriggerHeaderEntry.elements.add(notifTriggerHeader);
        addEntry(notifTriggerHeaderEntry);

        if (!notif.equals(ChatNotify.config().getNotif(0))) {
            Entry triggerTypeButtonEntry = new Entry();
            triggerTypeButtonEntry.elements.add(CycleButton.booleanBuilder(
                    Component.literal("Event Key"), Component.literal("Word/Phrase"))
                    .withInitialValue(notif.triggerIsKey)
                    .create(eX, eY, eW, eH, Component.literal("Type"),
                            (button, status) -> {
                                notif.triggerIsKey = status;
                                reloadScreen();
                            }));
            addEntry(triggerTypeButtonEntry);
        }

        if (this.notif.triggerIsKey) {
            Entry keyTriggerWarningEntry = new Entry();
            keyTriggerWarningEntry.elements.add(new StringWidget(eX, eY, eW, eH,
                    Component.literal("May not work on some servers."), minecraft.font));
            addEntry(keyTriggerWarningEntry);

            addEntry(new Entry.TriggerField(eX, eY, eW, eH, notif, this, 0));

            List<Pair<String,String>> keys = List.of(
                    new Pair<>(".", "Any Message"),
                    new Pair<>("commands.message.display", "Private Message"),
                    new Pair<>("multiplayer.player.joined", "Player Joined"),
                    new Pair<>("multiplayer.player.left", "Player Left"),
                    new Pair<>("chat.type.advancement", "Advancement"),
                    new Pair<>("death.", "Player/Pet Died")
            );
            for (int i = 0; i < keys.size(); i++) {
                // Requires an even number of keys
                addEntry(new Entry.KeyTriggerButton(eX, eY, eW, eH, notif, this, keys.get(i), keys.get(i+1)));
                i++;
            }
        }
        else {
            for (int i = 0; i < this.notif.getTriggers().size(); i++) {
                addEntry(new Entry.TriggerField(eX, eY, eW, eH, notif, this, i));
            }
            Entry addTriggerButtonEntry = new Entry();
            addTriggerButtonEntry.elements.add(Button.builder(Component.literal("+"),
                            (button) -> {
                                notif.addTrigger("");
                                reloadScreen();
                            })
                    .pos(eX, eY).size(eW, eH).build());
            addEntry(addTriggerButtonEntry);
        }

        Entry notifOptionsHeaderEntry = new Entry();
        notifOptionsHeaderEntry.elements.add(new StringWidget(eX, eY, eW, eH,
                Component.literal("Notification Options"), minecraft.font));
        addEntry(notifOptionsHeaderEntry);

        Entry soundOptionsEntry = new Entry();
        soundOptionsEntry.elements.add(Button.builder(
                        Component.literal("Sound: " + notif.getSound().toString()),
                        (button) -> openSoundConfig())
                .pos(eX, eY).size(eW - 30, eH).build());
        soundOptionsEntry.elements.add(CycleButton.onOffBuilder()
                .displayOnlyValue()
                .withInitialValue(notif.getControl(2))
                .create(eX + eW - 25, eY, 25, eH, Component.empty(),
                        (button, status) -> {
                            notif.setControl(2, status);
                            reloadScreen();
                        }));
        addEntry(soundOptionsEntry);

        Entry colorOptionsEntry = new Entry();
        MutableComponent message = Component.literal("Text Color");
        if (notif.getColor() != null) message.setStyle(Style.EMPTY.withColor(notif.getColor()));
        colorOptionsEntry.elements.add(Button.builder(message, (button) -> openColorConfig())
                .pos(eX, eY).size(eW / 2, eH).build());
        EditBox colorEdit = new EditBox(minecraft.font, eX + eW / 2 + 6, eY, eW * 0.27, 20,
                Component.literal("Hex Color"));
        colorEdit.setMaxLength(7);
        colorEdit.setResponder(color -> notif.setColor(ColorUtil.parseColor(color)));
        if (notif.getColor() != null) {
            colorEdit.setValue(notif.getColor().formatValue());
        }
        colorOptionsEntry.elements.add(colorEdit);
        colorOptionsEntry.elements.add(Button.builder(Component.literal("\ud83d\uddd8"),
                        (button) -> reloadScreen())
                .tooltip(Tooltip.create(Component.literal("Check Value")))
                .pos(width / 2 + 70, eY)
                .size(20, eH)
                .build());
        colorOptionsEntry.elements.add(CycleButton.onOffBuilder().displayOnlyValue()
                .withInitialValue(notif.getControl(0))
                .create(eX + width + 5, eY, 25, eH, Component.empty(),
                        (button, status) -> {
                            notif.setControl(0, status);
                            reloadScreen();
                        }));
        addEntry(colorOptionsEntry);




        addEntry(new Entry.ColorConfigOption(this.width, this.notif, this.minecraft, this));
        addEntry(new Entry.FormatOptionPrimary(this.width, this.notif, this));
        addEntry(new Entry.FormatOptionSecondary(this.width, this.notif, this));

        Entry advancedOptionsHeaderEntry = new Entry();
        StringWidget advancedOptionsHeader = new StringWidget(eX, eY, eW, eH,
                Component.literal("Advanced Settings"), minecraft.font);
        advancedOptionsHeaderEntry.elements.add(advancedOptionsHeader);
        addEntry(advancedOptionsHeaderEntry);

        Entry advancedOptionsButtonEntry = new Entry();
        Button advancedOptionsButton = Button.builder(Component.literal("Here be Dragons!"),
                        (button) -> openAdvancedConfig())
                .pos(eX, eY).size(eW, eH).build();
        advancedOptionsButtonEntry.elements.add(advancedOptionsButton);
        addEntry(advancedOptionsButtonEntry);
    }

    @Override
    public NotifConfigListWidget resize(int width, int height, int top, int bottom) {
        NotifConfigListWidget listWidget = new NotifConfigListWidget(
                minecraft, width, height, top, bottom, itemHeight, parent, title, notif);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reloadScreen() {
        this.reloadScreen(this);
    }

    private void openColorConfig() {
        assert minecraft.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.color");
        minecraft.setScreen(new NotifConfigScreen(minecraft.screen, title,
                new ColorConfigListWidget(minecraft,
                        minecraft.screen.width, minecraft.screen.height,
                        32, minecraft.screen.height - 32, 25,
                        minecraft.screen, title, notif)));
    }

    private void openSoundConfig() {
        assert minecraft.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.sound");
        minecraft.setScreen(new NotifConfigScreen(minecraft.screen, title,
                new SoundConfigListWidget(minecraft,
                        minecraft.screen.width, minecraft.screen.height,
                        32, minecraft.screen.height - 32, 21,
                        minecraft.screen, title, notif)));
    }

    private void openAdvancedConfig() {
        assert minecraft.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.advanced");
        minecraft.setScreen(new NotifConfigScreen(minecraft.screen, title,
                new AdvancedConfigListWidget(minecraft,
                        minecraft.screen.width, minecraft.screen.height,
                        32, minecraft.screen.height - 32, 25,
                        minecraft.screen, title, notif)));
    }

    private static class Entry extends ConfigListWidget.Entry {

        private static class TriggerField extends Entry {
            TriggerField(int x, int y, int width, int height, Notification notif,
                         NotifConfigListWidget listWidget, int index) {
                super();

                EditBox triggerEdit = new EditBox(
                        Minecraft.getInstance().font, x, y, width, height,
                        Component.literal("Notification Trigger"));
                triggerEdit.setMaxLength(120);
                triggerEdit.setValue(notif.getTrigger(index));
                triggerEdit.setResponder((trigger) ->
                        notif.setTrigger(index, trigger.strip()));
                elements.add(triggerEdit);

                if (index != 0) {
                    elements.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeTrigger(index);
                                        listWidget.reloadScreen();
                                    })
                            .size(25, 20)
                            .pos(width / 2 + 120 + 5, 0)
                            .build());
                }
            }
        }

        private static class KeyTriggerButton extends Entry {
            KeyTriggerButton(int x, int y, int width, int height, Notification notif,
                             NotifConfigListWidget listWidget,
                             Pair<String,String> keyLeft, Pair<String,String>  keyRight) {
                super();
                int buttonWidth = width / 2 - 6;
                elements.add(Button.builder(Component.literal(keyLeft.getSecond()),
                        (button) -> {
                            notif.setTrigger(keyLeft.getFirst());
                            listWidget.reloadScreen();
                        })
                        .pos(x, y).size(buttonWidth, height).build());
                elements.add(Button.builder(Component.literal(keyRight.getSecond()),
                                (button) -> {
                                    notif.setTrigger(keyRight.getFirst());
                                    listWidget.reloadScreen();
                                })
                        .pos(x + buttonWidth + 6, y).size(buttonWidth, height).build());
            }
        }

        private static class SoundConfigOption extends Entry {
            SoundConfigOption(int width, Notification notif,
                              NotifConfigListWidget listWidget) {
                super();
                elements.add(Button.builder(
                                Component.literal("Sound: " + notif.getSound().toString()),
                                (button) -> listWidget.openSoundConfig())
                        .size(210, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
                elements.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(2))
                        .create(width / 2 + 95, 0, 25, 20, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(2, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }

        private static class ColorConfigOption extends Entry {
            ColorConfigOption(int width, Notification notif, Minecraft minecraft,
                              NotifConfigListWidget listWidget) {
                super();


            }
        }

        private static class FormatOptionPrimary extends Entry {
            FormatOptionPrimary(int width, Notification notif,
                                NotifConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(0))
                        .create(width / 2 - 120, 0, 76, 20, Component.literal("Bold")
                                .withStyle(Style.EMPTY.withBold(
                                        notif.getFormatControl(0))),
                                (button, status) -> {
                                    notif.setFormatControl(0, status);
                                    listWidget.reloadScreen();
                                }));
                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(1))
                        .create(width / 2 - 38, 0, 76, 20, Component.literal("Italic")
                                        .withStyle(Style.EMPTY.withItalic(
                                                notif.getFormatControl(1))),
                                (button, status) -> {
                                    notif.setFormatControl(1, status);
                                    listWidget.reloadScreen();
                                }));
                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(2))
                        .create(width / 2 + 44, 0, 76, 20, Component.literal("Underline")
                                        .withStyle(Style.EMPTY.withUnderlined(
                                                notif.getFormatControl(2))),
                                (button, status) -> {
                                    notif.setFormatControl(2, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }

        private static class FormatOptionSecondary extends Entry {
            FormatOptionSecondary(int width, Notification notif,
                                  NotifConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(3))
                        .create(width / 2 - 120, 0, 117, 20, Component.literal("Strikethrough")
                                        .withStyle(Style.EMPTY.withStrikethrough(
                                                notif.getFormatControl(3))),
                                (button, status) -> {
                                    notif.setFormatControl(3, status);
                                    listWidget.reloadScreen();
                                }));
                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(4))
                        .create(width / 2 + 3, 0, 117, 20, Component.literal("Obfuscate")
                                        .withStyle(Style.EMPTY.withObfuscated(
                                                notif.getFormatControl(4))),
                                (button, status) -> {
                                    notif.setFormatControl(4, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }
    }
}