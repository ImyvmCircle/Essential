package com.imyvm.essential.commands;

import com.imyvm.essential.ImmediatelyTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ItemShowCommand extends BaseCommand {
    @Override
    protected void registerCommand(CommandDispatcher<Object> dispatcher) {
        dispatcher.register(
            LiteralArgumentBuilder.literal("ss")
                .requires(source -> ((ServerCommandSource) source).isExecutedByPlayer())
                .executes(ctx -> {
                    ServerPlayerEntity player = this.castCommandContext(ctx).getSource().getPlayer();

                    Text message = this.getItemShowMessage(player);
                    player.getServer().getPlayerManager().broadcast(message, MessageType.SYSTEM);

                    return Command.SINGLE_SUCCESS;
                }));
    }

    private Text getItemShowMessage(ServerPlayerEntity player) {
        ItemStack item = player.getMainHandStack();
        if (item.isEmpty()) {
            return ImmediatelyTranslator.translatable("commands.ss.empty", player.getName());
        } else if (item.getCount() == 1) {
            return ImmediatelyTranslator.translatable("commands.ss.show.single", player.getName(), item.toHoverableText());
        } else {
            return ImmediatelyTranslator.translatable("commands.ss.show.multiple", player.getName(), item.toHoverableText(), item.getCount());
        }
    }
}
