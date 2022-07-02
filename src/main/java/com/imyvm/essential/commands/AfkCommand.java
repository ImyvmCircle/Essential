package com.imyvm.essential.commands;

import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.imyvm.essential.Translator.tr;
import static net.minecraft.server.command.CommandManager.literal;

public class AfkCommand extends BaseCommand {
    @Override
    protected void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("afk")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        context.getSource().sendError(tr("commands.afk.failed.not_player"));
                        return 0;
                    }

                    PlayerEntityMixinInterface playerMixin = (PlayerEntityMixinInterface) player;
                    boolean status = playerMixin.imyvm$isAwayFromKeyboard();
                    playerMixin.imyvm$updateActivity();
                    playerMixin.imyvm$updateAwayFromKeyboard(!status);

                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
