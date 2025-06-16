package com.pey.switchworldmod.switchWorldMod.handlers;

import com.pey.switchworldmod.switchWorldMod.util.PlayerDataHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static com.pey.switchworldmod.switchWorldMod.util.Constants.*;

public class SwitchWorldHandler {

    public static int switchWorld(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command can only be executed by a player"));
            return 0;
        }

        MinecraftServer server = source.getServer();
        RegistryKey<World> currentKey = player.getWorld().getRegistryKey();
        RegistryKey<World> targetKey;

        if (currentKey.equals(TEST_WORLD)) {
            RegistryKey<World> previousKey = PlayerDataHandler.readPreviousWorldKey(player, server);
            targetKey = previousKey != null
                    ? previousKey
                    : World.OVERWORLD;
        } else {
            targetKey = TEST_WORLD;
        }

        ServerWorld targetWorld = server.getWorld(targetKey);
        if (targetWorld == null) {
            source.sendError(Text.literal("Destination not found"));
            return 0;
        }

        player.teleport(targetWorld, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());

        String destName = targetKey.getValue().toString().replace("minecraft:", "").replace(String.format("%s:", MOD_ID), "");
        source.sendFeedback(() -> Text.literal(String.format("Teleported to %s", destName)), false);

        return 1;
    }
}
