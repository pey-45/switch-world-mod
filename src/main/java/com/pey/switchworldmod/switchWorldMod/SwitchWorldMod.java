package com.pey.switchworldmod.switchWorldMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtDouble;
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

import static net.minecraft.server.command.CommandManager.literal;

public class SwitchWorldMod implements ModInitializer {

    public static final String MOD_ID = "switch-world-mod";
    public static final RegistryKey<World> TEST_WORLD =
            RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, "test_world"));

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("switchworld").executes(context -> {
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity player = source.getPlayer();
                MinecraftServer server = source.getServer();

                assert player != null;

                RegistryKey<World> currentKey = player.getWorld().getRegistryKey();
                RegistryKey<World> targetKey = currentKey.equals(TEST_WORLD) ? World.OVERWORLD : TEST_WORLD;

                ServerWorld targetWorld = server.getWorld(targetKey);
                if (targetWorld == null) {
                    source.sendError(Text.literal("Destination not found"));
                    return 0;
                }

                // Guardar estado actual del jugador en archivo
                savePlayerData(player, server, currentKey);

                // Cargar estado previo del nuevo mundo
                loadPlayerData(player, server, targetKey);

                // Cambiar de dimensión
                player.teleport(targetWorld, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());

                player.changeGameMode(targetKey.equals(TEST_WORLD) ? GameMode.CREATIVE : GameMode.SURVIVAL);

                source.sendFeedback(() -> Text.literal("Teleported to " + targetKey.getValue()), false);

                return 1;
            }));
        });
    }

    private void savePlayerData(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> key) {
        NbtCompound nbt = new NbtCompound();
        player.writeNbt(nbt);

        File file;
        if (key.equals(SwitchWorldMod.TEST_WORLD)) {
            // Guardar en testworldplayerdata
            File dir = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "testworldplayerdata");
            if (!dir.exists()) dir.mkdirs();
            file = new File(dir, player.getUuidAsString() + ".dat");
        } else if (key.equals(World.OVERWORLD)) {
            // Guardar en playerdata nativo
            file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), player.getUuidAsString() + ".dat");
        } else {
            return; // No guardamos en otros mundos
        }

        try {
            NbtIo.writeCompressed(nbt, file.toPath());
            System.out.println("[SwitchWorldMod] Saved player data to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[SwitchWorldMod] Error saving player data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadPlayerData(ServerPlayerEntity player, MinecraftServer server, RegistryKey<World> key) {
        ServerWorld world = server.getWorld(key);
        if (world == null) return;

        File file;
        if (key.equals(SwitchWorldMod.TEST_WORLD)) {
            file = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "testworldplayerdata/" + player.getUuidAsString() + ".dat");
        } else if (key.equals(World.OVERWORLD)) {
            file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), player.getUuidAsString() + ".dat");
        } else {
            return; // No cargamos en otros mundos
        }

        if (file.exists()) {
            try {
                NbtCompound nbt = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                player.readNbt(nbt);
                System.out.println("[SwitchWorldMod] Loaded player data from: " + file.getAbsolutePath());

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
                        return; // evitar teleport por defecto
                    }
                }
            } catch (IOException e) {
                System.err.println("[SwitchWorldMod] Failed to load player data: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[SwitchWorldMod] No previous data found for: " + file.getAbsolutePath());
        }

        // Si no había NBT con posición, enviar al spawn
        player.teleport(
                world,
                world.getSpawnPos().getX() + 0.5,
                world.getSpawnPos().getY() + 1,
                world.getSpawnPos().getZ() + 0.5,
                player.getYaw(), player.getPitch()
        );
    }
}

