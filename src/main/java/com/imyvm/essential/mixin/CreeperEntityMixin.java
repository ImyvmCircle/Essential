package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {
    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"))
    private Explosion redirectExplosion(World world, Entity entity, double x, double y, double z, float power, Explosion.DestructionType destructionType) {
        Explosion.DestructionType type = world.getGameRules().getBoolean(EssentialGameRules.DO_CREEPER_GRIEFING) ? destructionType : Explosion.DestructionType.NONE;
        return world.createExplosion(entity, x, y, z, power, type);
    }
}