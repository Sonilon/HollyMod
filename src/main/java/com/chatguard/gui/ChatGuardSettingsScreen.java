package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ChatGuardSettingsScreen extends Screen {

    private final Screen parent;

    public ChatGuardSettingsScreen(Screen parent) {
        super(Text.literal("ChatGuard — Настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width/2, bw = 220, bh = 20, top = 58, gap = 26;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§e⚡ Категории нарушений"),
                btn -> client.setScreen(new TriggerListScreen(this))
        ).dimensions(cx - bw/2, top, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(
                soundLabel(),
                btn -> {
                    ChatGuardConfig.getInstance().soundEnabled = !ChatGuardConfig.getInstance().soundEnabled;
                    ChatGuardConfig.save();
                    btn.setMessage(soundLabel());
                }
        ).dimensions(cx - bw/2, top + gap, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§b✎ Формат команды мута"),
                btn -> client.setScreen(new MuteFormatScreen(this))
        ).dimensions(cx - bw/2, top + gap*2, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(
                tgLabel(),
                btn -> client.setScreen(new TelegramSettingsScreen(this))
        ).dimensions(cx - bw/2, top + gap*3, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c✖ Закрыть"),
                btn -> client.setScreen(parent)
        ).dimensions(cx - 50, top + gap*4 + 10, 100, bh).build());
    }

    private Text soundLabel() {
        return ChatGuardConfig.getInstance().soundEnabled
                ? Text.literal("§a🔊 Звук: ВКЛ")
                : Text.literal("§7🔇 Звук: ВЫКЛ");
    }

    private Text tgLabel() {
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();
        boolean ok = cfg.telegramEnabled && !cfg.telegramBotToken.isEmpty();
        return ok
                ? Text.literal("§a✈ Telegram: ВКЛ")
                : Text.literal("§7✈ Telegram: настроить");
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFF00E676);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§a§lChat§f§lGuard"), width/2, 13, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Помощник модератора  §8|  §7[M / ь] = открыть"),
                width/2, 27, 0xFF90A4AE);

        ctx.fill(0, height-2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    @Override public boolean shouldPause() { return false; }
}
