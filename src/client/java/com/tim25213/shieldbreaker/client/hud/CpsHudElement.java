package com.tim25213.shieldbreaker.client.hud;

import com.tim25213.shieldbreaker.client.combat.SwapController;
import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class CpsHudElement implements HudElement {
	private static final int MANUAL_COLOR = 0xFF55FF55;
	private static final int AUTOMATED_COLOR = 0xFFFFAA00;
	private static final int LINE_HEIGHT = 10;

	private final ShieldBreakerConfig config;
	private final SwapController controller;
	private final ClickTracker tracker;

	public CpsHudElement(ShieldBreakerConfig config, SwapController controller, ClickTracker tracker) {
		this.config = config;
		this.controller = controller;
		this.tracker = tracker;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || (!config.counterLeft && !config.counterRight)) {
			return;
		}

		boolean automated = controller.isClicking();
		int color = automated ? AUTOMATED_COLOR : MANUAL_COLOR;
		String source = automated ? "SB" : "YOU";

		Matrix3x2fStack pose = extractor.pose();
		pose.pushMatrix();
		pose.translate(config.counterX, config.counterY);
		pose.scale(config.counterScale());

		int line = 0;
		if (config.counterLeft) {
			double cps = tracker.cps(automated, true);
			extractor.text(client.font, Component.literal("%s  L %.1f".formatted(source, cps)), 0, line * LINE_HEIGHT, color);
			line++;
		}
		if (config.counterRight) {
			double cps = tracker.cps(automated, false);
			extractor.text(client.font, Component.literal("%s  R %.1f".formatted(source, cps)), 0, line * LINE_HEIGHT, color);
		}

		pose.popMatrix();
	}
}
