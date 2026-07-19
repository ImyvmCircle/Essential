package com.imyvm.essential.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public class ImyvmEssentialManageCommand extends BaseCommand {
    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    public int runReload(CommandContext<CommandSourceStack> context) {
        CONFIG.loadAndSave();
        Supplier<Component> textSupplier = () -> tr("commands.imyvm_essential.reload.success");
        context.getSource().sendSuccess(textSupplier, true);
        return Command.SINGLE_SUCCESS;
    }
}