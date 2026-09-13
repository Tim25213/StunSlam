package com.tim25213.shieldbreaker.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.tim25213.shieldbreaker.client.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public final class KeyDriver {
	private static boolean driving;

	private KeyDriver() {
	}

	public static void click(KeyMapping mapping) {
		InputConstants.Key bound = ((KeyMappingAccessor) (Object) mapping).shieldbreaker$boundKey();
		if (bound.equals(InputConstants.UNKNOWN)) {
			return;
		}

		driving = true;
		try {
			KeyMapping.click(bound);
		} finally {
			driving = false;
		}
	}

	public static boolean isDriving() {
		return driving;
	}

	public static boolean isBoundTo(KeyMapping mapping, InputConstants.Key key) {
		return ((KeyMappingAccessor) (Object) mapping).shieldbreaker$boundKey().equals(key);
	}
}
