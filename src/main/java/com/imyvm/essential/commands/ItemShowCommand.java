package com.imyvm.essential.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import static com.imyvm.essential.Translator.tr;

public class ItemShowCommand extends BaseCommand {
    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        Component message = this.getItemShowMessage(player);
        player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);

        return Command.SINGLE_SUCCESS;
    }

    private Component getItemShowMessage(ServerPlayer player) {
        ItemStack item = player.getMainHandItem();

        return switch (item.getCount()) {
            case 0 -> tr("commands.ss.empty", player.getName());
            case 1 -> tr("commands.ss.show.single", player.getName(), item.getDisplayName());
            default -> tr("commands.ss.show.multiple", player.getName(), item.getDisplayName(), item.getCount());
        };
    }
}
