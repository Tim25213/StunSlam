package com.tim25213.shieldbreaker.client.mixin;

import com.tim25213.shieldbreaker.client.combat.SwapHandler;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class GameModeAttackMixin {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void shieldbreaker$swapForAttack(Player player, Entity target, CallbackInfo info) {
		if (SwapHandler.interceptAttack((MultiPlayerGameMode) (Object) this, player, target)) {
			info.cancel();
		}
	}
}
