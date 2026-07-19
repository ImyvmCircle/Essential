package com.imyvm.essential.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;

public class CommandUtil {
    private CommandUtil() {
    }

    public static CommandSourceStack getServerCommandSource(MinecraftServer server) {
        return server.createCommandSourceStack();
    }
}
