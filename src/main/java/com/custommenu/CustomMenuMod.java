package com.custommenu;

import com.custommenu.commands.MenuCommand;
import com.custommenu.config.MenuConfig;
import com.custommenu.handlers.KeyHandler;
import com.custommenu.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CustomMenuMod.MODID)
public class CustomMenuMod {
    public static final String MODID = "custommenu";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomMenuMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(KeyHandler::registerKeyBindings);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MenuConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
            MenuConfig.loadMenus();
            LOGGER.info("Custom Menu Mod initialized!");
        });
        
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }
    
    @SubscribeEvent
    public void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        MenuConfig.loadMenus();
        LOGGER.info("Menus reloaded on server start!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Client setup complete!");
        });
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        MenuCommand.register(event.getDispatcher());
    }
}
