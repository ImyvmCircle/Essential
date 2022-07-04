package com.imyvm.essential.commands;

import com.imyvm.essential.EssentialStatistics;
import com.imyvm.essential.util.TimeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.imyvm.essential.Translator.tr;

public class PlayTimeTrackCommand extends BaseCommand {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        int time = player.getStatHandler().getStat(EssentialStatistics.getStatOf(EssentialStatistics.PLAY_TIME_TRACK));
        Text timeFormatted = TimeUtil.formatDuration(time);

        player.sendMessage(tr("commands.ptt.message", timeFormatted));

        return Command.SINGLE_SUCCESS;
    }
}
