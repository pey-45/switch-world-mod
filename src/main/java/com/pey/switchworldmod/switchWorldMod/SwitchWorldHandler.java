package com.pey.switchworldmod.switchWorldMod;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SwitchWorldHandler {

    public static final String MOD_ID = "switch-world-mod";
    public static final RegistryKey<World> TEST_WORLD =
            RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, "testworld"));

    public static int switchWorld(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        MinecraftServer server = source.getServer();

        assert player != null;

        RegistryKey<World> currentKey = player.getWorld().getRegistryKey();
        RegistryKey<World> targetKey;

        if (currentKey.equals(TEST_WORLD)) {
            RegistryKey<World> previousKey = readPreviousWorldKey(player, server);
            targetKey = previousKey != null ? previousKey : World.OVERWORLD;
        } else {
            targetKey = TEST_WORLD;
        }

        ServerWorld targetWorld = server.getWorld(targetKey);
        if (targetWorld == null) {
            source.sendError(Text.literal("Destination not found"));
            return 0;
        }

        savePlayerData(player, server, currentKey);

        loadPlayerData(player, server, targetKey);

        player.teleport(targetWorld, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());

        player.changeGameMode(targetKey.equals(TEST_WORLD) ? GameMode.CREATIVE : GameMode.SURVIVAL);

        source.sendFeedback(() -> Text.literal(String.format("Teleported to %s", targetKey.getValue())), false);

        return 1;
    }

    public static void savePlayerData(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> key) {
        NbtCompound nbt = new NbtCompound();
        player.writeNbt(nbt);

        if (!key.equals(TEST_WORLD)) {
            nbt.putString("SwitchWorldMod_PreviousDimension", key.getValue().toString());
        }

        File file;
        if (key.equals(TEST_WORLD)) {
            // Guardar en testworldplayerdata
            File dir = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "playerdata/testworld");
            if (!dir.exists()) dir.mkdirs();
            file = new File(dir, String.format("%s.dat", player.getUuidAsString()));
        } else if (key.equals(World.OVERWORLD)
                || key.equals(World.NETHER)
                || key.equals(World.END)) {
            file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), String.format("%s.dat", player.getUuidAsString()));
        } else {
            return;
        }

        try {
            NbtIo.writeCompressed(nbt, file.toPath());
        } catch (IOException e) {
            System.err.printf("[SwitchWorldMod] Error saving player data: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }


    public static void loadPlayerData(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> key) {
        ServerWorld world = server.getWorld(key);
        if (world == null) return;

        File file;
        if (key.equals(TEST_WORLD)) {
            file = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), String.format("playerdata/testworld/%s.dat", player.getUuidAsString()));
        } else if (key.equals(World.OVERWORLD)
                || key.equals(World.NETHER)
                || key.equals(World.END)) {
            file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(),  String.format("%s.dat", player.getUuidAsString()));
        } else {
            return;
        }

        if (file.exists()) {
            try {
                NbtCompound nbt = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                player.readNbt(nbt);

                // Restaurar posición si está en el NBT
                if (nbt.contains("Pos")) {
                    List<Double> pos = nbt.getList("Pos", 6).stream()
                            .map(tag -> ((NbtDouble) tag).doubleValue())
                            .toList();
                    if (pos.size() == 3) {
                        player.teleport(
                                world,
                                pos.get(0), pos.get(1), pos.get(2),
                                player.getYaw(), player.getPitch()
                        );
                        return;
                    }
                }
            } catch (IOException e) {
                System.err.printf("[SwitchWorldMod] Failed to load player data: %s%n", e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.printf("[SwitchWorldMod] No previous data found for: %s%n", file.getAbsolutePath());
        }

        player.teleport(
                world,
                world.getSpawnPos().getX() + 0.5,
                world.getSpawnPos().getY() + 1,
                world.getSpawnPos().getZ() + 0.5,
                player.getYaw(), player.getPitch()
        );
    }

    public static RegistryKey<World> readPreviousWorldKey(ServerPlayerEntity player, MinecraftServer server) {
        File file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(),
                String.format("%s.dat", player.getUuidAsString()));

        if (file.exists()) {
            try {
                NbtCompound nbt = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                if (nbt.contains("SwitchWorldMod_PreviousDimension")) {
                    String id = nbt.getString("SwitchWorldMod_PreviousDimension");
                    return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(id));
                }
            } catch (IOException e) {
                System.err.printf("[SwitchWorldMod] Could not read previous world key: %s%n", e.getMessage());
            }
        }

        return null;
    }

}
