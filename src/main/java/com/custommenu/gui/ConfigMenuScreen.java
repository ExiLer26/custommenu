
package com.custommenu.gui;

import com.custommenu.config.MenuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ConfigMenuScreen extends Screen {
    private final Screen parent;
    private EditBox searchBox;
    private Button createMenuButton;
    private Button backButton;
    private List<MenuEntry> menuEntries = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 24;
    private static final int ENTRIES_PER_PAGE = 10;

    public ConfigMenuScreen(Screen parent) {
        super(Component.literal("Menu Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        
        // Search box
        searchBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 20, Component.literal("Search"));
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.literal("Search menus..."));
        addWidget(searchBox);

        // Create menu button
        createMenuButton = Button.builder(Component.literal("+ New Menu"), button -> {
            minecraft.setScreen(new CreateMenuScreen(this));
        }).bounds(this.width / 2 - 100, this.height - 50, 95, 20).build();
        addRenderableWidget(createMenuButton);

        // Back button
        backButton = Button.builder(Component.literal("Back"), button -> {
            minecraft.setScreen(parent);
        }).bounds(this.width / 2 + 5, this.height - 50, 95, 20).build();
        addRenderableWidget(backButton);

        refreshMenuList();
    }

    public void refreshMenuList() {
        menuEntries.clear();
        String search = searchBox != null ? searchBox.getValue().toLowerCase() : "";
        
        for (String menuName : MenuConfig.menus.keySet()) {
            if (search.isEmpty() || menuName.toLowerCase().contains(search)) {
                menuEntries.add(new MenuEntry(menuName));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);
        
        // Search box
        searchBox.render(graphics, mouseX, mouseY, partialTick);
        
        // Menu list
        int startY = 50;
        int maxEntries = Math.min(ENTRIES_PER_PAGE, menuEntries.size() - scrollOffset);
        
        for (int i = 0; i < maxEntries; i++) {
            int index = i + scrollOffset;
            if (index >= menuEntries.size()) break;
            
            MenuEntry entry = menuEntries.get(index);
            int y = startY + i * ENTRY_HEIGHT;
            
            // Background
            int bgColor = mouseX >= 20 && mouseX <= this.width - 20 && 
                         mouseY >= y && mouseY <= y + ENTRY_HEIGHT - 2 ? 0x80FFFFFF : 0x80000000;
            graphics.fill(20, y, this.width - 20, y + ENTRY_HEIGHT - 2, bgColor);
            
            // Menu info
            MenuConfig.MenuData menu = MenuConfig.getMenu(entry.menuName);
            if (menu != null) {
                String info = entry.menuName + " - " + menu.title + " (" + menu.items.size() + " items)";
                graphics.drawString(this.font, info, 25, y + 3, 0xFFFFFF);
                
                // Buttons
                int buttonY = y + 2;
                
                // Edit button
                if (mouseX >= this.width - 180 && mouseX <= this.width - 120 && 
                    mouseY >= buttonY && mouseY <= buttonY + 18) {
                    graphics.fill(this.width - 180, buttonY, this.width - 120, buttonY + 18, 0xFF4CAF50);
                } else {
                    graphics.fill(this.width - 180, buttonY, this.width - 120, buttonY + 18, 0xFF2E7D32);
                }
                graphics.drawCenteredString(this.font, "Edit", this.width - 150, buttonY + 5, 0xFFFFFF);
                
                // Delete button
                if (mouseX >= this.width - 110 && mouseX <= this.width - 50 && 
                    mouseY >= buttonY && mouseY <= buttonY + 18) {
                    graphics.fill(this.width - 110, buttonY, this.width - 50, buttonY + 18, 0xFFF44336);
                } else {
                    graphics.fill(this.width - 110, buttonY, this.width - 50, buttonY + 18, 0xFFD32F2F);
                }
                graphics.drawCenteredString(this.font, "Delete", this.width - 80, buttonY + 5, 0xFFFFFF);
            }
        }
        
        // Scroll indicator
        if (menuEntries.size() > ENTRIES_PER_PAGE) {
            String scrollInfo = (scrollOffset + 1) + "-" + Math.min(scrollOffset + ENTRIES_PER_PAGE, menuEntries.size()) + 
                              " of " + menuEntries.size();
            graphics.drawString(this.font, scrollInfo, 25, startY + ENTRIES_PER_PAGE * ENTRY_HEIGHT + 5, 0xAAAAAA);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (button == 0) {
            int startY = 50;
            for (int i = 0; i < Math.min(ENTRIES_PER_PAGE, menuEntries.size() - scrollOffset); i++) {
                int index = i + scrollOffset;
                if (index >= menuEntries.size()) break;
                
                MenuEntry entry = menuEntries.get(index);
                int y = startY + i * ENTRY_HEIGHT;
                int buttonY = y + 2;
                
                // Edit button
                if (mouseX >= this.width - 180 && mouseX <= this.width - 120 && 
                    mouseY >= buttonY && mouseY <= buttonY + 18) {
                    minecraft.setScreen(new EditMenuScreen(this, entry.menuName));
                    return true;
                }
                
                // Delete button
                if (mouseX >= this.width - 110 && mouseX <= this.width - 50 && 
                    mouseY >= buttonY && mouseY <= buttonY + 18) {
                    MenuConfig.deleteMenu(entry.menuName);
                    refreshMenuList();
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (menuEntries.size() > ENTRIES_PER_PAGE) {
            scrollOffset = Math.max(0, Math.min(menuEntries.size() - ENTRIES_PER_PAGE, 
                                               scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            refreshMenuList();
            scrollOffset = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox.charTyped(codePoint, modifiers)) {
            refreshMenuList();
            scrollOffset = 0;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static class MenuEntry {
        public final String menuName;

        public MenuEntry(String menuName) {
            this.menuName = menuName;
        }
    }
}
