package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.util.TelegramLogger;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class TelegramSettingsScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget tokenField;
    private TextFieldWidget chatIdField;

    public TelegramSettingsScreen(Screen parent) {
        super(Text.literal("✈ Telegram уведомления"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();

        tokenField = new TextFieldWidget(textRenderer, cx - 150, 75, 300, 20,
                Text.literal("Bot Token"));
        tokenField.setText(cfg.telegramBotToken);
        tokenField.setMaxLength(256);
        tokenField.setSuggestion(cfg.telegramBotToken.isEmpty() ? "123456:ABC-DEF..." : "");
        addDrawableChild(tokenField);

        chatIdField = new TextFieldWidget(textRenderer, cx - 150, 115, 300, 20,
                Text.literal("Chat ID"));
        chatIdField.setText(cfg.telegramChatId);
        chatIdField.setMaxLength(64);
        chatIdField.setSuggestion(cfg.telegramChatId.isEmpty() ? "-1001234567890" : "");
        addDrawableChild(chatIdField);

        // Вкл/выкл
        addDrawableChild(ButtonWidget.builder(
                tgLabel(),
                btn -> {
                    cfg.telegramEnabled = !cfg.telegramEnabled;
                    ChatGuardConfig.save();
                    btn.setMessage(tgLabel());
                }
        ).dimensions(cx - 100, 145, 200, 20).build());

        // Тест
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§b▶ Отправить тест"),
                btn -> {
                    saveFields();
                    TelegramLogger.sendViolation(
                        "TestPlayer", "тест",
                        "это тестовое сообщение",
                        "/mute TestPlayer 60m 3.10 | Тест ChatGuard",
                        new ChatGuardConfig.TriggerCategory("Тест", "60m", "3.10", "Тест ChatGuard")
                    );
                }
        ).dimensions(cx - 100, 172, 200, 20).build());

        // Сохранить
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"),
                btn -> { saveFields(); client.setScreen(parent); }
        ).dimensions(cx - 105, 200, 100, 20).build());

        // Назад
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7← Назад"),
                btn -> client.setScreen(parent)
        ).dimensions(cx + 5, 200, 100, 20).build());
    }

    private void saveFields() {
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();
        cfg.telegramBotToken = tokenField.getText().trim();
        cfg.telegramChatId   = chatIdField.getText().trim();
        ChatGuardConfig.save();
    }

    private Text tgLabel() {
        return ChatGuardConfig.getInstance().telegramEnabled
                ? Text.literal("§a✈ Telegram: ВКЛ")
                : Text.literal("§7✈ Telegram: ВЫКЛ");
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF29B6F6);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFF29B6F6);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§b✈ §f§lTelegram §b§lУведомления"), width/2, 13, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Логирование нарушений в Telegram-бот"),
                width/2, 27, 0xFF90A4AE);

        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§bBot Token §8(от @BotFather):"), cx()-150, 62, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§bChat ID §8(ваш канал/чат):"), cx()-150, 102, 0xFFCCCCCC);

        // Подсказка как получить Chat ID
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8Получить Chat ID: напишите боту @userinfobot"),
                width/2, 228, 0xFF546E7A);

        ctx.fill(0, height-2, width, height, 0xFF29B6F6);
        super.render(ctx, mx, my, delta);
    }

    private int cx() { return width / 2; }

    @Override public boolean shouldPause() { return false; }
}
