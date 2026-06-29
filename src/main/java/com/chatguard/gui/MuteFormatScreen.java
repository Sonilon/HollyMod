package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class MuteFormatScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget muteField;

    public MuteFormatScreen(Screen parent) {
        super(Text.literal("§b✎ Формат команды мута"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width/2;
        muteField = new TextFieldWidget(textRenderer, cx-150, 80, 300, 20, Text.literal("Команда"));
        muteField.setText(ChatGuardConfig.getInstance().muteCommand);
        muteField.setMaxLength(256);
        addDrawableChild(muteField);

        addDrawableChild(ButtonWidget.builder(Text.literal("§a✔ Сохранить"), btn -> {
            ChatGuardConfig.getInstance().muteCommand = muteField.getText().trim();
            ChatGuardConfig.save();
            client.setScreen(parent);
        }).dimensions(cx-55, 110, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§7← Назад"),
                btn -> client.setScreen(parent)
        ).dimensions(cx-55, 135, 110, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFF00E676);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width/2, 14, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Плейсхолдеры: §e{nick} {time} {rule} {reason}"),
                width/2, 65, 0xFF90A4AE);
        ctx.fill(0, height-2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    @Override public boolean shouldPause() { return false; }
}
