package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ViolationOverlay {

    private static final List<ViolationAlert> alerts = new ArrayList<>();
    private static final int MAX_ALERTS    = 4;
    private static final int ALERT_DURATION = 7000;

    public static void addAlert(String nick, String word, ChatGuardConfig.TriggerCategory cat) {
        if (alerts.size() >= MAX_ALERTS) alerts.remove(0);
        alerts.add(new ViolationAlert(nick, word, cat, System.currentTimeMillis()));
    }

    public static void render(DrawContext ctx, int sw, int sh, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        long now = System.currentTimeMillis();
        alerts.removeIf(a -> now - a.startTime > ALERT_DURATION);

        int y = 10;
        for (ViolationAlert alert : alerts) {
            long elapsed  = now - alert.startTime;
            float fadeIn  = Math.min(1f, elapsed / 250f);
            float fadeOut = elapsed > ALERT_DURATION - 500
                    ? Math.max(0f, (ALERT_DURATION - elapsed) / 500f) : 1f;
            int alpha = (int)(Math.min(fadeIn, fadeOut) * 230);
            renderAlert(ctx, alert, sw, y, alpha);
            y += 62;
        }
    }

    private static void renderAlert(DrawContext ctx, ViolationAlert a, int sw, int y, int alpha) {
        if (alpha <= 10) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = 270, h = 54;
        int x = sw - w - 8;

        // Цвета
        int bg     = (alpha << 24) | 0x0A1520;
        int bgTop  = (alpha << 24) | 0x14202E;
        int red    = (alpha << 24) | 0xC62828;
        int redBr  = (alpha << 24) | 0xFF5252;
        int green  = (alpha << 24) | 0x2E7D32;
        int white  = (alpha << 24) | 0xEEEEEE;
        int dim    = (alpha << 24) | 0x90A4AE;
        int yellow = (alpha << 24) | 0xFFD740;

        // Фон
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + 22, bgTop);

        // Левая красная полоска
        ctx.fill(x, y, x + 4, y + h, red);
        ctx.fill(x, y, x + 4, y + 8, redBr);

        // Рамка
        ctx.fill(x,         y,         x + w, y + 1,     red);
        ctx.fill(x,         y + h - 1, x + w, y + h,     red);
        ctx.fill(x + w - 1, y,         x + w, y + h,     red);

        // Заголовок
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§c⚠ §c§lМОДЕРАЦИЯ"),
                x + 8, y + 4, white);

        // Категория справа
        String catShort = a.cat.name.length() > 20 ? a.cat.name.substring(0, 20) : a.cat.name;
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§8" + catShort),
                x + w - mc.textRenderer.getWidth(catShort) - 6, y + 4, dim);

        // Ник
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7Игрок: §f§l" + a.nick),
                x + 8, y + 16, white);

        // Слово
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7Слово: §c\"" + a.word + "\""),
                x + 8, y + 27, white);

        // Время мута
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7Мут: §e" + a.cat.time
                        + " §8| §7" + a.cat.rule),
                x + 8, y + 38, dim);

        // Подсказка
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§8[нажмите на кнопку в чате]"),
                x + 8, y + 46, (alpha << 24) | 0x546E7A);
    }

    public static class ViolationAlert {
        public final String nick, word;
        public final ChatGuardConfig.TriggerCategory cat;
        public final long startTime;

        public ViolationAlert(String nick, String word,
                               ChatGuardConfig.TriggerCategory cat, long startTime) {
            this.nick = nick; this.word = word;
            this.cat = cat; this.startTime = startTime;
        }
    }
}
