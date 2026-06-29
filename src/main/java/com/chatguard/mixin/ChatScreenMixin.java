package com.chatguard.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    /**
     * Перехватываем нажатие клавиш в чате.
     * Можно расширить для дополнительных hotkey внутри чата.
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = false)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers,
                              CallbackInfoReturnable<Boolean> cir) {
        // Зарезервировано для будущего использования
        // Например, Tab-автодополнение ников нарушителей
    }
}
