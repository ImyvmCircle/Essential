package com.imyvm.essential.util;

import com.imyvm.essential.EssentialMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class CommandUtil {
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
}
