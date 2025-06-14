package com.pey.switchworldmod.switchWorldMod;

import com.sk89q.worldedit.entity.Player;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwitchWorldEvents {
    private static final Map<UUID, RegistryKey<World>> previousWorlds = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                RegistryKey<World> current = player.getWorld().getRegistryKey();
                RegistryKey<World> previous = previousWorlds.get(player.getUuid());

                if (previous == null || !previous.equals(current)) {
                    previousWorlds.put(player.getUuid(), current);
                    onWorldChange(player, previous, current);
                }
            }
        });
    }

    private static void onWorldChange(ServerPlayerEntity player, RegistryKey<World> from, RegistryKey<World> to) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (to.equals(SwitchWorldHandler.TEST_WORLD)) {
            player.changeGameMode(GameMode.CREATIVE);
        } else {
            player.changeGameMode(GameMode.SURVIVAL);
        }

        server.getCommandManager().sendCommandTree(player);
    }
}
