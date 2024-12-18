/*
 * Copyright 2024 TerminalMC
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

package dev.terminalmc.chatnotify.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class FormatUtil {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("\\u00A7.?");
    
    /**
     * {@link net.minecraft.util.StringUtil#stripColor} only strips valid 
     * format codes, but invalid codes are also hidden from view so we remove 
     * them as well.
     */
    public static String stripCodes(String str) {
        return COLOR_CODE_PATTERN.matcher(str).replaceAll("");
    }
    
    public static MutableComponent convertCodes(MutableComponent text) {
        if (!text.getString().contains("\u00A7")) return text;
        
        // Recurse for all siblings
        List<Component> siblings = text.getSiblings();
        siblings.replaceAll(sibling -> convertCodes(sibling.copy()));
        
        // Restyle contents
        if (text.getContents() instanceof PlainTextContents ptc) {
            // PlainTextContents is typically the lowest level
            text = convertCodes(ptc.text(), text.getStyle());
        }
        else if (text.getContents() instanceof TranslatableContents contents) {
            // Recurse for all args
            Object[] args = contents.getArgs();
            for (int i = 0; i < contents.getArgs().length; i++) {
                if (args[i] instanceof Component argComponent) {
                    args[i] = convertCodes(argComponent.copy());
                }
                else if (args[i] instanceof String argString) {
                    args[i] = convertCodes(argString, text.getStyle());
                }
            }
            // Reconstruct
            text = MutableComponent.create(
                    new TranslatableContents(contents.getKey(), contents.getFallback(), args))
                            .setStyle(text.getStyle());
        }
        
        // Attach restyled siblings
        text.getSiblings().addAll(siblings);
        return text;
    }

    private static MutableComponent convertCodes(String str, Style style) {
        if (!str.contains("\u00A7")) return Component.literal(str);
        
        MutableComponent text = Component.empty();
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        FormatCodes codes = new FormatCodes(style);
        
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\u00A7') { // Section sign
                if (!sb.isEmpty()) {
                    // Clear backlog
                    text.append(Component.literal(sb.toString()).withStyle(codes.createStyle()));
                    sb.setLength(0);
                }
                if (i < chars.length - 1) { // Next char exists
                    char next = chars[++i]; // Skip to next char
                    switch (next) { // Process code
                        case 'r' -> codes.clear();
                        case '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' 
                                -> codes.color = ChatFormatting.getByCode(next);
                        case 'k' -> codes.obfuscated = true;
                        case 'l' -> codes.bold = true;
                        case 'm' -> codes.strikethrough = true;
                        case 'n' -> codes.underline = true;
                        case 'o' -> codes.italic = true;
                        default -> {} // Ignore invalid codes
                    }
                }
            } else { // Not section sign
                sb.append(chars[i]);
            }
        }
        if (!sb.isEmpty()) {
            text.append(Component.literal(sb.toString()).withStyle(codes.createStyle()));
        }
        return text;
    }
    
    private static class FormatCodes {
        @Nullable ChatFormatting color = null;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        boolean obfuscated = false;
        private final Style baseStyle;
        
        FormatCodes(Style baseStyle) {
            this.baseStyle = baseStyle;
        }
        
        Style createStyle() {
            return new Style(
                    color == null ? null : TextColor.fromLegacyFormat(color),
                    bold ? true : null,
                    italic ? true : null,
                    underline ? true : null,
                    strikethrough ? true : null,
                    obfuscated ? true : null,
                    baseStyle.getClickEvent(),
                    baseStyle.getHoverEvent(),
                    baseStyle.getInsertion(), 
                    baseStyle.getFont()
            );
        }
        
        void clear() {
            color = null;
            bold = false;
            italic = false;
            underline = false;
            strikethrough = false;
            obfuscated = false;
        }
    }
}
