package com.imyvm.essential.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

public abstract class BaseCommand {
    protected BaseCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommand(dispatcher));
    }

    protected abstract void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher);
}
