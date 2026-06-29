package com.chatguard.event;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.gui.ViolationOverlay;
import com.chatguard.util.ChatParser;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ChatEventHandler {

    private static long lastFilePos = 0;
    private static Path logFile = null;
    private static int searchTickCooldown = 0;

    public static void register() {
        // Вместо перехвата чата — читаем лог-файл напрямую как AHK
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Ищем лог-файл раз в 100 тиков если не найден
            if (logFile == null || !Files.exists(logFile)) {
                if (searchTickCooldown-- <= 0) {
                    searchTickCooldown = 100;
                    logFile = findLogFile();
                    if (logFile != null) {
                        // Начинаем читать с конца — старые сообщения не нужны
                        try { lastFilePos = Files.size(logFile); } catch (Exception e) { lastFilePos = 0; }
                        System.out.println("[ChatGuard] Лог найден: " + logFile);
                    }
                }
                return;
            }

            // Читаем новые строки из лога
            try {
                long fileSize = Files.size(logFile);
                if (fileSize < lastFilePos) {
                    // Файл пересоздан (новая сессия)
                    lastFilePos = 0;
                }
                if (fileSize <= lastFilePos) return;

                try (RandomAccessFile raf = new RandomAccessFile(logFile.toFile(), "r")) {
                    raf.seek(lastFilePos);
                    String line;
                    while ((line = raf.readLine()) != null) {
                        // readLine возвращает bytes как ISO-8859-1, конвертируем в UTF-8
                        line = new String(line.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1),
                                java.nio.charset.StandardCharsets.UTF_8);
                        processLogLine(line, client);
                    }
                    lastFilePos = raf.getFilePointer();
                }
            } catch (Exception e) {
                // ignore
            }
        });
    }

    /**
     * Автопоиск лог-файла latest.log
     * Ищет в стандартных местах для Windows/Linux/Mac
     * и рядом с запущенным .jar (для нестандартных лаунчеров)
     */
    private static Path findLogFile() {
        List<Path> candidates = new ArrayList<>();

        String userHome = System.getProperty("user.home", "");
        String appData  = System.getenv("APPDATA");

        // 1. Стандартный .minecraft (Windows)
        if (appData != null)
            candidates.add(Paths.get(appData, ".minecraft", "logs", "latest.log"));

        // 2. .minecraft рядом с user.home (Linux/Mac)
        if (!userHome.isEmpty()) {
            candidates.add(Paths.get(userHome, ".minecraft", "logs", "latest.log"));
            candidates.add(Paths.get(userHome, "Library", "Application Support", "minecraft", "logs", "latest.log"));
        }

        // 3. Текущая рабочая директория (нестандартные лаунчеры — TLauncher, LabyMod и т.д.)
        Path cwd = Paths.get("").toAbsolutePath();
        candidates.add(cwd.resolve("logs").resolve("latest.log"));
        candidates.add(cwd.resolve("latest.log"));

        // 4. Родительская директория (если запущен из папки bin)
        candidates.add(cwd.getParent() != null
                ? cwd.getParent().resolve("logs").resolve("latest.log")
                : null);

        // 5. Через FabricLoader — получить game directory
        try {
            Path gameDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
            candidates.add(gameDir.resolve("logs").resolve("latest.log"));
            // Также проверяем папку выше (некоторые лаунчеры кладут logs рядом)
            candidates.add(gameDir.getParent() != null
                    ? gameDir.getParent().resolve("logs").resolve("latest.log")
                    : null);
        } catch (Exception ignored) {}

        for (Path p : candidates) {
            if (p != null && Files.exists(p)) {
                return p;
            }
        }

        System.err.println("[ChatGuard] Лог-файл не найден! Проверены пути: " + candidates);
        return null;
    }

    private static void processLogLine(String line, MinecraftClient client) {
        // Фильтр: только строки чата
        // Формат: [HH:MM:SS] [Render thread/INFO]: [System] [CHAT] сообщение
        if (!line.contains("[CHAT]")) return;

        // Извлекаем текст после [CHAT]
        int chatIdx = line.indexOf("[CHAT]");
        if (chatIdx < 0) return;
        String chatLine = line.substring(chatIdx + 6).trim();

        // Проверяем на триггеры
        ChatGuardConfig.TriggerCategory cat = ChatParser.findViolation(chatLine);
        if (cat == null) return;

        String nick    = ChatParser.extractNick(chatLine);
        if (nick == null || nick.isEmpty()) return;

        String msgText = ChatParser.extractMessage(chatLine);
        String word    = ChatParser.findTriggeredWord(chatLine, cat);
        String cmd     = "/mute " + nick + " " + cat.time + " " + cat.rule + " | " + cat.reason;

        // Выполняем на главном потоке
        client.execute(() -> {
            if (client.player == null) return;
            client.inGameHud.getChatHud().addMessage(buildAlert(nick, cat, msgText, word, cmd));
            if (ChatGuardConfig.getInstance().soundEnabled) {
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                        ChatGuardConfig.getInstance().soundVolume, 1.5f);
            }
            ViolationOverlay.addAlert(nick, word, cat);
        });
    }

    private static Text buildAlert(String nick, ChatGuardConfig.TriggerCategory cat,
                                    String msgText, String word, String cmd) {
        String sep = "§8§m──────────────────────────────────────§r";
        String shortMsg = msgText.length() > 50 ? msgText.substring(0, 50) + "..." : msgText;

        MutableText t = Text.literal(sep + "\n");
        t.append(Text.literal("§c§l[МОДЕРАЦИЯ] §e⚠ §f§lНарушение обнаружено!\n"));
        t.append(Text.literal("§7Игрок: §f" + nick
                + " §8| §7Причина: §f" + cat.reason
                + " §8| §7Рек. мут: §e" + cat.time + "\n"));
        t.append(Text.literal("§7Сообщение: §f\"" + shortMsg + "\"\n"));
        t.append(Text.literal("§7Детали: Слово: §c\"" + word
                + "\" §8| §7Категория: §f" + cat.name + "\n"));
        t.append(Text.literal("§7Команда: §a" + cmd + " "));

        MutableText btnInsert = Text.literal("§2§l[ВСТАВИТЬ]");
        btnInsert.setStyle(Style.EMPTY
                .withColor(Formatting.GREEN).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§aВставить команду в чат\n§7" + cmd))));
        t.append(btnInsert);
        t.append(Text.literal(" "));

        MutableText btnCopy = Text.literal("§3§l[КОПИРОВАТЬ]");
        btnCopy.setStyle(Style.EMPTY
                .withColor(Formatting.DARK_AQUA).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§bСкопировать в буфер обмена\n§7" + cmd))));
        t.append(btnCopy);
        t.append(Text.literal("\n"));
        t.append(Text.literal(sep));
        return t;
    }
}
