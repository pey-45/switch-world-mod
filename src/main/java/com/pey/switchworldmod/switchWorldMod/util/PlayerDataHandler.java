package com.pey.switchworldmod.switchWorldMod.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.pey.switchworldmod.switchWorldMod.util.Constants.MOD_NAME;
import static com.pey.switchworldmod.switchWorldMod.util.Constants.PLAYERDATA_PREVIOUS_DIMENSION_ENTRY;
import static com.pey.switchworldmod.switchWorldMod.util.Constants.TESTWORLD_PLAYERDATA_DIRECTORY;
import static com.pey.switchworldmod.switchWorldMod.util.Constants.TEST_WORLD;

public class PlayerDataHandler {

    public static void savePlayerData(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> key) {
        NbtCompound nbt = new NbtCompound();
        player.writeNbt(nbt);

        nbt.putString(PLAYERDATA_PREVIOUS_DIMENSION_ENTRY, key.getValue().toString());

        File file;
        if (key.equals(TEST_WORLD)) {
            // Guardar en testworldplayerdata
            File dir = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), TESTWORLD_PLAYERDATA_DIRECTORY);
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
            System.err.printf("[%s] Error saving player data: %s%n", MOD_NAME, e.getMessage());
            e.printStackTrace();
        }
    }


    public static void loadPlayerData(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> key) {
        ServerWorld world = server.getWorld(key);
        if (world == null) return;

        File file;
        if (key.equals(TEST_WORLD)) {
            file = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), String.format("%s/%s.dat", TESTWORLD_PLAYERDATA_DIRECTORY, player.getUuidAsString()));
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
                System.err.printf("[%s] Failed to load player data: %s%n", MOD_NAME, e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.printf("[%s] No previous data found for: %s%n", MOD_NAME, file.getAbsolutePath());
        }

    }

    public static RegistryKey<World> readPreviousWorldKey(ServerPlayerEntity player, MinecraftServer server) {
        File file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(),
                String.format("%s.dat", player.getUuidAsString()));

        if (file.exists()) {
            try {
                NbtCompound nbt = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                if (nbt.contains(PLAYERDATA_PREVIOUS_DIMENSION_ENTRY)) {
                    String id = nbt.getString(PLAYERDATA_PREVIOUS_DIMENSION_ENTRY);
                    return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(id));
                }
            } catch (IOException e) {
                System.err.printf("[%s] Could not read previous world key: %s%n", MOD_NAME, e.getMessage());
            }
        }

        return null;
    }

}
