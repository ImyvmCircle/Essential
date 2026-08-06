package com.imyvm.essential.commands;

import com.imyvm.essential.systems.ptt.BonusSupplier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class BonusAcquireCommand extends BaseCommand {
    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    public int runAcquire(CommandContext<CommandSourceStack> context, String typeId) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        String token = StringArgumentType.getString(context, "token");

        BonusSupplier.getInstance().onTicketAcquire(player, typeId, token);

        return Command.SINGLE_SUCCESS;
    }
}
