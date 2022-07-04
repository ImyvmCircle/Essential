package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialGameRules;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {
    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private Explosion.DestructionType modifyDestructionType(Explosion.DestructionType destructionType) {
        CreeperEntity self = (CreeperEntity) (Object) this;
        return self.getWorld().getGameRules().getBoolean(EssentialGameRules.DO_CREEPER_GRIEFING) ? destructionType : Explosion.DestructionType.NONE;
    }
}