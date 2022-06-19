package com.imyvm.essential.commands;

import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.control.TeleportRequest;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class TeleportCommand extends BaseCommand {
    @Override
    protected void registerCommand(CommandDispatcher<Object> dispatcher) {
        registerRequestCommand(dispatcher, "tpa", TeleportRequest.TeleportType.TELEPORT_TO);
        registerRequestCommand(dispatcher, "tpahere", TeleportRequest.TeleportType.TELEPORT_HERE);

        registerAcceptRejectCommand(dispatcher, "tpaccept", TeleportRequest::accept);
        registerAcceptRejectCommand(dispatcher, "tpreject", TeleportRequest::reject);

        dispatcher.register(
            LiteralArgumentBuilder.literal("tpcancel")
                .requires(source -> ((ServerCommandSource) source).isExecutedByPlayer())
                .executes(ctx -> {
                    ServerPlayerEntity player = this.castCommandContext(ctx).getSource().getPlayer();

                    TeleportRequest request = ((PlayerEntityMixinInterface) player).getRequestAsSender();
                    if (request == null) {
                        player.sendMessage(ImmediatelyTranslator.translatable("commands.tpcancel.failed.no_request"));
                        return -1;
                    }

                    request.cancel();
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private void registerRequestCommand(CommandDispatcher<Object> dispatcher, String command, TeleportRequest.TeleportType type) {
        dispatcher.register(
            LiteralArgumentBuilder.literal(command)
                .requires(source -> ((ServerCommandSource) source).isExecutedByPlayer())
                .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.player())
                    .executes(ctx -> {
                        CommandContext<ServerCommandSource> context = this.castCommandContext(ctx);
                        ServerPlayerEntity player = this.castCommandContext(ctx).getSource().getPlayer();

                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                        TeleportRequest request = new TeleportRequest(player, target, type);
                        boolean result = request.issue();

                        return result ? Command.SINGLE_SUCCESS : -1;
                    })));
    }

    private void registerAcceptRejectCommand(CommandDispatcher<Object> dispatcher, String command, Consumer<TeleportRequest> callback) {
        dispatcher.register(
            LiteralArgumentBuilder.literal(command)
                .requires(source -> ((ServerCommandSource) source).isExecutedByPlayer())
                .executes(ctx -> {
                    ServerPlayerEntity player = this.castCommandContext(ctx).getSource().getPlayer();

                    TeleportRequest request = ((PlayerEntityMixinInterface) player).getRequestAsReceiver();
                    if (request == null) {
                        player.sendMessage(ImmediatelyTranslator.translatable("commands." + command + ".failed.no_request"));
                        return -1;
                    }

                    callback.accept(request);
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
