package com.imyvm.essential.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.imyvm.essential.Translator.tr;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemShowCommand extends BaseCommand {
    @Override
    protected void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("ss")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    Text message = this.getItemShowMessage(player);
                    player.getServer().getPlayerManager().broadcast(message, MessageType.SYSTEM);

                    return Command.SINGLE_SUCCESS;
                }));
    }

    private Text getItemShowMessage(ServerPlayerEntity player) {
        ItemStack item = player.getMainHandStack();
        if (item.isEmpty()) {
            return tr("commands.ss.empty", player.getName());
        } else if (item.getCount() == 1) {
            return tr("commands.ss.show.single", player.getName(), item.toHoverableText());
        } else {
            return tr("commands.ss.show.multiple", player.getName(), item.toHoverableText(), item.getCount());
        }
    }
}
