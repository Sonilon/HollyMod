package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriggerEditScreen extends Screen {

    private final Screen parent;
    private final ChatGuardConfig.TriggerCategory cat;
    private final int index;

    private TextFieldWidget nameField;
    private TextFieldWidget timeField;
    private TextFieldWidget ruleField;
    private TextFieldWidget reasonField;
    // Слова разбиты на несколько строк по 256 символов каждая
    private final List<TextFieldWidget> wordFields = new ArrayList<>();
    private static final int WORDS_PER_FIELD = 256;

    public TriggerEditScreen(Screen parent, ChatGuardConfig.TriggerCategory cat, int index) {
        super(Text.literal(cat == null ? "§a+ Новая категория" : "§e✎ Редактировать категорию"));
        this.parent = parent;
        this.cat    = cat != null ? cat : new ChatGuardConfig.TriggerCategory();
        this.index  = index;
    }

    @Override
    protected void init() {
        int cx = width / 2, fw = 260, fh = 20;
        int top = 55, gap = 32;

        nameField = field(cx, fw, fh, top, "Название категории");
        nameField.setText(cat.name);
        addDrawableChild(nameField);

        timeField = field(cx, fw, fh, top + gap, "Время мута (60m, 3h, 1d...)");
        timeField.setText(cat.time);
        addDrawableChild(timeField);

        ruleField = field(cx, fw, fh, top + gap * 2, "Пункт правил");
        ruleField.setText(cat.rule);
        addDrawableChild(ruleField);

        reasonField = field(cx, fw, fh, top + gap * 3, "Причина мута");
        reasonField.setText(cat.reason);
        reasonField.setMaxLength(128);
        addDrawableChild(reasonField);

        // Слова — разбиваем на чанки по ~256 символов,
        // показываем минимум 2 поля (можно добавить ещё)
        wordFields.clear();
        List<String> chunks = splitWordsIntoChunks(cat.words, WORDS_PER_FIELD);
        // Минимум 2 поля
        while (chunks.size() < 2) chunks.add("");

        int wordTop = top + gap * 4;
        for (int i = 0; i < chunks.size(); i++) {
            TextFieldWidget wf = new TextFieldWidget(textRenderer,
                    cx - fw / 2, wordTop + i * (fh + 4), fw, fh,
                    Text.literal("Слова " + (i + 1)));
            wf.setMaxLength(WORDS_PER_FIELD);
            wf.setText(chunks.get(i));
            wordFields.add(wf);
            addDrawableChild(wf);
        }

        int btnY = wordTop + wordFields.size() * (fh + 4) + 8;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"), btn -> save()
        ).dimensions(cx - 105, btnY, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c✖ Отмена"), btn -> client.setScreen(parent)
        ).dimensions(cx + 5, btnY, 100, 20).build());
    }

    private TextFieldWidget field(int cx, int fw, int fh, int y, String hint) {
        TextFieldWidget tf = new TextFieldWidget(textRenderer,
                cx - fw / 2, y, fw, fh, Text.literal(hint));
        tf.setMaxLength(256);
        return tf;
    }

    /** Объединяем все слова в одну строку, потом режем на чанки ≤ maxLen */
    private List<String> splitWordsIntoChunks(List<String> words, int maxLen) {
        List<String> chunks = new ArrayList<>();
        if (words == null || words.isEmpty()) return chunks;

        String joined = String.join(", ", words);
        // Режем по maxLen, стараясь не резать посередине слова
        while (joined.length() > maxLen) {
            int cut = joined.lastIndexOf(',', maxLen);
            if (cut < 0) cut = maxLen;
            chunks.add(joined.substring(0, cut).trim());
            joined = joined.substring(cut).replaceFirst("^,\\s*", "");
        }
        if (!joined.isEmpty()) chunks.add(joined.trim());
        return chunks;
    }

    private void save() {
        cat.name   = nameField.getText().trim();
        cat.time   = timeField.getText().trim();
        cat.rule   = ruleField.getText().trim();
        cat.reason = reasonField.getText().trim();

        if (cat.name.isEmpty()) return;

        // Собираем все слова из всех полей
        cat.words.clear();
        for (TextFieldWidget wf : wordFields) {
            String text = wf.getText().trim();
            if (text.isEmpty()) continue;
            for (String w : text.split(",")) {
                String trimmed = w.trim();
                if (!trimmed.isEmpty()) cat.words.add(trimmed);
            }
        }

        if (index == -1) {
            ChatGuardConfig.getInstance().categories.add(cat);
        }
        ChatGuardConfig.save();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 44, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 44, 0xFF00E676);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 13, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7ПКМ по категории в списке = удалить"),
                width / 2, 26, 0xFF90A4AE);

        int cx = width / 2, top = 55, gap = 32, fh = 20;

        label(ctx, "§eНазвание:",  cx, top - 10);
        label(ctx, "§bВремя мута:", cx, top + gap - 10);
        label(ctx, "§aПравило:",   cx, top + gap * 2 - 10);
        label(ctx, "§fПричина:",   cx, top + gap * 3 - 10);

        int wordTop = top + gap * 4;
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§7Слова-триггеры §8(через запятую, несколько строк):"),
                cx - 130, wordTop - 10, 0xFFCCCCCC);

        ctx.fill(0, height - 2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    private void label(DrawContext ctx, String text, int cx, int y) {
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), cx - 130, y, 0xFFCCCCCC);
    }

    @Override
    public boolean shouldPause() { return false; }
}
