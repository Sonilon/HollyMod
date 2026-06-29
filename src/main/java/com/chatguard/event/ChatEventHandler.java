package com.chatguard.event;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.gui.ViolationOverlay;
import com.chatguard.util.ChatParser;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class ChatEventHandler {

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleChatMessage(message);
        });
    }

    private static void handleChatMessage(Text message) {
        String raw = message.getString();

        ChatGuardConfig.TriggerCategory cat = ChatParser.findViolation(raw);
        if (cat == null) return;

        String nick = ChatParser.extractNick(raw);
        if (nick == null || nick.isEmpty()) return;

        String msgText  = ChatParser.extractMessage(raw);
        String word     = ChatParser.findTriggeredWord(raw, cat);

        // Формируем команду мута
        String cmd = "/mute " + nick + " " + cat.time + " " + cat.rule + " | " + cat.reason;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Строим красивое уведомление
        mc.inGameHud.getChatHud().addMessage(buildAlert(nick, cat, msgText, word, cmd));

        // Звук
        if (ChatGuardConfig.getInstance().soundEnabled) {
            mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                    ChatGuardConfig.getInstance().soundVolume, 1.5f);
        }

        // HUD оверлей
        ViolationOverlay.addAlert(nick, word, cat);
    }

    // ============================================================
    //  КРАСИВОЕ УВЕДОМЛЕНИЕ В ЧАТ
    //  Формат как на скриншоте:
    //  ──────────────────────────────────────
    //  [МОДЕРАЦИЯ] ⚠ Нарушение обнаружено!
    //  Игрок: Nick | Причина: ... | Рек. мут: 60m
    //  Сообщение: "текст сообщения"
    //  Детали: Слово: "слово" | Категория: Оскорбления
    //  Команда: /mute Nick 60m ...   [ВСТАВИТЬ] [КОПИРОВАТЬ]
    //  ──────────────────────────────────────
    // ============================================================
    private static Text buildAlert(String nick, ChatGuardConfig.TriggerCategory cat,
                                    String msgText, String word, String cmd) {
        String sep = "§8§m──────────────────────────────────────§r";

        // Строка 1: разделитель
        MutableText t = Text.literal(sep + "\n");

        // Строка 2: заголовок
        t.append(Text.literal("§c§l[МОДЕРАЦИЯ] §e⚠ §f§lНарушение обнаружено!\n"));

        // Строка 3: игрок + причина + время
        t.append(Text.literal("§7Игрок: §f" + nick
                + " §8| §7Причина: §f" + cat.reason
                + " §8| §7Рек. мут: §e" + cat.time + "\n"));

        // Строка 4: сообщение
        String shortMsg = msgText.length() > 50 ? msgText.substring(0, 50) + "..." : msgText;
        t.append(Text.literal("§7Сообщение: §f\"" + shortMsg + "\"\n"));

        // Строка 5: детали
        t.append(Text.literal("§7Детали: §7Слово: §c\"" + word
                + "\" §8| §7Категория: §f" + cat.name + "\n"));

        // Строка 6: команда + кнопки [ВСТАВИТЬ] и [КОПИРОВАТЬ]
        t.append(Text.literal("§7Команда: §a" + cmd + " "));

        // Кнопка [ВСТАВИТЬ] — suggest command (открывает чат с командой)
        MutableText btnInsert = Text.literal("§2§l[ВСТАВИТЬ]");
        btnInsert.setStyle(Style.EMPTY
                .withColor(Formatting.GREEN)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§aВставить команду в чат\n§7" + cmd))));
        t.append(btnInsert);

        t.append(Text.literal(" "));

        // Кнопка [КОПИРОВАТЬ] — copy to clipboard
        MutableText btnCopy = Text.literal("§3§l[КОПИРОВАТЬ]");
        btnCopy.setStyle(Style.EMPTY
                .withColor(Formatting.DARK_AQUA)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§bСкопировать команду в буфер обмена\n§7" + cmd))));
        t.append(btnCopy);

        t.append(Text.literal("\n"));

        // Строка 7: разделитель
        t.append(Text.literal(sep));

        return t;
    }
}
