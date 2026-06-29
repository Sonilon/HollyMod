package com.chatguard.client;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.event.ChatEventHandler;
import com.chatguard.gui.ChatGuardSettingsScreen;
import com.chatguard.gui.ViolationOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ChatGuardClient implements ClientModInitializer {

    private boolean rightShiftWasDown = false;

    @Override
    public void onInitializeClient() {
        // Загружаем конфиг
        ChatGuardConfig.load();

        // Регистрируем обработчик чата
        ChatEventHandler.register();

        // Правый Shift → открыть настройки
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            long win = client.getWindow().getHandle();
            boolean rightShiftDown = InputUtil.isKeyPressed(win, GLFW.GLFW_KEY_RIGHT_SHIFT);

            if (rightShiftDown && !rightShiftWasDown) {
                // Не открываем, если уже открыт экран настроек
                if (client.currentScreen == null) {
                    client.setScreen(new ChatGuardSettingsScreen(null));
                }
            }
            rightShiftWasDown = rightShiftDown;
        });

        // HUD — рендер оверлея с нарушителями
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen != null) return; // не показывать поверх экранов
            if (mc.player == null) return;
            ViolationOverlay.render(ctx, mc.getWindow().getScaledWidth(),
                    mc.getWindow().getScaledHeight(), tickDelta);
        });

        System.out.println("[ChatGuard] Мод загружен! Правый Shift = настройки.");
    }
}
