package com.tim25213.shieldbreaker.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.tim25213.shieldbreaker.client.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public final class KeyDriver {
	private KeyDriver() {
	}

	public static boolean isBoundTo(KeyMapping mapping, InputConstants.Key key) {
		return ((KeyMappingAccessor) (Object) mapping).shieldbreaker$boundKey().equals(key);
	}
}
