package com.pey.switchworldmod.switchWorldMod.handlers;

import com.pey.switchworldmod.switchWorldMod.util.PlayerDataHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.pey.switchworldmod.switchWorldMod.util.Constants.TEST_WORLD;

public class WorldChangeHandler {
    private static final Map<UUID, RegistryKey<World>> previousWorlds = new HashMap<>();

    public static void register() {
        // executes on every tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // for every player
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // get the current dimension
                RegistryKey<World> current = player.getWorld().getRegistryKey();
                // get the previous dimension from the map
                RegistryKey<World> previous = previousWorlds.get(player.getUuid());

                // if there is no previous dimension (just logged) or it changes (no matter how)
                if (previous == null || !previous.equals(current)) {

                    // change gamemode based on the dimension
                    if (current.equals(TEST_WORLD)) {
                        player.changeGameMode(GameMode.CREATIVE);
                    } else {
                        player.changeGameMode(GameMode.SURVIVAL);
                    }

                    // save the current dimension to the map
                    previousWorlds.put(player.getUuid(), current);

                    // save the previous dimension's data if exists
                    if (previous != null) {
                        PlayerDataHandler.savePlayerData(player, server, previous);
                    }

                    // load the new dimension's data
                    PlayerDataHandler.loadPlayerData(player, server, current);
                }
            }
        });

        // STANDARD PLAYERDATA DEFINES PLAYER:
        // - IN TESTWORLD
        // - IN CREATIVE

    }
}
