package com.example.gradientchat.mixin;

import com.example.gradientchat.GradientChatMod;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    
    @ModifyArg(
            method = "sendMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"
            ),
            index = 0
    )
    private String modifyChatMessage(String message) {
        // Don't modify commands
        if (message.startsWith("/")) {
            return message;
        }
        
        // Apply gradient to the message
        return GradientChatMod.getGradientManager().applyGradient(message);
    }
}