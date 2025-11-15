
package com.custommenu.handlers;

import com.custommenu.config.MenuConfig;
import com.custommenu.gui.ConfigMenuScreen;
import com.custommenu.gui.CustomMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    public static KeyMapping menuKey;
    public static KeyMapping configKey;

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        menuKey = new KeyMapping(
            "key.custommenu.open",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.custommenu"
        );
        event.register(menuKey);

        configKey = new KeyMapping(
            "key.custommenu.config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.custommenu"
        );
        event.register(configKey);

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    public static class ClientEventHandler {
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();

                // Check config menu key
                while (configKey.consumeClick()) {
                    if (mc.screen == null && mc.player != null) {
                        mc.setScreen(new ConfigMenuScreen(null));
                    }
                }

                // Check default menu key
                while (menuKey.consumeClick()) {
                    if (mc.screen == null && mc.player != null) {
                        if (MenuConfig.getMenu("default") != null) {
                            mc.setScreen(new CustomMenuScreen("default"));
                        }
                    }
                }

                // Check custom menu keys
                if (mc.screen == null && mc.player != null) {
                    for (MenuConfig.MenuData menu : MenuConfig.menus.values()) {
                        if (menu.keyCode != -1 && isKeyPressed(menu.keyCode)) {
                            mc.setScreen(new CustomMenuScreen(menu.name));
                            break;
                        }
                    }
                }
            }
        }

        private boolean isKeyPressed(int keyCode) {
            long window = Minecraft.getInstance().getWindow().getWindow();
            return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
        }
    }
}
