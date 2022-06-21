package com.imyvm.essential;


import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;

public class EssentialStatistics {
    // The total amount of time played (tracked in seconds)
    public static final Identifier PLAY_TIME_TRACK;

    // creeper rules
    public static GameRules.Key<GameRules.BooleanRule> creeperGriefing;
    public static GameRules.Key<GameRules.BooleanRule> creeperFire;

    // Force JVM to initialize the static fields
    public static void initialize() {}

    private static Identifier register(String id, StatFormatter formatter) {
        Identifier identifier = new Identifier(EssentialMod.MOD_ID, id);
        Registry.register(Registry.CUSTOM_STAT, id, identifier);
        Stats.CUSTOM.getOrCreateStat(identifier, formatter);
        return identifier;
    }

    static {
        PLAY_TIME_TRACK = register("play_time_track", StatFormatter.DEFAULT);
        creeperGriefing = GameRuleRegistry.register("creeperGriefing", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
        creeperFire = GameRuleRegistry.register("creeperFire", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    }
}
