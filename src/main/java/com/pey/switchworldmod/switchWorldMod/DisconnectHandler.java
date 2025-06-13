package com.pey.switchworldmod.switchWorldMod;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class DisconnectHandler {
    public static void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;
        RegistryKey<World> currentKey = player.getWorld().getRegistryKey();

        if (currentKey.equals(SwitchWorldHandler.TEST_WORLD)) {
            SwitchWorldHandler.savePlayerData(player, server, currentKey);

            RegistryKey<World> previousKey = SwitchWorldHandler.readPreviousWorldKey(player, server);
            if (previousKey != null) {
                SwitchWorldHandler.loadPlayerData(player, server, previousKey);
            }

            player.changeGameMode(GameMode.SURVIVAL);
        }
    }
}
