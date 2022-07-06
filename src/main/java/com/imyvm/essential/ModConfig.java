package com.imyvm.essential;

import com.imyvm.hoki.config.ConfigOption;
import com.imyvm.hoki.config.HokiConfig;
import com.imyvm.hoki.config.Option;
import com.typesafe.config.Config;

public class ModConfig extends HokiConfig {
    public static final String CONFIG_FILENAME = "imyvm_essential.conf";

    public ModConfig() {
        super(CONFIG_FILENAME);
    }

    @ConfigOption
    public final Option<String> LANGUAGE = new Option<>(
        "core.language",
        "en_us",
        "the display language of Essential mod",
        Config::getString
    );

    @ConfigOption
    public final Option<Long> AFK_AFTER_NO_ACTION = new Option<>(
        "afk.afk_after_no_action",
        600L * 1000,
        "how long (in milliseconds) after the player has no actions, he will be set to AFK status.",
        Config::getLong);

    @ConfigOption
    public final Option<Integer> USER_GROUP_REQUIRED_PTT = new Option<>(
        "ptt.user_group_required_ptt",
        (42 * 60 + 16) * 60,  // default: 42 hours 16 minutes
        "How long (in seconds) players will be set to the \"user\" group after playing",
        Config::getInt);

    @ConfigOption
    public final Option<Long> FLY_HOURLY_NOTICE_AT = new Option<>(
        "fly.hourly_notice_at",
        60 * 1000L,
        "How long (in milliseconds) the flying session will be over, the player will be notified",
        Config::getLong);

    @ConfigOption
    public final Option<Integer> FLY_HOURLY_PRICE = new Option<>(
        "fly.hourly",
        40 * 100,
        "The price (in cents) of flying which changes hourly",
        Config::getInt);

    @ConfigOption
    public final Option<Integer> FLY_INTRA_WORLD_PRICE = new Option<>(
        "fly.intra_world",
        80 * 100,
        "The price (in cents) of one-time flying in the same world",
        Config::getInt);

    @ConfigOption
    public final Option<Integer> FLY_INTER_WORLD_PRICE = new Option<>(
        "fly.inter_world",
        160 * 100,
        "The price (in cents) of one-time flying, available until leave the game",
        Config::getInt);

    @ConfigOption
    public final Option<Integer> FLY_LIFETIME_PRICE = new Option<>(
        "fly.lifetime",
        100000 * 100,
        "The price (in cents) of lifetime flying",
        Config::getInt);

    @ConfigOption
    public final Option<Long> FLY_FALL_PROTECT_DURATION = new Option<>(
        "fly.fall_protect",
        30 * 1000L,
        "The duration (in milliseconds) of falling damage protection after the flying ended",
        Config::getLong);
}
