
package com.custommenu.gui;

import com.custommenu.config.MenuConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EditMenuScreen extends Screen {
    private final Screen parent;
    private final String menuName;
    private MenuConfig.MenuData menuData;
    private EditBox slotsBox;
    private Button addItemButton;
    private Button saveSlotsButton;
    private Button backButton;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 24;
    private static final int ENTRIES_PER_PAGE = 8;
    private String errorMessage = "";

    public EditMenuScreen(Screen parent, String menuName) {
        super(Component.literal("Edit Menu: " + menuName));
        this.parent = parent;
        this.menuName = menuName;
        this.menuData = MenuConfig.getMenu(menuName);
    }

    @Override
    protected void init() {
        super.init();
        
        // Slots input box
        slotsBox = new EditBox(this.font, this.width / 2 - 150, 25, 60, 20, Component.literal("Slots"));
        slotsBox.setMaxLength(2);
        if (menuData != null) {
            slotsBox.setValue(String.valueOf(menuData.slots));
        }
        addWidget(slotsBox);
        
        // Save slots button
        saveSlotsButton = Button.builder(Component.literal("Save Slots"), button -> {
            updateSlots();
        }).bounds(this.width / 2 - 80, 25, 80, 20).build();
        addRenderableWidget(saveSlotsButton);
        
        // Add item button
        addItemButton = Button.builder(Component.literal("+ Add Item"), button -> {
            minecraft.setScreen(new AddItemScreen(this, menuName));
        }).bounds(this.width / 2 - 100, this.height - 50, 95, 20).build();
        addRenderableWidget(addItemButton);
        
        // Back button
        backButton = Button.builder(Component.literal("Back"), button -> {
            minecraft.setScreen(parent);
        }).bounds(this.width / 2 + 5, this.height - 50, 95, 20).build();
        addRenderableWidget(backButton);
        
        refreshMenuData();
    }

    private void updateSlots() {
        errorMessage = "";
        
        try {
            int newSlots = Integer.parseInt(slotsBox.getValue().trim());
            
            if (newSlots < 9 || newSlots > 54) {
                errorMessage = "Slots must be between 9 and 54!";
                return;
            }
            
            newSlots = (newSlots / 9) * 9; // Round to nearest 9
            
            if (menuData != null && MenuConfig.updateMenuSlots(menuName, newSlots)) {
                slotsBox.setValue(String.valueOf(newSlots));
                refreshMenuData();
                errorMessage = "§aSlots updated successfully!";
            } else {
                errorMessage = "Failed to update slots!";
            }
        } catch (NumberFormatException e) {
            errorMessage = "Invalid number!";
        }
    }

    public void refreshMenuData() {
        this.menuData = MenuConfig.getMenu(menuName);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        
        if (menuData == null) {
            graphics.drawCenteredString(this.font, "Menu not found!", this.width / 2, this.height / 2, 0xFF5555);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        
        // Slots label
        graphics.drawString(this.font, "Slots:", this.width / 2 - 200, 30, 0xAAAAAA);
        slotsBox.render(graphics, mouseX, mouseY, partialTick);
        
        // Menu info
        String info = "Title: " + menuData.title + " | Items: " + menuData.items.size();
        graphics.drawCenteredString(this.font, info, this.width / 2, 55, 0xAAAAAA);
        
        // Error/Success message
        if (!errorMessage.isEmpty()) {
            int color = errorMessage.startsWith("§a") ? 0x55FF55 : 0xFF5555;
            graphics.drawCenteredString(this.font, errorMessage.replace("§a", ""), this.width / 2, 70, color);
        }
        
        // Items list
        int startY = 85;
        List<MenuConfig.MenuItem> items = menuData.items;
        int maxEntries = Math.min(ENTRIES_PER_PAGE, items.size() - scrollOffset);
        
        for (int i = 0; i < maxEntries; i++) {
            int index = i + scrollOffset;
            if (index >= items.size()) break;
            
            MenuConfig.MenuItem item = items.get(index);
            int y = startY + i * ENTRY_HEIGHT;
            
            // Background
            int bgColor = mouseX >= 20 && mouseX <= this.width - 20 && 
                         mouseY >= y && mouseY <= y + ENTRY_HEIGHT - 2 ? 0x80FFFFFF : 0x80000000;
            graphics.fill(20, y, this.width - 20, y + ENTRY_HEIGHT - 2, bgColor);
            
            // Item info
            String itemInfo = "Slot " + item.slot + ": " + item.itemName + " - " + item.displayName;
            graphics.drawString(this.font, itemInfo, 25, y + 3, 0xFFFFFF);
            
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
        
        // Scroll indicator
        if (items.size() > ENTRIES_PER_PAGE) {
            String scrollInfo = (scrollOffset + 1) + "-" + Math.min(scrollOffset + ENTRIES_PER_PAGE, items.size()) + 
                              " of " + items.size();
            graphics.drawString(this.font, scrollInfo, 25, startY + ENTRIES_PER_PAGE * ENTRY_HEIGHT + 5, 0xAAAAAA);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (slotsBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (button == 0 && menuData != null) {
            int startY = 85;
            List<MenuConfig.MenuItem> items = menuData.items;
            
            for (int i = 0; i < Math.min(ENTRIES_PER_PAGE, items.size() - scrollOffset); i++) {
                int index = i + scrollOffset;
                if (index >= items.size()) break;
                
                MenuConfig.MenuItem item = items.get(index);
                int y = startY + i * ENTRY_HEIGHT;
                int buttonY = y + 2;
                
                // Edit button
                if (mouseX >= this.width - 180 && mouseX <= this.width - 120 && 
                    mouseY >= buttonY && mouseY <= buttonY + 18) {
                    minecraft.setScreen(new EditItemScreen(this, menuName, item));
                    return true;
                }
                
                // Delete button
                if (mouseX >= this.width - 110 && mouseX <= this.width - 50 && 
                    mouseY >= buttonY && mouseY <= buttonY + 18) {
                    MenuConfig.removeMenuItem(menuName, item.slot);
                    refreshMenuData();
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (menuData != null && menuData.items.size() > ENTRIES_PER_PAGE) {
            scrollOffset = Math.max(0, Math.min(menuData.items.size() - ENTRIES_PER_PAGE, 
                                               scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (slotsBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (slotsBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
