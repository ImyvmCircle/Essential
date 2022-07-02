package com.imyvm.essential.mixin;

import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StatType.class)
public interface StatTypeAccessor<T> {
    @Accessor("stats")
    Map<T, Stat<T>> getStats();
}
