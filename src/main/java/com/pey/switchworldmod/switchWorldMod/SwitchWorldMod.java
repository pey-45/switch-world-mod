
package com.pey.switchworldmod;

import com.pey.switchworldmod.data.SwitchWorldStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class SwitchWorldMod implements ModInitializer {

    public static final String MOD_ID = "switch-world-mod";
    public static final RegistryKey<World> TEST_WORLD =
        RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, "test_world"));

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("switchworld").executes(context -> {
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity player = source.getPlayer();
                MinecraftServer server = source.getServer();

                if (player == null) return 0;

                RegistryKey<World> currentKey = player.getWorld().getRegistryKey();
                RegistryKey<World> targetKey = currentKey.equals(TEST_WORLD) ? World.OVERWORLD : TEST_WORLD;

                ServerWorld targetWorld = server.getWorld(targetKey);
                if (targetWorld == null) {
                    source.sendError(Text.literal("No se encontró el mundo destino."));
                    return 0;
                }

                SwitchWorldStorage storage = SwitchWorldStorage.get(server);

                // Guardar estado actual
                storage.savePlayerState(player, currentKey);

                // Restaurar estado anterior
                storage.restorePlayerState(player, targetKey);

                // Teleportar
                player.teleport(
                        targetWorld,
                        player.getX(), player.getY(), player.getZ(),
                        player.getYaw(), player.getPitch()
                );

                // Cambiar modo
                player.changeGameMode(targetKey.equals(TEST_WORLD) ? GameMode.CREATIVE : GameMode.SURVIVAL);
                source.sendFeedback(() -> Text.literal("Mundo cambiado a: " + targetKey.getValue()), false);

                return 1;
            }));
        });
    }
}
