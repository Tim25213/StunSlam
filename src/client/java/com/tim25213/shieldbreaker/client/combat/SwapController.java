package com.tim25213.shieldbreaker.client.combat;

import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import com.tim25213.shieldbreaker.client.input.KeyDriver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.random.RandomGenerator;

public class SwapController {
	private enum Phase {
		IDLE,
		ACTIVE,
		STUN_TAIL
	}

	private final ShieldBreakerConfig config;
	private final RandomGenerator jitter = RandomGenerator.getDefault();

	private Phase phase = Phase.IDLE;
	private int restoreSlot;
	private long nextClickAt;
	private long tailEndsAt;
	private boolean targetWasBlocking;

	public SwapController(ShieldBreakerConfig config) {
		this.config = config;
	}

	public boolean isClicking() {
		return phase != Phase.IDLE;
	}

	public void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || !config.enabled) {
			stop(client);
			return;
		}

		boolean airborne = !player.onGround() && !player.onClimbable() && !player.isInWater();
		boolean maceAvailable = carriesMace(player);

		if (phase == Phase.IDLE) {
			if (airborne && maceAvailable) {
				start(client, player);
			}
			return;
		}

		if (!airborne || !maceAvailable) {
			stop(client);
			return;
		}

		if (phase == Phase.ACTIVE && shieldJustDisabled(client)) {
			phase = Phase.STUN_TAIL;
			tailEndsAt = System.nanoTime() + (long) (config.stunDurationSeconds * 1_000_000_000L);
			scheduleClick(config.stunCpsMin, config.stunCpsMax);
		}

		if (phase == Phase.STUN_TAIL && System.nanoTime() >= tailEndsAt) {
			stop(client);
			return;
		}

		if (System.nanoTime() >= nextClickAt) {
			KeyDriver.click(client.options.keyAttack);
			if (phase == Phase.ACTIVE) {
				scheduleClick(config.cpsMin, config.cpsMax);
			} else {
				scheduleClick(config.stunCpsMin, config.stunCpsMax);
			}
		}
	}

	private void start(Minecraft client, LocalPlayer player) {
		int axeSlot = findAxeHotbarSlot(player);
		if (axeSlot < 0) {
			return;
		}

		restoreSlot = player.getInventory().getSelectedSlot();
		targetWasBlocking = false;
		phase = Phase.ACTIVE;
		KeyDriver.click(client.options.keyHotbarSlots[axeSlot]);
		scheduleClick(config.cpsMin, config.cpsMax);
	}

	private void stop(Minecraft client) {
		if (phase == Phase.IDLE) {
			return;
		}

		phase = Phase.IDLE;
		if (client.player != null) {
			KeyDriver.click(client.options.keyHotbarSlots[restoreSlot]);
		}
	}

	private boolean shieldJustDisabled(Minecraft client) {
		Entity target = client.crosshairPickEntity;
		if (!(target instanceof LivingEntity living)) {
			targetWasBlocking = false;
			return false;
		}

		boolean blocking = living.isBlocking();
		boolean wentDown = targetWasBlocking && !blocking;
		targetWasBlocking = blocking;
		return wentDown;
	}

	private void scheduleClick(double minCps, double maxCps) {
		double low = Math.min(minCps, maxCps);
		double high = Math.max(minCps, maxCps);
		double chosen = low + jitter.nextDouble() * (high - low);
		nextClickAt = System.nanoTime() + (long) (1_000_000_000L / chosen);
	}

	private static boolean carriesMace(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).getItem() == Items.MACE) {
				return true;
			}
		}
		return false;
	}

	private static int findAxeHotbarSlot(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && stack.typeHolder().is(ItemTags.AXES)) {
				return slot;
			}
		}
		return -1;
	}
}
