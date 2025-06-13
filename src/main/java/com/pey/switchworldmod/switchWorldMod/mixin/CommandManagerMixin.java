package com.pey.switchworldmod.switchWorldMod.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.pey.switchworldmod.switchWorldMod.SwitchWorldHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private void onExecute(ParseResults<ServerCommandSource> parse, String command, CallbackInfo ci) throws CommandSyntaxException {
        ServerCommandSource source = parse.getContext().getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) return;

        if (player.getWorld().getRegistryKey().equals(SwitchWorldHandler.TEST_WORLD)) {
            CommandDispatcher<ServerCommandSource> dispatcher = ((CommandManager)(Object)this).getDispatcher();
            CommandContextBuilder<ServerCommandSource> context = dispatcher.parse(command, source).getContext();

            if (context.getNodes().isEmpty()) return;

            CommandNode<ServerCommandSource> commandNode = context.getNodes().getFirst().getNode();
            Predicate<ServerCommandSource> requirement = commandNode.getRequirement();

            // Si normalmente no podrías ejecutarlo sin op...
            if (!requirement.test(source.withLevel(0))) {
                source.sendError(Text.literal("This command is not allowed in this world"));
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
            }
        }
    }


}

