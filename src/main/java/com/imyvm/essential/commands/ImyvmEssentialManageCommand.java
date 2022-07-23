package com.imyvm.essential.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public class ImyvmEssentialManageCommand extends BaseCommand {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public int runReload(CommandContext<ServerCommandSource> context) {
        CONFIG.loadAndSave();
        context.getSource().sendFeedback(tr("commands.imyvm_essential.reload.success"), true);
        return Command.SINGLE_SUCCESS;
    }
}
