package com.pey.switchworldmod.switchWorldMod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static com.pey.switchworldmod.switchWorldMod.util.Constants.SWITCH_WORLD_COMMAND;
import static net.minecraft.server.command.CommandManager.literal;

public class SwitchWorldCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(SWITCH_WORLD_COMMAND).executes(context ->
                SwitchWorldHandler.switchWorld(context.getSource())
        ));
    }

}
