package com.custommenu.gui;

import com.custommenu.config.MenuConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditItemScreen extends Screen {
    private final Screen parent;
    private final String menuName;
    private final MenuConfig.MenuItem originalItem;
    private EditBox slotBox;
    private EditBox itemBox;
    private EditBox displayNameBox;
    private EditBox commandBox;
    private Button saveButton;
    private Button cancelButton;
    private String errorMessage = "";

    public EditItemScreen(Screen parent, String menuName, MenuConfig.MenuItem item) {
        super(Component.literal("Edit Item"));
        this.parent = parent;
        this.menuName = menuName;
        this.originalItem = item;
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 50;
        
        slotBox = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.literal("Slot"));
        slotBox.setMaxLength(2);
        slotBox.setValue(String.valueOf(originalItem.slot));
        addWidget(slotBox);
        
        itemBox = new EditBox(this.font, centerX - 100, startY + 40, 200, 20, Component.literal("Item"));
        itemBox.setMaxLength(64);
        itemBox.setValue(originalItem.itemName);
        addWidget(itemBox);
        
        displayNameBox = new EditBox(this.font, centerX - 100, startY + 80, 200, 20, Component.literal("Display Name"));
        displayNameBox.setMaxLength(64);
        displayNameBox.setValue(originalItem.displayName);
        addWidget(displayNameBox);
        
        String[] colors = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"};
        String[] colorNames = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        int colorX = centerX - 100;
        int colorY = startY + 103;
        
        for (int i = 0; i < colors.length; i++) {
            final String colorCode = colors[i];
            int buttonX = colorX + (i % 8) * 25;
            int buttonY = colorY + (i / 8) * 12;
            
            addRenderableWidget(Button.builder(Component.literal(colorNames[i]), button -> {
                String current = displayNameBox.getValue();
                displayNameBox.setValue(current + colorCode);
                displayNameBox.setFocused(true);
            }).bounds(buttonX, buttonY, 23, 10).build());
        }
        
        addRenderableWidget(Button.builder(Component.literal("§lB"), button -> {
            displayNameBox.setValue(displayNameBox.getValue() + "§l");
            displayNameBox.setFocused(true);
        }).bounds(centerX + 105, startY + 80, 20, 10).build());
        
        addRenderableWidget(Button.builder(Component.literal("§oI"), button -> {
            displayNameBox.setValue(displayNameBox.getValue() + "§o");
            displayNameBox.setFocused(true);
        }).bounds(centerX + 105, startY + 92, 20, 10).build());
        
        commandBox = new EditBox(this.font, centerX - 100, startY + 130, 200, 20, Component.literal("Command"));
        commandBox.setMaxLength(256);
        commandBox.setValue(originalItem.command);
        addWidget(commandBox);
        
        saveButton = Button.builder(Component.literal("Save"), button -> {
            saveItem();
        }).bounds(centerX - 100, startY + 170, 95, 20).build();
        addRenderableWidget(saveButton);
        
        cancelButton = Button.builder(Component.literal("Cancel"), button -> {
            minecraft.setScreen(parent);
        }).bounds(centerX + 5, startY + 170, 95, 20).build();
        addRenderableWidget(cancelButton);
    }

    private void saveItem() {
        errorMessage = "";
        
        String slotStr = slotBox.getValue().trim();
        String item = itemBox.getValue().trim();
        String displayName = displayNameBox.getValue().trim();
        String command = commandBox.getValue().trim();
        
        int slot;
        try {
            slot = Integer.parseInt(slotStr);
            MenuConfig.MenuData menu = MenuConfig.getMenu(menuName);
            if (menu == null || slot < 0 || slot >= menu.slots) {
                errorMessage = "Invalid slot number!";
                return;
            }
        } catch (NumberFormatException e) {
            errorMessage = "Slot must be a number!";
            return;
        }
        
        if (item.isEmpty()) {
            errorMessage = "Item name cannot be empty!";
            return;
        }
        
        if (displayName.isEmpty()) {
            displayName = item;
        }
        
        if (command.isEmpty()) {
            errorMessage = "Command cannot be empty!";
            return;
        }
        
        if (slot != originalItem.slot) {
            MenuConfig.removeMenuItem(menuName, originalItem.slot);
        }
        
        MenuConfig.addMenuItem(menuName, slot, item, displayName, command);
        
        if (parent instanceof EditMenuScreen) {
            ((EditMenuScreen) parent).refreshMenuData();
        }
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        int centerX = this.width / 2;
        int startY = 50;
        
        graphics.drawString(this.font, "Slot:", centerX - 100, startY - 12, 0xAAAAAA);
        slotBox.render(graphics, mouseX, mouseY, partialTick);
        
        graphics.drawString(this.font, "Item (minecraft:item):", centerX - 100, startY + 28, 0xAAAAAA);
        itemBox.render(graphics, mouseX, mouseY, partialTick);
        
        graphics.drawString(this.font, "Display Name:", centerX - 100, startY + 68, 0xAAAAAA);
        displayNameBox.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(this.font, "Renk Kodları:", centerX - 100, startY + 102, 0x888888);
        
        graphics.drawString(this.font, "Command:", centerX - 100, startY + 118, 0xAAAAAA);
        commandBox.render(graphics, mouseX, mouseY, partialTick);
        
        if (!errorMessage.isEmpty()) {
            graphics.drawCenteredString(this.font, errorMessage, this.width / 2, startY + 195, 0xFF5555);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
