package com.imyvm.essential;

import com.imyvm.essential.commands.*;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import com.imyvm.essential.tasks.PlayTimeTrackTask;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class EssentialMod implements ModInitializer {
    public static final String MOD_ID = "imyvm_essential";
    public static final Logger LOGGER = LoggerFactory.getLogger("Essential");
    public static final ModConfig CONFIG = new ModConfig();
    public static final LazyTicker LAZY_TICKER = new LazyTicker(20);

    @Override
    public void onInitialize() {
        registerCommands();
        registerEvents();
        registerLazyTick();

        CONFIG.loadAndSave();
        EssentialStatistics.initialize();
        EssentialGameRules.initialize();

		LOGGER.info("Imyvm Essential initialized");
/*		LOGGER.info("_____  __  __ __     ____      __ __  __                                ");
		LOGGER.info("|_   _||  \/  |\ \   / /\ \    / /|  \/  |                              ");
		LOGGER.info("| |  | \  / | \ \_/ /  \ \  / / | \  / |								 ");
		LOGGER.info("| |  | |\/| |  \   /    \ \/ /  | |\/| |								 ");
		LOGGER.info("_| |_ | |  | |   | |      \  /   | |  | |                               ");
		LOGGER.info("|_____||_|  |_|   |_|       \/    |_|  |_| 							 ");
		LOGGER.info("______   _____  _____  ______  _   _  _______  _____            _       ");
		LOGGER.info("|  ____| / ____|/ ____||  ____|| \ | ||__   __||_   _|    /\    | |     ");
		LOGGER.info("| |__   | (___ | (___  | |__   |  \| |   | |     | |     /  \   | |     ");
		LOGGER.info("|  __|   \___ \ \___ \ |  __|  | . ` |   | |     | |    / /\ \  | |     ");
		LOGGER.info("| |____  ____) |____) || |____ | |\  |   | |    _| |_  / ____ \ | |____ ");
		LOGGER.info("|______||_____/|_____/ |______||_| \_|   |_|   |_____|/_/    \_\|______|");
*/
	}

    public void registerEvents() {
        Function<PlayerEntity, ActionResult> onActivity = (player) -> {
            ((PlayerEntityMixinInterface) player).imyvm$updateActivity();
            return ActionResult.PASS;
        };

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> onActivity.apply(sender));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> onActivity.apply(player));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onActivity.apply(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> onActivity.apply(player));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            onActivity.apply(player);
            return TypedActionResult.pass(null);
        });
    }

    public void registerCommands() {
        new AfkCommand();
        new ItemShowCommand();
        new PlayTimeTrackCommand();
    }

    public void registerLazyTick() {
        LAZY_TICKER.register((server, tick) -> server.getPlayerManager().getPlayerList().forEach(player -> ((PlayerEntityMixinInterface) player).imyvm$lazyTick()));
        LAZY_TICKER.register(new PlayTimeTrackTask());
    }
}
