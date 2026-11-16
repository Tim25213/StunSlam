package com.tim25213.shieldbreaker.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.tim25213.shieldbreaker.client.ShieldBreakerClient;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyMappingClickMixin {
	@Inject(at = @At("HEAD"), method = "click")
	private static void shieldbreaker$countClick(InputConstants.Key key, CallbackInfo info) {
		ShieldBreakerClient.onKeyClicked(key);
	}
}
