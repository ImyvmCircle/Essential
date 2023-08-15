package com.imyvm.essential.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.imyvm.essential.Translator.tr;

public class ItemShowCommand extends BaseCommand {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        Text message = this.getItemShowMessage(player);
        player.getServer().getPlayerManager().broadcast(message, false);

        return Command.SINGLE_SUCCESS;
    }

    private Text getItemShowMessage(ServerPlayerEntity player) {
        ItemStack item = player.getMainHandStack();

        return switch (item.getCount()) {
            case 0 -> tr("commands.ss.empty", player.getName());
            case 1 -> tr("commands.ss.show.single", player.getName(), item.toHoverableText());
            default -> tr("commands.ss.show.multiple", player.getName(), item.toHoverableText(), item.getCount());
        };
    }
}
