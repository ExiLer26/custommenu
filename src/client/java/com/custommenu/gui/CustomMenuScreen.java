package com.custommenu.gui;

import com.custommenu.config.MenuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CustomMenuScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final MenuConfig.MenuData menuData;
    private final int imageWidth = 176;
    private int imageHeight;
    private int leftPos;
    private int topPos;
    private final List<MenuSlot> slots = new ArrayList<>();

    public CustomMenuScreen(String menuName) {
        super(Component.literal(menuName));
        this.menuData = MenuConfig.getMenu(menuName);
        if (this.menuData != null) {
            int rows = this.menuData.slots / 9;
            this.imageHeight = 114 + rows * 18;
        } else {
            this.imageHeight = 166;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        slots.clear();
        if (menuData != null) {
            for (MenuConfig.MenuItem menuItem : menuData.items) {
                if (menuItem.slot < menuData.slots) {
                    int row = menuItem.slot / 9;
                    int col = menuItem.slot % 9;
                    slots.add(new MenuSlot(menuItem, leftPos + col * 18 + 8, topPos + row * 18 + 18));
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        if (menuData == null) {
            graphics.drawCenteredString(this.font, "Menu not found!", this.width / 2, this.height / 2, 0xFF0000);
            return;
        }

        int x = leftPos;
        int y = topPos;
        int rows = menuData.slots / 9;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, rows * 18 + 17);
        graphics.blit(TEXTURE, x, y + rows * 18 + 17, 0, 126, imageWidth, 96);

        graphics.drawString(this.font, menuData.title, x + 8, y + 6, 4210752, false);

        for (MenuSlot slot : slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slot.x, slot.y);
                graphics.renderItemDecorations(this.font, stack, slot.x, slot.y);
            }
        }

        for (MenuSlot slot : slots) {
            if (isHoveringSlot(slot, mouseX, mouseY)) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    graphics.renderTooltip(this.font, stack, mouseX, mouseY);
                }
                break;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (MenuSlot slot : slots) {
                if (isHoveringSlot(slot, (int) mouseX, (int) mouseY)) {
                    if (slot.menuItem != null && !slot.menuItem.command.isEmpty()) {
                        String command = slot.menuItem.command;
                        if (!command.startsWith("/")) {
                            command = "/" + command;
                        }
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.connection.sendCommand(command.substring(1));
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isHoveringSlot(MenuSlot slot, int mouseX, int mouseY) {
        return mouseX >= slot.x && mouseX < slot.x + 16 && mouseY >= slot.y && mouseY < slot.y + 16;
    }

    private static class MenuSlot {
        public final MenuConfig.MenuItem menuItem;
        public final int x;
        public final int y;

        public MenuSlot(MenuConfig.MenuItem menuItem, int x, int y) {
            this.menuItem = menuItem;
            this.x = x;
            this.y = y;
        }

        public ItemStack getStack() {
            String itemId = "minecraft:" + menuItem.itemName;
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
            
            if (item != null && item != Items.AIR) {
                ItemStack stack = new ItemStack(item);
                stack.setHoverName(Component.literal(menuItem.displayName));
                return stack;
            }
            return ItemStack.EMPTY;
        }
    }
}
