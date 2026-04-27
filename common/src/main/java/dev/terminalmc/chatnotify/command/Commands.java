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

package dev.terminalmc.chatnotify.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.gui.screen.RootScreen;
import dev.terminalmc.chatnotify.util.Unicode;
import dev.terminalmc.chatnotify.util.text.DebugParseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Commands {

    public static final List<String> FORMAT_CODES = List.of(
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "l",
            "m",
            "n",
            "o",
            "k",
            "r"
    );

    private Commands() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static <S> void register(CommandDispatcher<S> dispatcher, CommandBuildContext buildCtx) {
        Minecraft mc = Minecraft.getInstance();
        //noinspection unchecked
        dispatcher.register((LiteralArgumentBuilder<S>) literal(ChatNotify.MOD_ID)
                .executes((ctx) -> {
                    mc.schedule(() -> mc.setScreen(new RootScreen(mc.screen)));
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("debug")
                        .then(literal("format")
                                        .then(argument("pattern", StringArgumentType.greedyString())
                                                .suggests(((ctx, builder) -> {
                                                    String[] split = ctx.getInput().split("format ");
                                                    String input = split.length < 2 ? "" : split[1];
                                                    String pre = input.endsWith("$") ? input : input + "$";
                                                    FORMAT_CODES.forEach((s) -> builder.suggest(pre + s));
                                                    return builder.buildFuture();
                                                }))
                                                .executes(ctx -> {
                                                    String pattern = StringArgumentType.getString(
                                                            ctx, "pattern"
                                                    );
                                                    Component text = Component.literal(pattern.replaceAll(
                                                            "\\$",
                                                            Unicode.SECTION.str
                                                    ));

                                    mc.gui.getChat().addClientSystemMessage(text);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        )
                        .then(literal("parse")
                                .then(literal("string")
                                        .then(argument("string", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String string =
                                                            StringArgumentType.getString(
                                                                    ctx,
                                                                    "string"
                                                            );
                                                    Component text =
                                                            DebugParseUtil.parseMutableComponent(string);

                                                    mc.gui.getChat().addClientSystemMessage(text);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(literal("file")
                                        .then(argument("path", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String path = StringArgumentType.getString(
                                                            ctx,
                                                            "path"
                                                    );
                                                    parseFromPath(mc, path);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    private static void parseFromPath(Minecraft mc, String path) {
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            for (String line : lines) {
                Component text = DebugParseUtil.parseMutableComponent(line.strip());
                mc.gui.getChat().addClientSystemMessage(text);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
