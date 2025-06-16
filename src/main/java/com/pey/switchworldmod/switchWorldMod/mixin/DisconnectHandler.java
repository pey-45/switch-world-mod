package com.pey.switchworldmod.switchWorldMod.mixin;

import com.pey.switchworldmod.switchWorldMod.util.PlayerDataHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pey.switchworldmod.switchWorldMod.util.Constants.TEST_WORLD;

@Mixin(PlayerManager.class)
public class DisconnectHandler {

    // intercepts minecraft's automatic playerdata save method for a player
    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void onSavePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        // if player in the test world
        if (player.getWorld().getRegistryKey().equals(TEST_WORLD)) {
            MinecraftServer server = player.getServer();
            // saves the data in the test world's playerdata directory
            PlayerDataHandler.savePlayerData(player, server, TEST_WORLD);
            // cancels minecraft's automatic playerdata save method
            ci.cancel();
        }
    }

}
