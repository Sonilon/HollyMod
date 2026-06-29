package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.Arrays;

public class TriggerEditScreen extends Screen {

    private final Screen parent;
    private final ChatGuardConfig.TriggerCategory cat;
    private final int index;

    private TextFieldWidget nameField;
    private TextFieldWidget timeField;
    private TextFieldWidget ruleField;
    private TextFieldWidget reasonField;
    private TextFieldWidget wordsField; // слова через запятую

    public TriggerEditScreen(Screen parent, ChatGuardConfig.TriggerCategory cat, int index) {
        super(Text.literal(cat == null ? "§a+ Новая категория" : "§e✎ Редактировать категорию"));
        this.parent = parent;
        this.cat    = cat != null ? cat : new ChatGuardConfig.TriggerCategory();
        this.index  = index;
    }

    @Override
    protected void init() {
        int cx = width / 2, top = 58, fw = 260, fh = 20, gap = 34;

        nameField = new TextFieldWidget(textRenderer, cx - fw/2, top, fw, fh, Text.literal("Название"));
        nameField.setText(cat.name); nameField.setMaxLength(64);
        addDrawableChild(nameField);

        timeField = new TextFieldWidget(textRenderer, cx - fw/2, top + gap, fw, fh, Text.literal("Время"));
        timeField.setText(cat.time); timeField.setMaxLength(20);
        addDrawableChild(timeField);

        ruleField = new TextFieldWidget(textRenderer, cx - fw/2, top + gap*2, fw, fh, Text.literal("Правило"));
        ruleField.setText(cat.rule); ruleField.setMaxLength(20);
        addDrawableChild(ruleField);

        reasonField = new TextFieldWidget(textRenderer, cx - fw/2, top + gap*3, fw, fh, Text.literal("Причина"));
        reasonField.setText(cat.reason); reasonField.setMaxLength(128);
        addDrawableChild(reasonField);

        // Слова через запятую
        String wordsStr = String.join(", ", cat.words);
        wordsField = new TextFieldWidget(textRenderer, cx - fw/2, top + gap*4, fw, fh, Text.literal("Слова"));
        wordsField.setText(wordsStr); wordsField.setMaxLength(2048);
        addDrawableChild(wordsField);

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"), btn -> save()
        ).dimensions(cx - 105, top + gap*5 + 6, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c✖ Отмена"), btn -> client.setScreen(parent)
        ).dimensions(cx + 5, top + gap*5 + 6, 100, 20).build());
    }

    private void save() {
        cat.name   = nameField.getText().trim();
        cat.time   = timeField.getText().trim();
        cat.rule   = ruleField.getText().trim();
        cat.reason = reasonField.getText().trim();

        // Парсим слова через запятую
        cat.words.clear();
        for (String w : wordsField.getText().split(",")) {
            String trimmed = w.trim();
            if (!trimmed.isEmpty()) cat.words.add(trimmed);
        }

        if (cat.name.isEmpty()) return;
        if (index == -1) ChatGuardConfig.getInstance().categories.add(cat);
        ChatGuardConfig.save();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFF00E676);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width/2, 14, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7ПКМ по категории в списке = удалить"),
                width/2, 27, 0xFF90A4AE);

        int cx = width/2, top = 58, gap = 34;
        label(ctx, "§eНазвание категории:", cx, top - 11);
        label(ctx, "§bВремя мута (60m, 3h, 1d...):", cx, top + gap - 11);
        label(ctx, "§aПункт правил (напр. 3.10):", cx, top + gap*2 - 11);
        label(ctx, "§fПричина мута:", cx, top + gap*3 - 11);
        label(ctx, "§7Слова-триггеры §8(через запятую):", cx, top + gap*4 - 11);

        ctx.fill(0, height - 2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    private void label(DrawContext ctx, String text, int cx, int y) {
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), cx - 130, y, 0xFFCCCCCC);
    }

    @Override public boolean shouldPause() { return false; }
}
