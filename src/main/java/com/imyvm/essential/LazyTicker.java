package com.imyvm.essential;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class LazyTicker {
    private final List<Task> tasks = new ArrayList<>();
    private final int period;
    private long tick = 0;

    public LazyTicker(int period) {
        this.period = period;

        ServerTickEvents.START_SERVER_TICK.register(this::tick);
    }

    public void tick(MinecraftServer server) {
        this.tick += 1;

        long currentTimestamp = System.currentTimeMillis();
        int phase = (int) (this.tick % this.period);
        for (int i = phase; i < tasks.size(); i += this.period)
            this.tasks.get(i).tick(server, this.tick, currentTimestamp);
    }

    public void register(LazyTickable target) {
        this.tasks.add(new Task(target));
    }

    private static class Task {
        private final LazyTickable target;
        private long lastTickAt;

        public Task(LazyTickable target) {
            this.target = target;
        }

        void tick(MinecraftServer server, long tick, long currentTimestamp) {
            if (this.lastTickAt != 0) {
                long duration = currentTimestamp - this.lastTickAt;
                this.target.lazyTick(server, tick, duration);
            }
            this.lastTickAt = currentTimestamp;
        }
    }

    @FunctionalInterface
    public interface LazyTickable {
        void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick);
    }
}
