package com.imyvm.essential.mixin;

import com.imyvm.essential.data.PlayerData;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.imyvm.essential.EssentialMod.PLAYER_DATA_STORAGE;
import static com.imyvm.essential.Translator.tr;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void getAfkDisplayName(CallbackInfoReturnable<Component> ci) {
        Player player = (Player) (Object) this;
        PlayerEntityMixinInterface accessor = (PlayerEntityMixinInterface) player;
        Component name = player.getName();
        Component displayName = accessor.imyvm$isAwayFromKeyboard() ? tr("gui.tab_list.player_away", name) : name;
        ci.setReturnValue(displayName);
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void checkShouldKeepInventory(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.level().getGameRules().get(GameRules.KEEP_INVENTORY))
            return;

        if (!restoreAll && !oldPlayer.isSpectator()) {
            PlayerData data = PLAYER_DATA_STORAGE.getOrCreate(player.getUUID());
            if (data.shouldKeepInventoryAtRespawn()) {
                player.getInventory().replaceWith(oldPlayer.getInventory());
                data.setKeepInventoryAtRespawn(false);
            }
        }
    }
}