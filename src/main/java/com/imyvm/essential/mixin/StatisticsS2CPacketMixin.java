package com.imyvm.essential.mixin;

import com.imyvm.essential.EssentialMod;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(StatisticsS2CPacket.class)
public class StatisticsS2CPacketMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeMap(Ljava/util/Map;Lnet/minecraft/network/PacketByteBuf$PacketWriter;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"), index = 0)
    private Map<Stat<?>, Integer> ignoreCustomStatistics(Map<Stat<?>, Integer> map) {
        return map.entrySet().stream().filter(entry -> {
            if (entry.getKey().getValue() instanceof Identifier identifier)
                return !identifier.getNamespace().equals(EssentialMod.MOD_ID);
            return true;
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
    }
}
