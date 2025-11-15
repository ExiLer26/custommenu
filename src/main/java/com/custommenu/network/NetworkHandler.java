package com.custommenu.network;

import com.custommenu.CustomMenuMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CustomMenuMod.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(PacketOpenMenu.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketOpenMenu::encode)
            .decoder(PacketOpenMenu::decode)
            .consumerMainThread(PacketOpenMenu::handle)
            .add();

        CustomMenuMod.LOGGER.info("Network packets registered");
    }
}
