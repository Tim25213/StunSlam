package com.tim25213.shieldbreaker.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tim25213.shieldbreaker.ShieldBreaker;
import com.tim25213.shieldbreaker.client.command.ShieldBreakerCommand;
import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import com.tim25213.shieldbreaker.client.hud.ClickTracker;
import com.tim25213.shieldbreaker.client.hud.CpsHudElement;
import com.tim25213.shieldbreaker.client.input.KeyDriver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class ShieldBreakerClient implements ClientModInitializer {
	private static final ShieldBreakerConfig CONFIG = ShieldBreakerConfig.load();
	private static final ClickTracker TRACKER = new ClickTracker();

	public static ShieldBreakerConfig config() {
		return CONFIG;
	}

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> ShieldBreakerCommand.register(dispatcher, context, CONFIG));
		HudElementRegistry.addLast(ShieldBreaker.id("cps_counter"), new CpsHudElement(CONFIG, TRACKER));

		ShieldBreaker.LOGGER.info("Ready in {} mode", CONFIG.mode.name().toLowerCase());
	}

	public static void onKeyClicked(InputConstants.Key key) {
		Options options = Minecraft.getInstance().options;
		if (options == null) {
			return;
		}

		boolean attack = KeyDriver.isBoundTo(options.keyAttack, key);
		if (!attack && !KeyDriver.isBoundTo(options.keyUse, key)) {
			return;
		}

		TRACKER.record(attack);
	}
}
