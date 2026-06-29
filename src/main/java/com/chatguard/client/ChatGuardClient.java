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

    // Открытие настроек: клавиша M (на русской раскладке — ь)
    // Без модификаторов, срабатывает только вне чата/меню
    private static final int OPEN_KEY = GLFW.GLFW_KEY_M;

    private boolean keyWasDown = false;

    @Override
    public void onInitializeClient() {
        ChatGuardConfig.load();
        ChatEventHandler.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (client.currentScreen != null) return; // не срабатывает в чате/меню

            long win = client.getWindow().getHandle();
            boolean keyDown = InputUtil.isKeyPressed(win, OPEN_KEY);

            if (keyDown && !keyWasDown) {
                client.setScreen(new ChatGuardSettingsScreen(null));
            }
            keyWasDown = keyDown;
        });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen != null || mc.player == null) return;
            ViolationOverlay.render(ctx,
                    mc.getWindow().getScaledWidth(),
                    mc.getWindow().getScaledHeight(),
                    tickDelta);
        });

        System.out.println("[ChatGuard] Загружен! M (ь) = настройки ChatGuard.");
    }
}
