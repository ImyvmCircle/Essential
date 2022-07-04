package com.imyvm.essential;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class LazyTicker {
    private final List<LazyTickable> tasks = new ArrayList<>();
    private final int period;
    private long tick = 0;

    public LazyTicker(int period) {
        this.period = period;

        ServerTickEvents.START_SERVER_TICK.register(this::tick);
    }

    public void tick(MinecraftServer server) {
        this.tick += 1;

        int phase = (int) (this.tick % this.period);
        for (int i = phase; i < tasks.size(); i += this.period)
            this.tasks.get(i).lazyTick(server, this.tick);
    }

    public void register(LazyTickable task) {
        this.tasks.add(task);
    }

    @FunctionalInterface
    public interface LazyTickable {
        void lazyTick(MinecraftServer server, long tick);
    }
}
