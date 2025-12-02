package com.custommenu.config;

import com.custommenu.CustomMenuMod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class MenuConfig {
    public static Map<String, MenuData> menus = new HashMap<>();
    
    private static int menuKey = 77;
    private static int tooltipOffsetX = 0;
    private static int tooltipOffsetY = 0;
    private static int maxMenus = 10;

    public static int getMenuKey() {
        return menuKey;
    }
    
    public static int getTooltipOffsetX() {
        return tooltipOffsetX;
    }
    
    public static int getTooltipOffsetY() {
        return tooltipOffsetY;
    }
    
    public static int getMaxMenus() {
        return maxMenus;
    }

    public static void loadMenus() {
        menus.clear();

        try {
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configPath = configDir.resolve("custommenu-menus.txt");

            if (!Files.exists(configPath)) {
                CustomMenuMod.LOGGER.info("Menu config file not found, creating default menus...");
                createDefaultMenu();
                saveMenus();
                return;
            }

            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            String currentMenu = null;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("MENU:")) {
                    String menuData = line.substring(5);
                    String[] parts = menuData.split(";");
                    if (parts.length >= 3) {
                        String menuName = parts[0].trim();
                        int slots = Integer.parseInt(parts[1].trim());
                        String title = parts[2].trim();
                        int keyCode = parts.length >= 4 ? Integer.parseInt(parts[3].trim()) : -1;

                        MenuData menu = new MenuData(menuName, slots, title, new ArrayList<>());
                        menu.keyCode = keyCode;
                        menus.put(menuName, menu);
                        currentMenu = menuName;

                        CustomMenuMod.LOGGER.debug("Loaded menu: {} with {} slots", menuName, slots);
                    }
                } else if (line.startsWith("ITEM:") && currentMenu != null) {
                    String itemData = line.substring(5);
                    String[] parts = itemData.split(";", 4);
                    if (parts.length >= 4) {
                        int slot = Integer.parseInt(parts[0].trim());
                        String itemName = parts[1].trim();
                        String displayName = parts[2].trim();
                        String command = parts[3].trim();

                        MenuData menu = menus.get(currentMenu);
                        if (menu != null) {
                            menu.items.add(new MenuItem(slot, itemName, displayName, command));
                        }
                    }
                }
            }

            if (menus.isEmpty()) {
                CustomMenuMod.LOGGER.warn("No menus found in config, creating default menu");
                createDefaultMenu();
                saveMenus();
            }

            CustomMenuMod.LOGGER.info("Loaded {} menus from config", menus.size());

        } catch (Exception e) {
            CustomMenuMod.LOGGER.error("Failed to load menus from config", e);
            e.printStackTrace();
            createDefaultMenu();
            saveMenus();
        }
    }

    private static void createDefaultMenu() {
        MenuData defaultMenu = new MenuData("default", 27, "Custom Menu", new ArrayList<>());
        defaultMenu.items.add(new MenuItem(0, "diamond", "§bElmas", "give @p minecraft:diamond 1"));
        defaultMenu.items.add(new MenuItem(1, "emerald", "§aZümrüt", "give @p minecraft:emerald 1"));
        defaultMenu.keyCode = 77;
        menus.put("default", defaultMenu);
        CustomMenuMod.LOGGER.info("Created default menu");
    }

    public static boolean createMenu(String menuName, int slots, String title) {
        if (menus.size() >= maxMenus) {
            return false;
        }
        if (menus.containsKey(menuName)) {
            return false;
        }
        MenuData newMenu = new MenuData(menuName, slots, title, new ArrayList<>());
        menus.put(menuName, newMenu);
        saveMenus();
        return true;
    }

    public static boolean deleteMenu(String name) {
        if (menus.remove(name) != null) {
            saveMenus();
            return true;
        }
        return false;
    }

    public static boolean updateMenuSlots(String menuName, int newSlots) {
        MenuData menu = menus.get(menuName);
        if (menu == null) {
            return false;
        }

        menu.items.removeIf(item -> item.slot >= newSlots);
        menu.slots = newSlots;

        saveMenus();
        return true;
    }

    public static MenuData getMenu(String name) {
        return menus.get(name);
    }

    public static boolean setMenuKey(String menuName, int keyCode) {
        MenuData menu = menus.get(menuName);
        if (menu == null) {
            return false;
        }

        for (MenuData m : menus.values()) {
            if (m.keyCode == keyCode && !m.name.equals(menuName)) {
                m.keyCode = -1;
            }
        }

        menu.keyCode = keyCode;
        saveMenus();
        return true;
    }

    public static MenuData getMenuByKey(int keyCode) {
        for (MenuData menu : menus.values()) {
            if (menu.keyCode == keyCode) {
                return menu;
            }
        }
        return null;
    }

    public static void addMenuItem(String menuName, int slot, String item, String name, String command) {
        MenuData menu = menus.get(menuName);
        if (menu != null) {
            menu.items.removeIf(i -> i.slot == slot);
            menu.items.add(new MenuItem(slot, item, name, command));
            saveMenus();
        }
    }

    public static void removeMenuItem(String menuName, int slot) {
        MenuData menu = menus.get(menuName);
        if (menu != null) {
            menu.items.removeIf(i -> i.slot == slot);
            saveMenus();
        }
    }

    public static void saveMenus() {
        try {
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configPath = configDir.resolve("custommenu-menus.txt");

            StringBuilder content = new StringBuilder();
            content.append("# CustomMenu Configuration File\n");
            content.append("# DO NOT EDIT MANUALLY - Use in-game commands\n");
            content.append("# Format:\n");
            content.append("# MENU:name;slots;title;keyCode\n");
            content.append("# ITEM:slot;itemName;displayName;command\n");
            content.append("\n");

            for (MenuData menuData : menus.values()) {
                content.append("MENU:")
                    .append(menuData.name).append(";")
                    .append(menuData.slots).append(";")
                    .append(menuData.title).append(";")
                    .append(menuData.keyCode)
                    .append("\n");

                for (MenuItem item : menuData.items) {
                    content.append("ITEM:")
                        .append(item.slot).append(";")
                        .append(item.itemName).append(";")
                        .append(item.displayName).append(";")
                        .append(item.command)
                        .append("\n");
                }

                content.append("\n");
                CustomMenuMod.LOGGER.debug("Saved menu '{}' with {} items", menuData.name, menuData.items.size());
            }

            if (Files.exists(configPath)) {
                Files.delete(configPath);
            }
            Files.write(configPath, content.toString().getBytes(StandardCharsets.UTF_8), 
                       StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            CustomMenuMod.LOGGER.info("Saved {} menus to config file", menus.size());

        } catch (Exception e) {
            CustomMenuMod.LOGGER.error("Failed to save menus to config", e);
            e.printStackTrace();
        }
    }

    public static class MenuData {
        public String name;
        public int slots;
        public String title;
        public List<MenuItem> items;
        public int keyCode = -1;

        public MenuData(String name, int slots, String title, List<MenuItem> items) {
            this.name = name;
            this.slots = slots;
            this.title = title;
            this.items = items;
        }
    }

    public static class MenuItem {
        public int slot;
        public String itemName;
        public String displayName;
        public String command;

        public MenuItem(int slot, String itemName, String displayName, String command) {
            this.slot = slot;
            this.itemName = itemName;
            this.displayName = displayName;
            this.command = command;
        }
    }
}
