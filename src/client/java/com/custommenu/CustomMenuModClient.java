package com.custommenu;

import com.custommenu.config.MenuConfig;
import com.custommenu.gui.ConfigMenuScreen;
import com.custommenu.gui.CustomMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class CustomMenuModClient implements ClientModInitializer {
    public static KeyMapping menuKey;
    public static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        CustomMenuMod.LOGGER.info("Custom Menu Mod client initializing...");
        
        ClientNetworkHandler.register();
        
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.custommenu.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.custommenu"
        ));
        
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.custommenu.config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.custommenu"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                if (client.screen == null && client.player != null) {
                    client.setScreen(new ConfigMenuScreen(null));
                }
            }
            
            while (menuKey.consumeClick()) {
                if (client.screen == null && client.player != null) {
                    if (MenuConfig.getMenu("default") != null) {
                        client.setScreen(new CustomMenuScreen("default"));
                    }
                }
            }
            
            if (client.screen == null && client.player != null) {
                for (MenuConfig.MenuData menu : MenuConfig.menus.values()) {
                    if (menu.keyCode != -1 && isKeyPressed(menu.keyCode)) {
                        client.setScreen(new CustomMenuScreen(menu.name));
                        break;
                    }
                }
            }
        });
        
        CustomMenuMod.LOGGER.info("Custom Menu Mod client initialized!");
    }
    
    private boolean isKeyPressed(int keyCode) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }
}
