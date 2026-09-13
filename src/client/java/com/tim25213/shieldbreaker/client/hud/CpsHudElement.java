package com.tim25213.shieldbreaker.client.hud;

import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class CpsHudElement implements HudElement {
	private static final int TEXT_COLOR = 0xFFFFFFFF;

	private final ShieldBreakerConfig config;
	private final ClickTracker tracker;

	public CpsHudElement(ShieldBreakerConfig config, ClickTracker tracker) {
		this.config = config;
		this.tracker = tracker;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || (!config.counterLeft && !config.counterRight)) {
			return;
		}

		String label;
		if (config.counterLeft && config.counterRight) {
			label = tracker.cps(true) + "|" + tracker.cps(false) + " cps";
		} else if (config.counterLeft) {
			label = tracker.cps(true) + " cps";
		} else {
			label = tracker.cps(false) + " cps";
		}

		Matrix3x2fStack pose = extractor.pose();
		pose.pushMatrix();
		pose.translate(config.counterX, config.counterY);
		pose.scale(config.counterScale());
		extractor.text(client.font, Component.literal(label), 0, 0, TEXT_COLOR);
		pose.popMatrix();
	}
}
