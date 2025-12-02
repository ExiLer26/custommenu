package com.custommenu;

import com.custommenu.commands.MenuCommand;
import com.custommenu.config.MenuConfig;
import com.custommenu.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomMenuMod implements ModInitializer {
    public static final String MOD_ID = "custommenu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Custom Menu Mod initializing...");
        
        NetworkHandler.register();
        
        MenuConfig.loadMenus();
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MenuCommand.register(dispatcher);
        });
        
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MenuConfig.loadMenus();
            LOGGER.info("Menus reloaded on server start!");
        });
        
        LOGGER.info("Custom Menu Mod initialized!");
    }
}
