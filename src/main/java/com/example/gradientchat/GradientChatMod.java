package com.example.gradientchat;

import com.example.gradientchat.gui.GradientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradientChatMod implements ClientModInitializer {
    public static final String MOD_ID = "gradient-chat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static KeyBinding openGuiKey;
    private static GradientManager gradientManager;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Gradient Chat Mod");
        
        // Initialize the gradient manager
        gradientManager = new GradientManager();
        
        // Register keybinding (Right Shift)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gradient-chat.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.gradient-chat.general"
        ));
        
        // Register tick event to check for keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new GradientScreen(gradientManager));
            }
        });
    }
    
    public static GradientManager getGradientManager() {
        return gradientManager;
    }
}