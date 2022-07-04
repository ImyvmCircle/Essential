package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialGameRules;
import net.minecraft.entity.LightningEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LightningEntity;spawnFire(I)V"))
    private void redirectSpawnFire (LightningEntity Entity, int spreadAttempts) {
        if (Entity.world.getGameRules().getBoolean(EssentialGameRules.DO_LIGHTNING_SPAWNFIRE)) {
            ((LightningEntityInvoker) Entity).invokespawnFire(spreadAttempts);
        } else {
            return;
        }
    }
}

