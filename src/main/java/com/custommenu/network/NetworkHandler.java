package com.custommenu.network;

import com.custommenu.CustomMenuMod;
import com.custommenu.config.MenuConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {
    public static final ResourceLocation OPEN_MENU_PACKET = new ResourceLocation(CustomMenuMod.MOD_ID, "open_menu");

    public static void register() {
        CustomMenuMod.LOGGER.info("Network packets registered");
    }
    
    public static void sendOpenMenuPacket(ServerPlayer player, String menuName, MenuConfig.MenuData menuData) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(menuName);
        buf.writeInt(menuData.slots);
        buf.writeUtf(menuData.title);
        buf.writeInt(menuData.items.size());
        
        for (MenuConfig.MenuItem item : menuData.items) {
            buf.writeInt(item.slot);
            buf.writeUtf(item.itemName);
            buf.writeUtf(item.displayName);
            buf.writeUtf(item.command);
        }
        
        ServerPlayNetworking.send(player, OPEN_MENU_PACKET, buf);
    }
}
