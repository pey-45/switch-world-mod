package com.pey.switchworldmod.switchWorldMod.handlers;

import com.pey.switchworldmod.switchWorldMod.util.PlayerDataHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import static com.pey.switchworldmod.switchWorldMod.util.Constants.TEST_WORLD;

public class LoginHandler {

    public static void register() {
        // execute when a player joins the server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // if player in the test world
            if (player.getWorld().getRegistryKey().equals(TEST_WORLD)) {
                // loads its data
                PlayerDataHandler.loadPlayerData(player, server, TEST_WORLD);
            }
        });
    }
}
