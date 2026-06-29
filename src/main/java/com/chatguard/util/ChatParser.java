package com.chatguard.util;

import com.chatguard.config.ChatGuardConfig;

import java.util.regex.*;

public class ChatParser {

    /**
     * Парсим из getString() — Minecraft уже убрал цветовые коды.
     *
     * Форматы из логов сервера:
     *   [ALL] ? | «стажер» 1pirs Обмен? Ко мне!: сообщение
     *   ? | «стажер» 1pirs Обмен? Ко мне!: сообщение
     *   ? | 58z: сообщение
     *   [ALL] ? | «?x????????» Blamixxx: сообщение
     *
     * Ник ВСЕГДА идёт:
     *   - сразу после закрывающей » (если есть титул)
     *   - сразу после | (если нет титула)
     */
    public static String extractNick(String raw) {
        String clean = stripColors(raw).trim();
        int colon = clean.indexOf(':');
        if (colon < 0) return null;
        String before = clean.substring(0, colon);

        // После » (закрывающая «ёлочка» — Unicode \u00BB или обычный »)
        Matcher m1 = Pattern.compile("[»\u00BB]\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m1.find()) return m1.group(1);

        // После | (пайпа)
        Matcher m2 = Pattern.compile("\\|\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m2.find()) return m2.group(1);

        return null;
    }

    /**
     * Ищет нарушение в тексте сообщения (часть после первого ':').
     * Проверяет категории по порядку (приоритет).
     * Возвращает сработавшую категорию или null.
     */
    public static ChatGuardConfig.TriggerCategory findViolation(String raw) {
        if (raw == null) return null;
        String clean = stripColors(raw).trim();

        // Это должно быть сообщение игрока — перед ':' должен быть | или »
        int colon = clean.indexOf(':');
        if (colon < 0 || colon >= clean.length() - 1) return null;

        String before = clean.substring(0, colon);
        boolean isChat = before.contains("|") || before.contains("»") || before.contains("\u00BB");
        if (!isChat) return null;

        String msgPart = clean.substring(colon + 1).trim().toLowerCase();

        for (ChatGuardConfig.TriggerCategory cat : ChatGuardConfig.getInstance().categories) {
            if (!cat.enabled || cat.words == null) continue;
            for (String word : cat.words) {
                if (word == null || word.isEmpty()) continue;
                if (msgPart.contains(word.toLowerCase())) {
                    return cat;
                }
            }
        }
        return null;
    }

    public static String extractMessage(String raw) {
        String clean = stripColors(raw).trim();
        int colon = clean.indexOf(':');
        if (colon >= 0 && colon < clean.length() - 1)
            return clean.substring(colon + 1).trim();
        return clean;
    }

    /** Найти какое слово сработало */
    public static String findTriggeredWord(String raw, ChatGuardConfig.TriggerCategory cat) {
        String clean = stripColors(raw).trim();
        int colon = clean.indexOf(':');
        if (colon < 0) return "?";
        String msgPart = clean.substring(colon + 1).trim().toLowerCase();
        for (String word : cat.words) {
            if (msgPart.contains(word.toLowerCase())) return word;
        }
        return "?";
    }

    private static String stripColors(String s) {
        return s.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
