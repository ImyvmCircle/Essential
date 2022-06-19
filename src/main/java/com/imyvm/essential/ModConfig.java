package com.imyvm.essential;

import com.typesafe.config.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ModConfig {
    public final String CONFIG_FILENAME = "imyvm_essential.conf";

    private boolean isConfigOutdated = false;

    private long afkAfterNoAction;
    private long teleportTimeout;
    private long teleportWait;

    public ModConfig() throws IOException {
        File file = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILENAME).toFile();
        Config config = ConfigFactory.parseFile(file);

        ConfigWrapper wrapper = new ConfigWrapper(config);
        wrapper.slope("afk", this::loadAfkConfig);
        wrapper.slope("teleport", this::loadTeleportConfig);

        if (isConfigOutdated) {
            ConfigRenderOptions options = ConfigRenderOptions
                .defaults()
                .setJson(false)
                .setOriginComments(false);

            try (PrintWriter writer = new PrintWriter(file)) {
                writer.write(wrapper.config.root().render(options));
            }
        }
    }

    private void loadAfkConfig(ConfigWrapper node) {
        this.afkAfterNoAction = (Long) node.get(
            "afk_after_no_action",
            "how long (in milliseconds) after the player has no actions, he will be set to AFK status.",
            600 * 1000,
            Config::getLong);
    }

    private void loadTeleportConfig(ConfigWrapper node) {
        this.teleportTimeout = (Long) node.get(
            "timeout",
            "the timeout (in milliseconds) of the teleport request",
            30 * 1000,
            Config::getLong);

        this.teleportWait = (Long) node.get(
            "wait",
            "how long (in milliseconds) the player needs to wait before teleportation.",
            2500,
            Config::getLong);
    }

    public long getAfkAfterNoAction() {
        return this.afkAfterNoAction;
    }

    public long getTeleportTimeout() {
        return teleportTimeout;
    }

    public long getTeleportWait() {
        return teleportWait;
    }

    private class ConfigWrapper {
        Config config;
        String slope;

        ConfigWrapper(Config config, String slope) {
            this.config = config;
            this.slope = slope;
        }

        ConfigWrapper(Config config) {
            this(config, "");
        }

        <T> T get(String key, String comments, T def, BiFunction<Config, String, T> getter) {
            key = slope + key;
            try {
                config.getValue(key);
            } catch (ConfigException.Missing e) {
                isConfigOutdated = true;
                ConfigOrigin origin = ConfigOriginFactory.newSimple().withComments(Arrays.asList(comments.split("\n")));
                ConfigValue value = ConfigValueFactory.fromAnyRef(def).withOrigin(origin);
                config = config.withValue(key, value);
            }
            return getter.apply(config, key);
        }

        void slope(String key, Consumer<ConfigWrapper> consumer) {
            ConfigWrapper wrapper = new ConfigWrapper(config, slope + key + ".");
            consumer.accept(wrapper);
            config = wrapper.config;
        }
    }
}
