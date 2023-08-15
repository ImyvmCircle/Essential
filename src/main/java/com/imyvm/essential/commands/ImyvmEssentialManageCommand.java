package com.imyvm.essential.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.Supplier;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public class ImyvmEssentialManageCommand extends BaseCommand {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public int runReload(CommandContext<ServerCommandSource> context) {
        CONFIG.loadAndSave();
        Supplier<Text> textSupplier = () -> tr("commands.imyvm_essential.reload.success");
        context.getSource().sendFeedback(textSupplier, true);
        return Command.SINGLE_SUCCESS;
    }
}