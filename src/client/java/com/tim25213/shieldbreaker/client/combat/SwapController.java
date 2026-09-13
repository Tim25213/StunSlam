package com.tim25213.shieldbreaker.client.combat;

import com.tim25213.shieldbreaker.client.config.Mode;
import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import com.tim25213.shieldbreaker.client.input.KeyDriver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.random.RandomGenerator;

public class SwapController {
	private enum Phase {
		IDLE,
		SPEAR,
		AXE,
		TAIL,
		SWORD,
		CHARGE,
		MACE
	}

	private static final float STRONG_ATTACK_SCALE = 0.9f;
	private static final double SPEAR_MIN_DISTANCE = 3.0;

	private final ShieldBreakerConfig config;
	private final RandomGenerator jitter = RandomGenerator.getDefault();

	private Phase phase = Phase.IDLE;
	private int restoreSlot;
	private long nextActionAt;
	private long tailEndsAt;
	private boolean targetWasBlocking;

	public SwapController(ShieldBreakerConfig config) {
		this.config = config;
	}

	public void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || !config.enabled) {
			stop(client);
			return;
		}

		boolean airborne = !player.onGround() && !player.onClimbable() && !player.isInWater();
		boolean falling = player.getDeltaMovement().y < 0.0;
		boolean maceAvailable = carriesMace(player);
		LivingEntity target = aimedTarget(client, player);

		if (phase == Phase.IDLE) {
			if (airborne && falling && maceAvailable && target != null) {
				start(client, player, target);
			}
			return;
		}

		if (!airborne || !maceAvailable) {
			stop(client);
			return;
		}

		if (target == null) {
			return;
		}

		switch (phase) {
			case SPEAR -> { if (due()) enterAxe(client, player); }
			case AXE -> tickAxe(client, player, target);
			case TAIL -> tickTail(client);
			case SWORD -> { if (due()) afterSword(client, player); }
			case CHARGE -> { if (player.getAttackStrengthScale(0.5f) > STRONG_ATTACK_SCALE) enterMace(client, player); }
			case MACE -> { if (due()) stop(client); }
			default -> {
			}
		}
	}

	private void start(Minecraft client, LocalPlayer player, LivingEntity target) {
		restoreSlot = player.getInventory().getSelectedSlot();
		targetWasBlocking = false;

		int spearSlot = findHotbarSlot(player, ItemTags.SPEARS);
		if (config.mode == Mode.AUTOMATIC && config.spearEnabled
				&& spearSlot >= 0 && player.distanceTo(target) > SPEAR_MIN_DISTANCE) {
			phase = Phase.SPEAR;
			swapAttack(client, spearSlot);
			schedule(config.cpsMin, config.cpsMax);
			return;
		}

		enterAxe(client, player);
	}

	private void enterAxe(Minecraft client, LocalPlayer player) {
		int axeSlot = findHotbarSlot(player, ItemTags.AXES);
		if (axeSlot < 0) {
			stop(client);
			return;
		}

		phase = Phase.AXE;
		swapAttack(client, axeSlot);
		schedule(config.cpsMin, config.cpsMax);
	}

	private void tickAxe(Minecraft client, LocalPlayer player, LivingEntity target) {
		if (shieldJustDisabled(target)) {
			if (config.mode == Mode.AUTOMATIC) {
				enterSword(client, player);
			} else if (config.stunDurationSeconds <= 0.0) {
				stop(client);
			} else {
				phase = Phase.TAIL;
				tailEndsAt = System.nanoTime() + (long) (config.stunDurationSeconds * 1_000_000_000L);
				schedule(config.stunCpsMin, config.stunCpsMax);
			}
			return;
		}

		if (due()) {
			KeyDriver.click(client.options.keyAttack);
			schedule(config.cpsMin, config.cpsMax);
		}
	}

	private void tickTail(Minecraft client) {
		if (System.nanoTime() >= tailEndsAt) {
			stop(client);
			return;
		}

		if (due()) {
			KeyDriver.click(client.options.keyAttack);
			schedule(config.stunCpsMin, config.stunCpsMax);
		}
	}

	private void enterSword(Minecraft client, LocalPlayer player) {
		int swordSlot = findHotbarSlot(player, ItemTags.SWORDS);
		if (swordSlot < 0) {
			enterMace(client, player);
			return;
		}

		phase = Phase.SWORD;
		swapAttack(client, swordSlot);
		schedule(config.cpsMin, config.cpsMax);
	}

	private void afterSword(Minecraft client, LocalPlayer player) {
		if (config.chargeBeforeMace) {
			phase = Phase.CHARGE;
		} else {
			enterMace(client, player);
		}
	}

	private void enterMace(Minecraft client, LocalPlayer player) {
		int maceSlot = findMaceHotbarSlot(player);
		if (maceSlot < 0) {
			stop(client);
			return;
		}

		phase = Phase.MACE;
		swapAttack(client, maceSlot);
		schedule(config.cpsMin, config.cpsMax);
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

	private void swapAttack(Minecraft client, int slot) {
		KeyDriver.click(client.options.keyHotbarSlots[slot]);
		KeyDriver.click(client.options.keyAttack);
	}

	private LivingEntity aimedTarget(Minecraft client, LocalPlayer player) {
		Entity aimed = client.crosshairPickEntity;
		if (aimed instanceof LivingEntity living && player.distanceTo(living) <= config.triggerDistance) {
			return living;
		}
		return null;
	}

	private boolean shieldJustDisabled(LivingEntity target) {
		boolean blocking = target.isBlocking();
		boolean wentDown = targetWasBlocking && !blocking;
		targetWasBlocking = blocking;
		return wentDown;
	}

	private boolean due() {
		return System.nanoTime() >= nextActionAt;
	}

	private void schedule(double minCps, double maxCps) {
		double low = Math.min(minCps, maxCps);
		double high = Math.max(minCps, maxCps);
		double chosen = low + jitter.nextDouble() * (high - low);
		nextActionAt = System.nanoTime() + (long) (1_000_000_000L / chosen);
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

	private static int findMaceHotbarSlot(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (inventory.getItem(slot).getItem() == Items.MACE) {
				return slot;
			}
		}
		return -1;
	}

	private static int findHotbarSlot(LocalPlayer player, TagKey<Item> tag) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && stack.typeHolder().is(tag)) {
				return slot;
			}
		}
		return -1;
	}
}
