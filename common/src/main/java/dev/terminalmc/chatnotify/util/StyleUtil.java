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
import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleUtil {
    private static boolean debug = false;
    
    public static Component restyle(Component msg, String cleanStr, Trigger trig, Matcher matcher,
                                    TextStyle textStyle, boolean restyleAllInstances) {
        debug = Config.get().debugMode == Config.DebugMode.ALL;
        if (!textStyle.isEnabled()) return msg;
        try {
            // Convert message into a format suitable for recursive processing
            msg = FormatUtil.convertToStyledLiteral(msg.copy());
            if (debug) {
                ChatNotify.LOG.warn("Converting message prior to initiating restyle");
                ChatNotify.LOG.warn("Converted text:");
                ChatNotify.LOG.warn(msg.getString());
                ChatNotify.LOG.warn("Converted tree:");
                ChatNotify.LOG.warn(msg.toString());
            }

            // Restyle, using style string if possible
            boolean restyled = false;
            if (trig.styleTarget.enabled && !trig.styleTarget.string.isBlank()) {
                switch(trig.styleTarget.type) {
                    case NORMAL -> {
                        Matcher m = styleSearch(cleanStr, trig.styleTarget.string);
                        if (m.find()) {
                            restyled = true;
                            do {
                                msg = restyleLeaves(msg, textStyle, m.start(), m.end());
                            } while (restyleAllInstances && m.find());
                        }
                    }
                    case REGEX -> {
                        if (trig.styleTarget.pattern != null) {
                            Matcher m = trig.styleTarget.pattern.matcher(cleanStr);
                            if (m.find()) {
                                restyled = true;
                                do {
                                    msg = restyleLeaves(msg, textStyle, m.start(), m.end());
                                } while (restyleAllInstances && m.find());
                            }
                        }
                    }
                }
            }
            // If style string not usable, attempt to restyle trigger
            if (!restyled) {
                if (debug) {
                    ChatNotify.LOG.warn("Style target '{}' (type {})",
                            trig.styleTarget.string, trig.styleTarget.type);
                    ChatNotify.LOG.warn("Defaulting to trigger restyle");
                }
                switch(trig.type) {
                    case NORMAL -> {
                        do {
                            msg = restyleLeaves(msg, textStyle,
                                    matcher.start() + matcher.group(1).length(),
                                    matcher.end() - matcher.group(2).length());
                        } while (restyleAllInstances && matcher.find());
                    }
                    case REGEX -> {
                        do {
                            msg = restyleLeaves(msg, textStyle, matcher.start(), matcher.end());
                        } while (restyleAllInstances && matcher.find());
                    }
                    case KEY -> msg = restyleRoot(msg, textStyle);
                }
            }
        } catch (IllegalArgumentException e) {
            if (debug) ChatNotify.LOG.warn("Restyle error", e);
        }
        return msg;
    }

    /**
     * Performs a case-insensitive substring search for the string within the 
     * message.
     * @param msg the message to search.
     * @param str the string to search for.
     * @return the {@link Matcher} for the search.
     */
    public static Matcher styleSearch(String msg, String str) {
        return Pattern.compile("(?iU)" + Pattern.quote(str)).matcher(msg);
    }

    /**
     * Overwrites the existing root style of the message with the specified
     * style.
     * @param msg the message to restyle.
     * @param style the {@link TextStyle} to apply.
     * @return the restyled message.
     */
    private static Component restyleRoot(Component msg, TextStyle style) {
        return msg.copy().setStyle(applyStyle(msg.getStyle(), style));
    }

    /**
     * Uses a recursive traversal algorithm to apply the specified style to 
     * only the specified part of the message.
     * @param msg the message to restyle.
     * @param style the {@link TextStyle} to apply.
     * @param start the starting index of the string to restyle.
     * @param end the index after the end of the string to restyle.
     * @return the restyled message.
     */
    private static Component restyleLeaves(Component msg, TextStyle style, int start, int end) {
        return recursiveRestyle(msg.copy(), style, start, end, 0);
    }

    /**
     * Recursive traversal restyling algorithm.
     *
     * <p><b>Note:</b> Unable to process format codes or translatable 
     * components, use {@link FormatUtil#convertToStyledLiteral} prior to 
     * invoking this method.</p>
     *
     * @param msg the message to restyle.
     * @param style the style to apply.
     * @param start the root string index of the first character in the target substring.
     * @param end the root string index of the last character in the target substring, plus one.
     * @param index the index of the start of {@code msg} in the root string.
     * @return the message, restyled if applicable.
     */
    private static MutableComponent recursiveRestyle(MutableComponent msg, TextStyle style,
                                                     int start, int end, int index) {
        if (debug) ChatNotify.LOG.warn("recursiveRestyle('{}', {}, {}, {})",
                msg.getString(), start, end, index);

        // Detach siblings
        List<Component> oldSiblings = new ArrayList<>(msg.getSiblings());
        msg.getSiblings().clear();

        // Restyle contents
        if (msg.getContents() instanceof PlainTextContents contents) {
            if (debug) ChatNotify.LOG.warn("PlainTextContents");
            String str = contents.text();
            if (index + str.length() >= start && index < end) {
                // Target string overlaps with current substring, so restyle
                // by splitting into 3 components; before, target, and after
                Style oldStyle = msg.getStyle();
                msg = Component.empty().withStyle(oldStyle);

                int localStart = Math.max(0, start - index);
                int localEnd = Math.min(str.length(), end - index);

                String part1 = str.substring(0, localStart);
                if (!part1.isEmpty()) msg.append(Component.literal(part1));

                String part2 = str.substring(localStart, localEnd);
                if (!part2.isEmpty()) msg.append(
                        Component.literal(part2).withStyle(style.getStyle()));

                String part3 = str.substring(localEnd);
                if (!part3.isEmpty()) msg.append(Component.literal(part3));
            }
            index += str.length();
        }

        // Recurse for original siblings and re-attach
        List<Component> siblings = msg.getSiblings();
        for (Component sibling : oldSiblings) {
            String str = sibling.getString();
            if (index + str.length() >= start && index < end) {
                siblings.add(recursiveRestyle(sibling.copy(), style, start, end, index));
            } else {
                siblings.add(sibling);
            }
            index += str.length();
        }

        return msg;
    }

    /**
     * For each enabled field of the specified {@link TextStyle}, overrides the
     * corresponding {@link Style} field.
     * @param style the {@link Style} to apply to.
     * @param textStyle the {@link TextStyle} to apply.
     * @return the {@link Style}, with the {@link TextStyle} applied.
     */
    private static Style applyStyle(Style style, TextStyle textStyle) {
        if (!textStyle.bold.equals(TextStyle.FormatMode.DISABLED))
            style = style.withBold(textStyle.bold.equals(TextStyle.FormatMode.ON));
        if (!textStyle.italic.equals(TextStyle.FormatMode.DISABLED))
            style = style.withItalic(textStyle.italic.equals(TextStyle.FormatMode.ON));
        if (!textStyle.underlined.equals(TextStyle.FormatMode.DISABLED))
            style = style.withUnderlined(textStyle.underlined.equals(TextStyle.FormatMode.ON));
        if (!textStyle.strikethrough.equals(TextStyle.FormatMode.DISABLED))
            style = style.withStrikethrough(textStyle.strikethrough.equals(TextStyle.FormatMode.ON));
        if (!textStyle.obfuscated.equals(TextStyle.FormatMode.DISABLED))
            style = style.withObfuscated(textStyle.obfuscated.equals(TextStyle.FormatMode.ON));
        if (textStyle.doColor) style = style.withColor(textStyle.getTextColor());
        return style;
    }
}
