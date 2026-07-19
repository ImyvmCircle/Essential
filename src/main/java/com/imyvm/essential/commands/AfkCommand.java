package com.imyvm.essential.commands;

import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class AfkCommand extends BaseCommand {
    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        PlayerEntityMixinInterface playerMixin = (PlayerEntityMixinInterface) player;
        boolean status = playerMixin.imyvm$isAwayFromKeyboard();
        playerMixin.imyvm$updateActivity();
        playerMixin.imyvm$updateAwayFromKeyboard(!status);

        return Command.SINGLE_SUCCESS;
    }
}
