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
}
