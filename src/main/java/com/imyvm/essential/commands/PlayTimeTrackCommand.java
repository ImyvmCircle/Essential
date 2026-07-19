package com.imyvm.essential.commands;

import com.imyvm.essential.systems.ptt.PlayTimeTrackSystem;
import com.imyvm.essential.systems.ptt.PlayerTrackData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;

public class PlayTimeTrackCommand extends BaseCommand {
    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        PlayerTrackData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID()).getPlayerTrackData();
        player.sendSystemMessage(PlayTimeTrackSystem.getInstance().getPlayTimeText(data));

        return Command.SINGLE_SUCCESS;
    }
}
