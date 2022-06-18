package com.imyvm.essential.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

public abstract class BaseCommand {
    protected BaseCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommand((CommandDispatcher<Object>) (Object) dispatcher));
    }

    protected abstract void registerCommand(CommandDispatcher<Object> dispatcher);

    protected CommandContext<ServerCommandSource> castCommandContext(CommandContext<Object> context) {
        return (CommandContext<ServerCommandSource>) (Object) context;
    }
}
