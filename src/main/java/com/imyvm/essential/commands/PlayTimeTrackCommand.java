package com.imyvm.essential.commands;

import com.imyvm.essential.systems.ptt.PlayTimeTrackSystem;
import com.imyvm.essential.systems.ptt.PlayerTrackData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;

public class PlayTimeTrackCommand extends BaseCommand {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        PlayerTrackData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUuid()).getPlayerTrackData();
        player.sendMessage(PlayTimeTrackSystem.getInstance().getPlayTimeText(data));

        return Command.SINGLE_SUCCESS;
    }
}
