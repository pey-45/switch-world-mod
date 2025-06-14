package com.pey.switchworldmod.switchWorldMod.mixin;

import com.pey.switchworldmod.switchWorldMod.SwitchWorldHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void onSavePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().getRegistryKey().equals(SwitchWorldHandler.TEST_WORLD)) {
            MinecraftServer server = player.getServer();
            SwitchWorldHandler.savePlayerData(player, server, SwitchWorldHandler.TEST_WORLD);
            ci.cancel();
        }
    }
}
