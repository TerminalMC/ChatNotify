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

package dev.terminalmc.chatnotify.util;

import com.google.gson.JsonObject;
import dev.terminalmc.chatnotify.ChatNotify;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Handles sending messages to Discord webhooks.
 */
public class DiscordWebhookHandler {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private DiscordWebhookHandler() {
    }

    /**
     * Sends a message to a Discord webhook asynchronously.
     *
     * @param webhookUrl the Discord webhook URL
     * @param content    the message content to send
     * @param triggerMessage the original message that triggered the notification
     */
    public static void sendAsync(String webhookUrl, String content, @Nullable Component triggerMessage) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            ChatNotify.LOG.warn("Discord webhook URL is empty, skipping webhook send");
            return;
        }

        if (content == null || content.isBlank()) {
            ChatNotify.LOG.warn("Discord webhook content is empty, skipping webhook send");
            return;
        }

        // Validate webhook URL format
        if (!isValidWebhookUrl(webhookUrl)) {
            ChatNotify.LOG.error("Invalid Discord webhook URL format: {}", webhookUrl);
            return;
        }

        // Build JSON payload with embed
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Chat triggered");
        
        // Use the trigger message content if available, otherwise use the response content
        String description = triggerMessage != null 
                ? triggerMessage.getString() 
                : content;
        embed.addProperty("description", description);
        embed.addProperty("color", 3303592); // #3268a8
        
        JsonObject footer = new JsonObject();
        // Get server/world name
        Minecraft mc = Minecraft.getInstance();
        String serverName = "Minecraft";
        if (mc.level != null) {
            if (mc.getCurrentServer() != null) {
                // Multiplayer - use server name
                serverName = mc.getCurrentServer().name;
            } else if (mc.getSingleplayerServer() != null) {
                // Singleplayer - use world name
                serverName = mc.getSingleplayerServer().getWorldData().getLevelSettings().levelName();
            }
        }
        footer.addProperty("text", serverName);
        embed.add("footer", footer);
        
        JsonObject payload = new JsonObject();
        com.google.gson.JsonArray embeds = new com.google.gson.JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        String jsonPayload = payload.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        // Send asynchronously
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(statusCode -> {
                    if (statusCode >= 200 && statusCode < 300) {
                        ChatNotify.LOG.info("Successfully sent Discord webhook message");
                    } else if (statusCode == 429) {
                        ChatNotify.LOG.warn("Discord webhook rate limited (HTTP {})", statusCode);
                    } else {
                        ChatNotify.LOG.error("Failed to send Discord webhook message (HTTP {})", statusCode);
                    }
                })
                .exceptionally(throwable -> {
                    ChatNotify.LOG.error("Error sending Discord webhook message", throwable);
                    return null;
                });
    }

    /**
     * Validates that a webhook URL is a valid Discord webhook URL.
     *
     * @param webhookUrl the URL to validate
     * @return true if the URL is a valid Discord webhook URL
     */
    private static boolean isValidWebhookUrl(String webhookUrl) {
        try {
            URI uri = URI.create(webhookUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            
            // Discord webhook URLs must use HTTPS for secure transmission
            // and should be https://discord.com/api/webhooks/... or https://discordapp.com/api/webhooks/...
            return "https".equals(scheme) &&
                   (host != null && (host.equals("discord.com") || host.equals("discordapp.com"))) &&
                   path != null && path.startsWith("/api/webhooks/");
        } catch (Exception e) {
            return false;
        }
    }
}
