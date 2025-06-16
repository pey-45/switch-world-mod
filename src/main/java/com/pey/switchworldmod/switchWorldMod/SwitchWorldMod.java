package com.pey.switchworldmod.switchWorldMod;

import com.pey.switchworldmod.switchWorldMod.handlers.LoginHandler;
import com.pey.switchworldmod.switchWorldMod.handlers.WorldChangeHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SwitchWorldMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SwitchWorldCommand.register(dispatcher));
        LoginHandler.register();
        WorldChangeHandler.register();
    }

}

