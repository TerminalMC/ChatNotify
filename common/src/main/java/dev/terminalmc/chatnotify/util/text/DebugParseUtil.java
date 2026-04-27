/*
 * Copyright 2026 TerminalMC
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

package dev.terminalmc.chatnotify.util.text;

import dev.terminalmc.chatnotify.ChatNotify;
import net.minecraft.network.chat.ClickEvent.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebugParseUtil {

    /**
     * Attempts to reverse {@link MutableComponent#toString}.
     * <p/>
     * Behavior is undefined if the string contains any mismatched brackets or braces.
     */
    public static MutableComponent parseMutableComponent(String string) {
        //
        // Guards!
        //
        string = string.strip();
        if (string.length() < 5) {
            ChatNotify.LOG.warn("Stripped string is less than 5 characters: {}", string);
            return Component.literal(string);
        }

        //
        // Scan for mismatched brackets or braces
        //
        int bracketDepth = 0;
        int braceDepth = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '[' -> bracketDepth++;
                case ']' -> bracketDepth--;
                case '{' -> braceDepth++;
                case '}' -> braceDepth--;
            }
        }
        if (bracketDepth != 0 || braceDepth != 0) {
            ChatNotify.LOG.error("String contains mismatched brackets or braces: {}", string);
            return Component.literal(string);
        }

        //
        // Split into core and meta
        //
        String coreStr = string;
        String metaStr = null;

        if (string.endsWith("]")) {
            int depth = 1;

            int i;
            for (i = string.length() - 2; i >= 0; i--) {
                char c = string.charAt(i);
                switch (c) {
                    case ']' -> depth++;
                    case '[' -> depth--;
                }
                if (depth == 0)
                    break;
            }

            coreStr = string.substring(0, i);
            metaStr = string.substring(i + 1, string.length() - 1);
        }

        //
        // Parse core
        //
        MutableComponent core;
        if (coreStr.startsWith("empty")) {
            // empty
            core = Component.empty();
        } else if (coreStr.startsWith("literal{")) {
            // literal{<string>}
            core = Component.literal(coreStr.substring(8, coreStr.length() - 1));
        } else if (coreStr.startsWith("translation{")) {
            // translation{key='<string>'[, fallback='<string>'], args=[<arg1>, <arg2>]}
            String substring = coreStr.substring(12, coreStr.length() - 1);
            String pattern = "^key='(.*?)', (?:fallback='(.*?)', )?args=\\[(.*?)]$";
            Matcher matcher = Pattern.compile(pattern).matcher(substring);
            if (matcher.matches()) {
                String key = matcher.group(1);
                String fallback = matcher.group(2);
                List<String> argStrings = splitTopLevel(matcher.group(3), ",");

                Object[] args = new Object[argStrings.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = parseMutableComponent(argStrings.get(i).strip());
                }

                if (fallback == null) {
                    core = Component.translatable(key, args);
                } else {
                    core = Component.translatableWithFallback(key, fallback, args);
                }
            } else {
                ChatNotify.LOG.error(
                        "Translation string does not match regex pattern: {}",
                        substring
                );
                core = Component.literal(coreStr);
            }
        } else if (coreStr.startsWith("score{")) {
            // score{name='<name>', objective='<objective>'}
            String substring = coreStr.substring(6, coreStr.length() - 1);
            String pattern = "^name='(.*?)', objective='(.*?)'$";
            Matcher matcher = Pattern.compile(pattern).matcher(substring);
            if (matcher.matches()) {
                String name = matcher.group(1);
                String objective = matcher.group(2);

                core = Component.score(name, objective);
            } else {
                ChatNotify.LOG.error("Score string does not match regex pattern: {}", substring);
                core = Component.literal(coreStr);
            }
        } else if (coreStr.startsWith("keybind{")) {
            // keybind{<name>}
            String substring = coreStr.substring(8, coreStr.length() - 1);
            core = Component.keybind(substring);
        } else if (coreStr.startsWith("pattern{")) {
            // pattern{<pattern>}
            String substring = coreStr.substring(8, coreStr.length() - 1);
            core = Component.literal("PATTERN:" + substring);
        } else {
            // nbt
            core = Component.literal(coreStr);
        }

        //
        // Parse meta
        //
        if (metaStr != null) {
            String pattern = "^(?:style=\\{(.*)}(?:, )?)?(?:siblings=\\[(.*)])?$";
            Matcher matcher = Pattern.compile(pattern).matcher(metaStr);
            if (matcher.matches()) {
                String styleStr = matcher.group(1);
                String siblingsStr = matcher.group(2);

                if (styleStr != null) {
                    core.setStyle(parseStyle(styleStr));
                }

                if (siblingsStr != null) {
                    List<String> siblings = splitTopLevel(siblingsStr, ",");
                    for (String sibling : siblings) {
                        core.append(parseMutableComponent(sibling.strip()));
                    }
                }
            } else {
                ChatNotify.LOG.error("Meta string does not match regex pattern: {}", metaStr);
                return Component.literal(string);
            }
        }

        return core;
    }

    /**
     * Attempts to reverse {@link Style#toString}.
     * <p/>
     * Behavior is undefined if the string contains any mismatched brackets or braces.
     */
    public static Style parseStyle(String string) {
        Style style = Style.EMPTY;
        List<String> split = splitTopLevel(string, ",");

        for (String s : split) {
            if (s.isBlank())
                continue;

            if (s.startsWith("color=")) {
                style = style.withColor(TextColor.parseColor(s.substring(6)).getOrThrow());
            } else if (s.startsWith("clickEvent=")) {
                String substring = s.substring(11);
                String pattern = "^ClickEvent\\{action=(\\w+), value='(.*)'}$";
                Matcher matcher = Pattern.compile(pattern).matcher(substring);
                if (matcher.matches()) {
                    String action = matcher.group(1);
                    String value = matcher.group(2);
                    style = style.withClickEvent(switch (Action.valueOf(action)) {
                        case OPEN_URL -> new OpenUrl(URI.create(value));
                        case OPEN_FILE -> new OpenFile(value);
                        case RUN_COMMAND -> new RunCommand(value);
                        case SUGGEST_COMMAND -> new SuggestCommand(value);
                        case SHOW_DIALOG -> new SuggestCommand("SHOW_DIALOG:" + value);
                        case CHANGE_PAGE -> new SuggestCommand("CHANGE_PAGE:" + value);
                        case COPY_TO_CLIPBOARD -> new CopyToClipboard(value);
                        case CUSTOM -> new SuggestCommand("CUSTOM:" + value);
                    });
                } else {
                    ChatNotify.LOG.warn(
                            "ClickEvent string does not match regex pattern: {}",
                            substring
                    );
                    style = style.withClickEvent(new SuggestCommand(
                            "Sample click text"
                    ));
                }
            } else if (s.startsWith("hoverEvent=")) {
                style = style.withHoverEvent(new ShowText(Component.literal("Sample hover text")));
            } else if (s.startsWith("insertion=")) {
                style = style.withInsertion(s.substring(10));
            } else if (s.startsWith("font=")) {
                style = style.withFont(new Resource(Identifier.parse(s.substring(5))));
            } else if (s.equals("bold")) {
                style = style.withBold(true);
            } else if (s.equals("italic")) {
                style = style.withItalic(true);
            } else if (s.equals("underlined")) {
                style = style.withUnderlined(true);
            } else if (s.equals("strikethrough")) {
                style = style.withStrikethrough(true);
            } else if (s.equals("obfuscated")) {
                style = style.withObfuscated(true);
            }
        }

        return style;
    }

    /**
     * Splits the string on occurrences of the delimiter that occur outside any brackets or braces.
     * <p/>
     * Assumes the input string does not contain mismatched brackets or braces.
     *
     * @param string    the string to split.
     * @param delimiter the delimiter to split on.
     * @return the list of parts.
     */
    public static List<String> splitTopLevel(String string, String delimiter) {
        List<String> parts = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int bracketDepth = 0;
        int braceDepth = 0;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '[' -> bracketDepth++;
                case ']' -> bracketDepth--;
                case '{' -> braceDepth++;
                case '}' -> braceDepth--;
            }

            if (bracketDepth == 0 && braceDepth == 0 && string.startsWith(delimiter, i)) {
                // Encountered a top-level delimiter, add builder as new part and reset
                parts.add(builder.toString());
                builder.setLength(0);
                i += delimiter.length() - 1;
            } else {
                builder.append(c);
            }
        }

        // Add final part
        if (!builder.isEmpty()) {
            parts.add(builder.toString());
        }

        return parts;
    }
}
