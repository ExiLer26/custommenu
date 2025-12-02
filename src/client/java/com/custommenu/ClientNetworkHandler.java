package com.custommenu;

import com.custommenu.config.MenuConfig;
import com.custommenu.gui.CustomMenuScreen;
import com.custommenu.network.NetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ClientNetworkHandler {
    
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.OPEN_MENU_PACKET, (client, handler, buf, responseSender) -> {
            String menuName = buf.readUtf();
            int slots = buf.readInt();
            String title = buf.readUtf();
            int itemCount = buf.readInt();
            
            List<MenuConfig.MenuItem> items = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                int slot = buf.readInt();
                String itemName = buf.readUtf();
                String displayName = buf.readUtf();
                String command = buf.readUtf();
                items.add(new MenuConfig.MenuItem(slot, itemName, displayName, command));
            }
            
            client.execute(() -> {
                if (client.player != null) {
                    CustomMenuMod.LOGGER.info("Opening menu: {}", menuName);
                    MenuConfig.MenuData clientMenuData = new MenuConfig.MenuData(menuName, slots, title, items);
                    MenuConfig.menus.put(menuName, clientMenuData);
                    client.setScreen(new CustomMenuScreen(menuName));
                }
            });
        });
    }
}
