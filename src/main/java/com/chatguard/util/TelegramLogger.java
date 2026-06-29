package com.chatguard.util;

import com.chatguard.config.ChatGuardConfig;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.time.format.*;

public class TelegramLogger {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public static void sendViolation(String nick, String word,
                                      String message, String cmd,
                                      ChatGuardConfig.TriggerCategory cat) {
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();
        if (!cfg.telegramEnabled) return;
        if (cfg.telegramBotToken.isEmpty() || cfg.telegramChatId.isEmpty()) return;

        String time = DateTimeFormatter.ofPattern("HH:mm:ss")
                .format(LocalTime.now());

        // Красивое сообщение в Telegram с эмодзи
        String text =
            "🚨 *НАРУШЕНИЕ ОБНАРУЖЕНО*\n" +
            "━━━━━━━━━━━━━━━━━━━\n" +
            "👤 *Игрок:* `" + nick + "`\n" +
            "📂 *Категория:* " + cat.name + "\n" +
            "🔤 *Слово:* `" + word + "`\n" +
            "💬 *Сообщение:* _" + escapeMarkdown(message) + "_\n" +
            "⏱ *Мут:* " + cat.time + " | Правило " + cat.rule + "\n" +
            "📋 *Команда:*\n`" + cmd + "`\n" +
            "━━━━━━━━━━━━━━━━━━━\n" +
            "🕐 " + time;

        // Отправляем в отдельном потоке чтобы не лагал клиент
        new Thread(() -> {
            try {
                String url = "https://api.telegram.org/bot" + cfg.telegramBotToken + "/sendMessage";
                String body = "chat_id=" + URLEncoder.encode(cfg.telegramChatId, "UTF-8")
                        + "&text=" + URLEncoder.encode(text, "UTF-8")
                        + "&parse_mode=Markdown";

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .timeout(Duration.ofSeconds(5))
                        .build();

                HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                System.err.println("[ChatGuard] Ошибка Telegram: " + e.getMessage());
            }
        }).start();
    }

    private static String escapeMarkdown(String text) {
        return text.replace("_", "\\_").replace("*", "\\*")
                   .replace("[", "\\[").replace("`", "\\`");
    }
}
