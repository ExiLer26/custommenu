package com.custommenu.network;

import com.custommenu.CustomMenuMod;
import com.custommenu.config.MenuConfig;
import com.custommenu.gui.CustomMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketOpenMenu {
    private final String menuName;
    private final int slots;
    private final String title;
    private final List<MenuConfig.MenuItem> items;

    public PacketOpenMenu(String menuName, MenuConfig.MenuData menuData) {
        this.menuName = menuName;
        this.slots = menuData.slots;
        this.title = menuData.title;
        this.items = new ArrayList<>(menuData.items);
    }

    private PacketOpenMenu(String menuName, int slots, String title, List<MenuConfig.MenuItem> items) {
        this.menuName = menuName;
        this.slots = slots;
        this.title = title;
        this.items = items;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(menuName);
        buf.writeInt(slots);
        buf.writeUtf(title);
        buf.writeInt(items.size());
        for (MenuConfig.MenuItem item : items) {
            buf.writeInt(item.slot);
            buf.writeUtf(item.itemName);
            buf.writeUtf(item.displayName);
            buf.writeUtf(item.command);
        }
    }

    public static PacketOpenMenu decode(FriendlyByteBuf buf) {
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
        return new PacketOpenMenu(menuName, slots, title, items);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                CustomMenuMod.LOGGER.info("Opening menu: {}", menuName);
                MenuConfig.MenuData clientMenuData = new MenuConfig.MenuData(menuName, slots, title, items);
                MenuConfig.menus.put(menuName, clientMenuData);
                mc.setScreen(new CustomMenuScreen(menuName));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
