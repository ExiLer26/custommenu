package com.custommenu.gui;

import com.custommenu.config.MenuConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CreateMenuScreen extends Screen {
    private final Screen parent;
    private EditBox nameBox;
    private EditBox titleBox;
    private EditBox slotsBox;
    private Button createButton;
    private Button cancelButton;
    private String errorMessage = "";

    public CreateMenuScreen(Screen parent) {
        super(Component.literal("Create New Menu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 60;

        // Name field
        nameBox = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.literal("Menu Name"));
        nameBox.setMaxLength(32);
        nameBox.setHint(Component.literal("my_menu"));
        addWidget(nameBox);

        // Title field
        titleBox = new EditBox(this.font, centerX - 100, startY + 40, 200, 20, Component.literal("Menu Title"));
        titleBox.setMaxLength(64);
        titleBox.setHint(Component.literal("My Custom Menu"));
        addWidget(titleBox);

        // Slots field
        slotsBox = new EditBox(this.font, centerX - 100, startY + 80, 200, 20, Component.literal("Slots"));
        slotsBox.setMaxLength(2);
        slotsBox.setHint(Component.literal("27"));
        addWidget(slotsBox);

        // Create button
        createButton = Button.builder(Component.literal("Create"), button -> {
            createMenu();
        }).bounds(centerX - 100, startY + 120, 95, 20).build();
        addRenderableWidget(createButton);

        // Cancel button
        cancelButton = Button.builder(Component.literal("Cancel"), button -> {
            minecraft.setScreen(parent);
        }).bounds(centerX + 5, startY + 120, 95, 20).build();
        addRenderableWidget(cancelButton);
    }

    private void createMenu() {
        errorMessage = "";

        String name = nameBox.getValue().trim();
        String title = titleBox.getValue().trim();
        String slotsStr = slotsBox.getValue().trim();

        if (name.isEmpty()) {
            errorMessage = "Menu name cannot be empty!";
            return;
        }

        if (title.isEmpty()) {
            title = "Custom Menu";
        }

        int slots;
        try {
            slots = Integer.parseInt(slotsStr);
            if (slots < 9 || slots > 54) {
                errorMessage = "Slots must be between 9 and 54!";
                return;
            }
            slots = (slots / 9) * 9; // Round to nearest 9
        } catch (NumberFormatException e) {
            errorMessage = "Invalid slots number!";
            return;
        }

        if (MenuConfig.createMenu(name, slots, title)) {
            if (parent instanceof ConfigMenuScreen) {
                ((ConfigMenuScreen) parent).refreshMenuList();
            }
            minecraft.setScreen(parent);
        } else {
            errorMessage = "Failed to create menu! (Name already exists or max limit reached)";
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = 60;

        graphics.drawString(this.font, "Menu Name:", centerX - 100, startY - 12, 0xAAAAAA);
        nameBox.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, "Menu Title:", centerX - 100, startY + 28, 0xAAAAAA);
        titleBox.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(this.font, "Renk Kodları:", centerX - 100, startY + 62, 0x888888);

        graphics.drawString(this.font, "Slots (9-54):", centerX - 100, startY + 78, 0xAAAAAA);
        slotsBox.render(graphics, mouseX, mouseY, partialTick);

        if (!errorMessage.isEmpty()) {
            graphics.drawCenteredString(this.font, errorMessage, this.width / 2, startY + 155, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}