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

package dev.terminalmc.chatnotify.util;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Pattern;

public class FormatUtil {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("\\u00A7.?");
    private static final String PLACEHOLDER_PATTERN_STRING 
            = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_PATTERN_STRING);
    
    /**
     * {@link net.minecraft.util.StringUtil#stripColor} only strips valid 
     * format codes, but invalid codes are also hidden from view so we remove 
     * them as well.
     */
    public static String stripCodes(String str) {
        return COLOR_CODE_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * Recursively converts any {@link TranslatableContents} elements of the 
     * {@link MutableComponent} tree to {@link PlainTextContents} elements, 
     * and in the process converts any format codes to {@link Style}s.
     */
    public static MutableComponent convertToStyledLiteral(MutableComponent text) 
            throws IllegalArgumentException {
        // If contents are translatable, convert to literal
        if (text.getContents() instanceof TranslatableContents) {
            text = convertToLiteral(text);
        }

        // Recurse for all siblings
        text.getSiblings().replaceAll(sibling -> convertToStyledLiteral(sibling.copy()));
        
        // Convert codes in contents
        if (text.getContents() instanceof PlainTextContents) {
            text = convertCodesToStyles(text);
        }
        
        return text;
    }

    /**
     * Converts the contents of the {@link MutableComponent} from 
     * {@link TranslatableContents} to {@link PlainTextContents}.
     * 
     * <p><b>Note:</b> Does not recurse, only affects root. Caller must recurse
     * if required.</p>
     */
    private static MutableComponent convertToLiteral(MutableComponent text) 
            throws IllegalArgumentException {
        if (!(text.getContents() instanceof TranslatableContents contents)) return text;
        
        boolean debug = Config.get().debugMode.equals(Config.DebugMode.ALL);
        
        if (debug) {
            ChatNotify.LOG.warn("Converting message to literal");
            ChatNotify.LOG.warn("Text:");
            ChatNotify.LOG.warn(text.getString());
            ChatNotify.LOG.warn("Tree:");
            ChatNotify.LOG.warn(text.toString());
        }
        
        // Detach siblings
        List<Component> oldSiblings = new ArrayList<>(text.getSiblings());
        
        // Process translatable contents
        Language lang = Language.getInstance();
        String key = contents.getKey();
        @Nullable String fallback = contents.getFallback();

        // This is the unformatted string to be shown to the user
        String string = fallback == null
                ? lang.getOrDefault(key)
                : lang.getOrDefault(key, fallback);
        
        // Minecraft will not attempt to process the placeholders if the string
        // format is invalid, so we check that here
        boolean validFormat = true;
        try {
            String.format(string, contents.getArgs());
        } catch (IllegalFormatException e) {
            validFormat = false;
            text = Component.literal(string).withStyle(text.getStyle());
            if (debug) {
                ChatNotify.LOG.warn("Invalid string format:");
                ChatNotify.LOG.warn(e.getMessage());
                ChatNotify.LOG.warn(string);
            }
        }
        
        if (validFormat) {
            // Split on placeholders to get an array of plain text elements
            List<String> split = new ArrayList<>(List.of(PLACEHOLDER_PATTERN.split(string)));
            
            // Pad the array if necessary for ease of iteration
            if (split.isEmpty()) {
                split.add(""); // Pad start
                split.add(""); // Pad end
            } else if (!string.endsWith(split.getLast())) {
                split.add(""); // Pad end only (start is already padded)
            }

            if (debug) {
                ChatNotify.LOG.warn("Format string:");
                ChatNotify.LOG.warn(string);
                ChatNotify.LOG.warn("Size of split array: {}", split.size());
                ChatNotify.LOG.warn("Size of args array: {}", contents.getArgs().length);
            }

            if (split.size() == 1) {
                // No placeholders, create component from literal string
                text = Component.literal(string).withStyle(text.getStyle());
            } else {
                // Create component by alternating literal elements and args
                // Note: args.length is at least numPlaceholders at this point,
                // else the earlier String.format check would have failed.
                Object[] args = contents.getArgs();
                int numPlaceholders = split.size() - 1;

                // Create an empty component, and add each literal element and 
                // each arg as siblings in sequence
                text = Component.empty().withStyle(text.getStyle());
                List<Component> siblings = text.getSiblings();

                for (int i = 0; i < numPlaceholders; i++) {
                    // Add translated substring
                    if (!split.get(i).isEmpty()) {
                        if (debug) {
                            ChatNotify.LOG.warn("Adding translated substring:");
                            ChatNotify.LOG.warn(split.get(i));
                        }
                        siblings.add(Component.literal(split.get(i)));
                    }
                    // Add subsequent arg
                    if (args[i] instanceof Component argComponent) {
                        if (debug) {
                            ChatNotify.LOG.warn("Adding arg component");
                            ChatNotify.LOG.warn("Text:");
                            ChatNotify.LOG.warn(argComponent.getString());
                            ChatNotify.LOG.warn("Tree:");
                            ChatNotify.LOG.warn(argComponent.toString());
                        }
                        siblings.add(argComponent);
                    } else {
                        if (debug) {
                            ChatNotify.LOG.warn("Adding arg object");
                            ChatNotify.LOG.warn("getClass():");
                            ChatNotify.LOG.warn(args[i].getClass().getName());
                            ChatNotify.LOG.warn("toString():");
                            ChatNotify.LOG.warn(args[i].toString());
                        }
                        siblings.add(Component.literal(args[i].toString()));
                    }
                }
                // Add final translated substring
                if (!split.getLast().isEmpty()) {
                    siblings.add(Component.literal(split.getLast()));
                }
            }
        }
        
        // Re-attach siblings
        text.getSiblings().addAll(oldSiblings);
        
        return text;
    }

    /**
     * Converts any format codes in the literal contents of the 
     * {@link MutableComponent} to {@link Style}s.
     *
     * <p><b>Note:</b> does not recurse, only affects root.</p>
     */
    private static MutableComponent convertCodesToStyles(MutableComponent text) {
        if (!(text.getContents() instanceof PlainTextContents contents)) return text;
        
        // Check whether conversion is required
        String str = contents.text();
        if (!str.contains("\u00A7")) return text;
        
        // Detach siblings
        List<Component> oldSiblings = new ArrayList<>(text.getSiblings());
        
        // Convert
        text = Component.empty().withStyle(text.getStyle());
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        FormatCodes codes = new FormatCodes();
        
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
        
        // Re-attach siblings
        text.getSiblings().addAll(oldSiblings);
        
        return text;
    }
    
    private static class FormatCodes {
        @Nullable ChatFormatting color = null;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        boolean obfuscated = false;
        
        Style createStyle() {
            return new Style(
                    color == null ? null : TextColor.fromLegacyFormat(color),
                    null,
                    bold ? true : null,
                    italic ? true : null,
                    underline ? true : null,
                    strikethrough ? true : null,
                    obfuscated ? true : null,
                    null,
                    null,
                    null,
                    null
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
