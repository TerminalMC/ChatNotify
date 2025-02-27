/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.gui.widget.list.root;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class PrefixList extends OptionList {
    public PrefixList(Minecraft mc, OptionScreen screen, int width, int height, int y,
                      int entryWidth, int entryHeight, int entrySpacing) {
        super(mc, screen, width, height, y, entryWidth, entryHeight, entrySpacing);
    }

    @Override
    protected void addEntries() {
        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "prefix.list", "ℹ"),
                Tooltip.create(localized("option", "prefix.list.tooltip")), -1));

        int max = Config.get().prefixes.size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new OptionList.Entry.ActionButton(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().prefixes.add("");
                    init();
                }));
    }

    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class PrefixFieldEntry extends PrefixList.Entry {
            PrefixFieldEntry(int x, int width, int height, PrefixList list, int index) {
                super();

                TextField prefixField = new TextField(x, 0, width, height);
                prefixField.setMaxLength(30);
                prefixField.setResponder((prefix) ->
                        Config.get().prefixes.set(index, prefix.strip()));
                prefixField.setValue(Config.get().prefixes.get(index));
                elements.add(prefixField);

                elements.add(Button.builder(
                                Component.literal("❌").withStyle(ChatFormatting.RED),
                                (button) -> {
                                    Config.get().prefixes.remove(index);
                                    list.init();
                                })
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
