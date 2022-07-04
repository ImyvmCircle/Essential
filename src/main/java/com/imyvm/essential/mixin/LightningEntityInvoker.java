package com.imyvm.essential.mixin;

import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LightningEntity.class)
public interface LightningEntityInvoker {
    @Invoker("spawnFire")
    void imyvm$spawnFire(int spreadAttempts);
}
