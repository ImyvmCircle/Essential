package com.imyvm.essential.commands;

import com.imyvm.essential.systems.ptt.BonusSupplier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BonusAcquireCommand extends BaseCommand {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public int runAcquire(CommandContext<ServerCommandSource> context, String typeId) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String token = StringArgumentType.getString(context, "token");

        try {
            BonusSupplier.getInstance().onTicketAcquire(player, typeId, token);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return Command.SINGLE_SUCCESS;
    }
}
