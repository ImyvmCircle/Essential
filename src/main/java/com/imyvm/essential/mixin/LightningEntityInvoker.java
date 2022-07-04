package com.imyvm.essential.mixin;

import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LightningEntity.class)
public interface LightningEntityInvoker {
    @Invoker("spawnFire")
    public void invokespawnFire(int spreadAttempts);
}
