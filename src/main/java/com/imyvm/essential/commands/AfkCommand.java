package com.imyvm.essential.commands;

import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AfkCommand extends BaseCommand {
    @Override
    protected void registerCommand(CommandDispatcher<Object> dispatcher) {
        dispatcher.register(
			LiteralArgumentBuilder.literal("afk")
				.executes(ctx -> {
					CommandContext<ServerCommandSource> context = this.castCommandContext(ctx);
					ServerPlayerEntity player = context.getSource().getPlayer();
					if (player == null) {
						context.getSource().sendError(ImmediatelyTranslator.translatable("commands.afk.failed.not_player"));
						return 0;
					}

					PlayerEntityMixinInterface playerMixin = (PlayerEntityMixinInterface) player;
					playerMixin.updateAwayFromKeyboard(!playerMixin.isAwayFromKeyboard());

					return Command.SINGLE_SUCCESS;
				})
		);
    }
}
