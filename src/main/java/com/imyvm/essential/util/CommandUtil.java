package com.imyvm.essential.util;

import com.imyvm.essential.EssentialMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class CommandUtil {
    private CommandUtil() {
    }

    public static ServerCommandSource getServerCommandSource(MinecraftServer server) {
        ServerWorld world = server.getOverworld();

        return new ServerCommandSource(
            CommandOutput.DUMMY,
            world == null ? Vec3d.ZERO : Vec3d.of(world.getSpawnPos()),
            Vec2f.ZERO,
            world,
            2,
            EssentialMod.MOD_ID,
            Text.literal("Essential"),
            server,
            null);
    }

    public static Text asSuggestCommandText(String command) {
        Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return Text.literal(command).setStyle(style);
    }
}
