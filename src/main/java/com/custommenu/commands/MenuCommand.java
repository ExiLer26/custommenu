package com.custommenu.commands;

import com.custommenu.config.MenuConfig;
import com.custommenu.network.NetworkHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class MenuCommand {
    private static final Map<String, Integer> KEY_NAME_TO_CODE = new HashMap<>();
    private static final Map<Integer, String> KEY_CODE_TO_NAME = new HashMap<>();
    
    static {
        addKey("A", 65);
        addKey("B", 66);
        addKey("C", 67);
        addKey("D", 68);
        addKey("E", 69);
        addKey("F", 70);
        addKey("G", 71);
        addKey("H", 72);
        addKey("I", 73);
        addKey("J", 74);
        addKey("K", 75);
        addKey("L", 76);
        addKey("M", 77);
        addKey("N", 78);
        addKey("O", 79);
        addKey("P", 80);
        addKey("Q", 81);
        addKey("R", 82);
        addKey("S", 83);
        addKey("T", 84);
        addKey("U", 85);
        addKey("V", 86);
        addKey("W", 87);
        addKey("X", 88);
        addKey("Y", 89);
        addKey("Z", 90);
        addKey("0", 48);
        addKey("1", 49);
        addKey("2", 50);
        addKey("3", 51);
        addKey("4", 52);
        addKey("5", 53);
        addKey("6", 54);
        addKey("7", 55);
        addKey("8", 56);
        addKey("9", 57);
        addKey("F1", 290);
        addKey("F2", 291);
        addKey("F3", 292);
        addKey("F4", 293);
        addKey("F5", 294);
        addKey("F6", 295);
        addKey("F7", 296);
        addKey("F8", 297);
        addKey("F9", 298);
        addKey("F10", 299);
        addKey("F11", 300);
        addKey("F12", 301);
    }
    
    private static void addKey(String name, int code) {
        KEY_NAME_TO_CODE.put(name, code);
        KEY_CODE_TO_NAME.put(code, name);
    }
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("menu")
            .requires(source -> source.hasPermission(0))
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .then(Commands.argument("slots", IntegerArgumentType.integer(9, 54))
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                int slots = IntegerArgumentType.getInteger(context, "slots");
                                String title = StringArgumentType.getString(context, "title");
                                return createMenu(context.getSource(), name, slots, title);
                            })
                        )
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            int slots = IntegerArgumentType.getInteger(context, "slots");
                            return createMenu(context.getSource(), name, slots, "Custom Menu");
                        })
                    )
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        return createMenu(context.getSource(), name, 27, "Custom Menu");
                    })
                )
            )
            .then(Commands.literal("open")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        return openMenu(context.getSource(), name);
                    })
                )
            )
            .then(Commands.literal("add")
                .then(Commands.argument("menu", StringArgumentType.word())
                    .then(Commands.argument("slot", IntegerArgumentType.integer(0, 53))
                        .then(Commands.argument("item", StringArgumentType.word())
                            .then(Commands.argument("displayName", StringArgumentType.word())
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String menu = StringArgumentType.getString(context, "menu");
                                        int slot = IntegerArgumentType.getInteger(context, "slot");
                                        String item = StringArgumentType.getString(context, "item");
                                        String displayName = StringArgumentType.getString(context, "displayName");
                                        String command = StringArgumentType.getString(context, "command");
                                        return addMenuItem(context.getSource(), menu, slot, item, displayName, command);
                                    })
                                )
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("menu", StringArgumentType.word())
                    .then(Commands.argument("slot", IntegerArgumentType.integer(0, 53))
                        .executes(context -> {
                            String menu = StringArgumentType.getString(context, "menu");
                            int slot = IntegerArgumentType.getInteger(context, "slot");
                            return removeMenuItem(context.getSource(), menu, slot);
                        })
                    )
                )
            )
            .then(Commands.literal("delete")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        return deleteMenu(context.getSource(), name);
                    })
                )
            )
            .then(Commands.literal("list")
                .executes(context -> listMenus(context.getSource()))
            )
            .then(Commands.literal("setkey")
                .then(Commands.argument("menu", StringArgumentType.word())
                    .then(Commands.argument("key", StringArgumentType.word())
                        .executes(context -> {
                            String menu = StringArgumentType.getString(context, "menu");
                            String key = StringArgumentType.getString(context, "key");
                            return setMenuKey(context.getSource(), menu, key);
                        })
                    )
                )
            )
            .then(Commands.literal("reload")
                .executes(context -> reloadMenus(context.getSource()))
            )
        );
    }

    private static int createMenu(CommandSourceStack source, String name, int slots, String title) {
        slots = (slots / 9) * 9;
        if (MenuConfig.createMenu(name, slots, title)) {
            source.sendSuccess(() -> Component.literal("§aMenu '§e" + name + "§a' oluşturuldu!"), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cMenu oluşturulamadı!"));
            return 0;
        }
    }

    private static int openMenu(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cBu komut sadece oyuncular tarafından kullanılabilir!"));
            return 0;
        }

        if (MenuConfig.getMenu(name) == null) {
            source.sendFailure(Component.literal("§cMenu bulunamadı: " + name));
            return 0;
        }

        MenuConfig.MenuData menuData = MenuConfig.getMenu(name);
        NetworkHandler.sendOpenMenuPacket(player, name, menuData);
        source.sendSuccess(() -> Component.literal("§aMenu '§e" + name + "§a' açılıyor..."), false);
        return 1;
    }

    private static int addMenuItem(CommandSourceStack source, String menu, int slot, String item, String displayName, String command) {
        if (MenuConfig.getMenu(menu) == null) {
            source.sendFailure(Component.literal("§cMenu bulunamadı: " + menu));
            return 0;
        }

        MenuConfig.addMenuItem(menu, slot, item, displayName, command);
        source.sendSuccess(() -> Component.literal("§aItem eklendi!"), false);
        return 1;
    }

    private static int removeMenuItem(CommandSourceStack source, String menu, int slot) {
        if (MenuConfig.getMenu(menu) == null) {
            source.sendFailure(Component.literal("§cMenu bulunamadı: " + menu));
            return 0;
        }

        MenuConfig.removeMenuItem(menu, slot);
        source.sendSuccess(() -> Component.literal("§aItem kaldırıldı!"), false);
        return 1;
    }

    private static int deleteMenu(CommandSourceStack source, String name) {
        if (MenuConfig.deleteMenu(name)) {
            source.sendSuccess(() -> Component.literal("§aMenu '§e" + name + "§a' silindi!"), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cMenu bulunamadı: " + name));
            return 0;
        }
    }

    private static int listMenus(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§aMevcut Menüler:"), false);
        for (String name : MenuConfig.menus.keySet()) {
            MenuConfig.MenuData menu = MenuConfig.getMenu(name);
            String keyName = KEY_CODE_TO_NAME.getOrDefault(menu.keyCode, null);
            String keyInfo = keyName != null ? ", tuş: " + keyName : (menu.keyCode != -1 ? ", tuş kodu: " + menu.keyCode : "");
            source.sendSuccess(() -> Component.literal("§7- §e" + name + " §7(" + menu.title + ", " + menu.slots + " slot" + keyInfo + ")"), false);
        }
        return 1;
    }

    private static int setMenuKey(CommandSourceStack source, String menuName, String keyName) {
        if (MenuConfig.getMenu(menuName) == null) {
            source.sendFailure(Component.literal("§cMenu bulunamadı: " + menuName));
            return 0;
        }

        Integer keyCode = KEY_NAME_TO_CODE.get(keyName.toUpperCase());
        if (keyCode == null) {
            source.sendFailure(Component.literal("§cGeçersiz tuş adı: " + keyName + ". Desteklenen tuşlar: A-Z, 0-9, F1-F12"));
            return 0;
        }

        if (MenuConfig.setMenuKey(menuName, keyCode)) {
            source.sendSuccess(() -> Component.literal("§aMenu '§e" + menuName + "§a' için tuş '§e" + keyName.toUpperCase() + "§a' atandı!"), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cTuş atanamadı!"));
            return 0;
        }
    }

    private static int reloadMenus(CommandSourceStack source) {
        MenuConfig.loadMenus();
        source.sendSuccess(() -> Component.literal("§aMenuler yeniden yüklendi! §7(Toplam: " + MenuConfig.menus.size() + " menu)"), false);
        return 1;
    }
}
