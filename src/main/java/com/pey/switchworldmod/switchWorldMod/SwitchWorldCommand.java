package com.pey.switchworldmod.switchWorldMod;

import com.mojang.brigadier.CommandDispatcher;
import com.pey.switchworldmod.switchWorldMod.handlers.SwitchWorldHandler;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SwitchWorldCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("switchworld").executes(context ->
                SwitchWorldHandler.switchWorld(context.getSource())
        ));
    }

}
