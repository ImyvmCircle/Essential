package com.imyvm.essential;

import com.imyvm.hoki.config.ConfigOption;
import com.imyvm.hoki.config.HokiConfig;
import com.imyvm.hoki.config.Option;
import com.typesafe.config.Config;

@SuppressWarnings({"AbbreviationAsWordInName", "DeclarationOrder", "LineLength", "MemberName"})
public class ModConfig extends HokiConfig {
    public static final String CONFIG_FILENAME = "imyvm_essential.conf";

    public ModConfig() {
        super(CONFIG_FILENAME);
    }

    @ConfigOption public final Option<String> LANGUAGE = new Option<>("core.language", "en_us", "the display language of Essential mod", Config::getString);

    @ConfigOption public final Option<Long> AFK_AFTER_NO_ACTION = new Option<>("afk.afk_after_no_action", 600L * 1000, "how long (in milliseconds) after the player has no actions, he will be set to AFK status.", Config::getLong);

    @ConfigOption public final Option<Integer> DEATH_PROTECT_BASE_PRICE = new Option<>("death_protect.base_price", 40 * 100, "The base price (in cents) of one death-protect level", Config::getInt);

    @ConfigOption public final Option<Long> PTT_ACQUIRE_TIME_LIMIT = new Option<>("ptt.time_limit", 60 * 1000L /* 1 m */, "The time limit (in milliseconds) for player to acquire the bonus", Config::getLong);
    @ConfigOption public final Option<Long> PTT_CONTINUOUS_REQUIRED = new Option<>("ptt.required.continuous", (10 * 60 - 30) * 1000L /* 9 m 30 s */, "How long (in milliseconds) player need to be online to acquire 10-minute (i.e. continuous) bonus", Config::getLong);
    @ConfigOption public final Option<Long> PTT_DAY_REQUIRED = new Option<>("ptt.required.day", 3600 * 1000L /* 1 h */, "How long (in milliseconds) player need to be online to acquire daily bonus", Config::getLong);
    @ConfigOption public final Option<Long> PTT_WEEK_REQUIRED = new Option<>("ptt.required.week", (16 * 60 + 40) * 60 * 1000L /* 16 h 40 m */, "How long (in milliseconds) player need to be online to acquire weekly bonus", Config::getLong);
    @ConfigOption public final Option<Long> PTT_MONTH_REQUIRED = new Option<>("ptt.required.month", 70 * 3600 * 1000L /* 70 h */, "How long (in milliseconds) player need to be online to acquire monthly bonus", Config::getLong);
    @ConfigOption public final Option<Long> PTT_YEAR_REQUIRED = new Option<>("ptt.required.year", 850 * 3600 * 1000L /* 850 h */, "How long (in milliseconds) player need to be online to acquire yearly bonus", Config::getLong);
    @ConfigOption public final Option<Integer> PTT_CONTINUOUS_BONUS = new Option<>("ptt.bonus.continuous", 2 * 100, "How much money player can get from 10-minute (continuous) bonus", Config::getInt);
    @ConfigOption public final Option<Integer> PTT_DAY_BONUS = new Option<>("ptt.bonus.day", 45 * 100, "How much money player can get from daily bonus", Config::getInt);
    @ConfigOption public final Option<Integer> PTT_WEEK_BONUS = new Option<>("ptt.bonus.week", 350 * 100, "How much money player can get from weekly bonus", Config::getInt);
    @ConfigOption public final Option<Integer> PTT_MONTH_BONUS = new Option<>("ptt.bonus.month", 1000 * 100, "How much money player can get from monthly bonus", Config::getInt);
    @ConfigOption public final Option<Integer> PTT_YEAR_BONUS = new Option<>("ptt.bonus.year", 12000 * 100, "How much money player can get from yearly bonus", Config::getInt);
    @ConfigOption public final Option<Double> PTT_REISSUE_RATIO = new Option<>("ptt.bonus.reissue.ratio", 0.25, "Reissue ratio after online reward expires", Config::getDouble);

    @ConfigOption public final Option<Long> FLY_HOURLY_NOTICE_AT = new Option<>("fly.hourly_notice_at", 60 * 1000L, "How long (in milliseconds) the flying session will be over, the player will be notified", Config::getLong);
    @ConfigOption public final Option<Integer> FLY_HOURLY_PRICE = new Option<>("fly.price.hourly", 40 * 100, "The price (in cents) of flying which changes hourly", Config::getInt);
    @ConfigOption public final Option<Integer> FLY_INTRA_WORLD_PRICE = new Option<>("fly.price.intra_world", 80 * 100, "The price (in cents) of one-time flying in the same world", Config::getInt);
    @ConfigOption public final Option<Integer> FLY_INTER_WORLD_PRICE = new Option<>("fly.price.inter_world", 160 * 100, "The price (in cents) of one-time flying, available until leave the game", Config::getInt);
    @ConfigOption public final Option<Integer> FLY_LIFETIME_PRICE = new Option<>("fly.price.lifetime", 100000 * 100, "The price (in cents) of lifetime flying", Config::getInt);
    @ConfigOption public final Option<Long> FLY_FALL_PROTECT_DURATION = new Option<>("fly.fall_protect", 30 * 1000L, "The duration (in milliseconds) of falling damage protection after the flying ended", Config::getLong);
    @ConfigOption public final Option<String> FLY_HOURLY_ICON = new Option<>("fly.icon.hourly", "minecraft:emerald", "The icon of hourly flying, represented by Minecraft item identifier", Config::getString);
    @ConfigOption public final Option<String> FLY_INTRA_WORLD_ICON = new Option<>("fly.icon.intra_world", "minecraft:iron_ingot", "The icon of intra-world flying, represented by Minecraft item identifier", Config::getString);
    @ConfigOption public final Option<String> FLY_INTER_WORLD_ICON = new Option<>("fly.icon.inter_world", "minecraft:gold_ingot", "The icon of inter-world flying, represented by Minecraft item identifier", Config::getString);
    @ConfigOption public final Option<String> FLY_LIFETIME_ICON = new Option<>("fly.icon.lifetime", "minecraft:diamond", "The icon of lifetime flying, represented by Minecraft item identifier", Config::getString);
    @ConfigOption public final Option<String> FLY_CANCEL_ICON = new Option<>("fly.icon.cancel", "minecraft:barrier", "The icon of cancelling, represented by Minecraft item identifier", Config::getString);

    @ConfigOption public final Option<Boolean> DO_CREEPER_GRIEFING = new Option<>("tweak.doCreeperGriefing", true, "If disabled, creeper explosions do not break blocks", Config::getBoolean);
    @ConfigOption public final Option<Boolean> DO_LIGHTNING_SPAWN_FIRE = new Option<>("tweak.doLightningSpawnFire", true, "If disabled, lightnings do not spawn fire", Config::getBoolean);
    @ConfigOption public final Option<Boolean> DO_LAVA_SPREAD_FIRE = new Option<>("tweak.doLavaSpreadFire", true, "If disabled, lava does not spread fire", Config::getBoolean);

    @ConfigOption public final Option<Boolean> FIX_ALLAY_DUPLICATE_ITEM = new Option<>("fix.allay_duplicate_item", true, "Whether to fix allay duplicating item when it goes through the nether portal", Config::getBoolean);
}
