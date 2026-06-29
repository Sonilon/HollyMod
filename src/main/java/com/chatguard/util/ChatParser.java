package com.chatguard.util;

import com.chatguard.config.ChatGuardConfig;

import java.util.regex.*;

public class ChatParser {

    /**
     * Парсим строку ИЗ ЛОГ-ФАЙЛА (уже после "[CHAT] ")
     * Примеры из реальных логов:
     *   ʟ | «стᴀжᴇр» 1pirs Обмен? Ко мне!: test
     *   [ALL] ʟ | «стᴀжᴇр» 1pirs Обмен? Ко мне!: test
     *   ʟ | 58z: привет
     *
     * Ник всегда идёт СРАЗУ после закрывающей » или после |
     * Символы «» в логах — реальные Unicode U+00AB / U+00BB
     */
    public static String extractNick(String chatLine) {
        // Берём часть до первого двоеточия
        int colon = chatLine.indexOf(':');
        if (colon < 0) return null;
        String before = chatLine.substring(0, colon);

        // Паттерн 1: после » (U+00BB или обычный »)
        Matcher m1 = Pattern.compile("[\u00BB»]\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m1.find()) return m1.group(1);

        // Паттерн 2: после | (пайпа)
        Matcher m2 = Pattern.compile("\\|\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m2.find()) return m2.group(1);

        return null;
    }

    /**
     * Ищет нарушение в тексте после первого ':'
     * Строка должна содержать | или » — признак что это сообщение игрока
     */
    public static ChatGuardConfig.TriggerCategory findViolation(String chatLine) {
        if (chatLine == null) return null;

        int colon = chatLine.indexOf(':');
        if (colon < 0 || colon >= chatLine.length() - 1) return null;

        String before  = chatLine.substring(0, colon);
        String msgPart = chatLine.substring(colon + 1).trim();

        // Фильтр: только сообщения игроков
        boolean isPlayer = before.contains("|") || before.contains("»") || before.contains("\u00BB");
        if (!isPlayer) return null;

        String lower = msgPart.toLowerCase();
        for (ChatGuardConfig.TriggerCategory cat : ChatGuardConfig.getInstance().categories) {
            if (!cat.enabled || cat.words == null) continue;
            for (String word : cat.words) {
                if (word == null || word.isEmpty()) continue;
                if (lower.contains(word.toLowerCase())) return cat;
            }
        }
        return null;
    }

    public static String extractMessage(String chatLine) {
        int colon = chatLine.indexOf(':');
        if (colon >= 0 && colon < chatLine.length() - 1)
            return chatLine.substring(colon + 1).trim();
        return chatLine;
    }

    public static String findTriggeredWord(String chatLine, ChatGuardConfig.TriggerCategory cat) {
        int colon = chatLine.indexOf(':');
        if (colon < 0) return "?";
        String msgPart = chatLine.substring(colon + 1).trim().toLowerCase();
        for (String word : cat.words) {
            if (msgPart.contains(word.toLowerCase())) return word;
        }
        return "?";
    }
}
