package com.pey.switchworldmod.switchWorldMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SwitchWorld implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SwitchWorldCommand.register(dispatcher));
        SwitchWorldEvents.register();
    }

}

