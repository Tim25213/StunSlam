package com.tim25213.shieldbreaker.client.combat;

import com.tim25213.shieldbreaker.client.ShieldBreakerClient;
import com.tim25213.shieldbreaker.client.config.Mode;
import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SwapHandler {
	private static boolean swapping;

	private SwapHandler() {
	}

	public static boolean interceptAttack(MultiPlayerGameMode gameMode, Player player, Entity target) {
		ShieldBreakerConfig config = ShieldBreakerClient.config();
		if (swapping || !config.enabled) {
			return false;
		}

		int originalSlot = player.getInventory().getSelectedSlot();
		int swapSlot = chooseSlot(config, player, target);
		if (swapSlot < 0 || swapSlot == originalSlot) {
			return false;
		}

		swapping = true;
		try {
			selectSlot(player, swapSlot);
			gameMode.attack(player, target);
			selectSlot(player, originalSlot);
		} finally {
			swapping = false;
		}
		return true;
	}

	private static int chooseSlot(ShieldBreakerConfig config, Player player, Entity target) {
		if (!(target instanceof LivingEntity living)) {
			return -1;
		}

		if (living.isBlocking()) {
			return findHotbarSlot(player, ItemTags.AXES);
		}

		if (config.mode == Mode.AUTOMATIC) {
			return config.spearEnabled ? findHotbarSlot(player, ItemTags.SPEARS) : findMaceHotbarSlot(player);
		}

		return -1;
	}

	private static void selectSlot(Player player, int slot) {
		player.getInventory().setSelectedSlot(slot);

		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			connection.send(new ServerboundSetCarriedItemPacket(slot));
		}
	}

	private static int findMaceHotbarSlot(Player player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (inventory.getItem(slot).getItem() == Items.MACE) {
				return slot;
			}
		}
		return -1;
	}

	private static int findHotbarSlot(Player player, TagKey<Item> tag) {
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
