package com.imyvm.essential.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import com.imyvm.essential.EssentialStatistics;

@Mixin(CreeperEntity.class)
public class CreeperMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"),
            method = "explode")
    private Explosion redirectExplosion(World world, Entity entity, double x, double y, double z, float power, Explosion.DestructionType destructionType) {
        GameRules gameRules = world.getGameRules();
        return world.createExplosion(
                entity,
                x,
                y,
                z,
                power,
                gameRules.getBoolean(EssentialStatistics.creeperGriefing) ? destructionType : Explosion.DestructionType.NONE);
    }
}
