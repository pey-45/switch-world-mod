package com.pey.switchworldmod.switchWorldMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class SwitchWorldMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SwitchWorldCommand.register(dispatcher));
        ServerPlayConnectionEvents.INIT.register(PlayerConnectionHandler::onTriggerConnection);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionHandler::onTriggerConnection);
    }

}

