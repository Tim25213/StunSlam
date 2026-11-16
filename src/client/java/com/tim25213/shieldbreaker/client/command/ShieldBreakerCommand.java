package com.tim25213.shieldbreaker.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tim25213.shieldbreaker.client.config.Mode;
import com.tim25213.shieldbreaker.client.config.ShieldBreakerConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public final class ShieldBreakerCommand {
	private ShieldBreakerCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context, ShieldBreakerConfig config) {
		dispatcher.register(ClientCommands.literal("shieldbreaker")
			.then(ClientCommands.literal("toggle")
				.executes(ctx -> {
					config.enabled = !config.enabled;
					return apply(ctx, config, "ShieldBreaker " + (config.enabled ? "enabled" : "disabled"));
				}))
			.then(ClientCommands.literal("mode")
				.then(ClientCommands.literal("manual").executes(ctx -> {
					config.mode = Mode.MANUAL;
					return apply(ctx, config, "Mode set to manual: axe swap on blocking targets only");
				}))
				.then(ClientCommands.literal("automatic").executes(ctx -> {
					config.mode = Mode.AUTOMATIC;
					return apply(ctx, config, "Mode set to automatic: axe on block, damage swap otherwise");
				})))
			.then(ClientCommands.literal("spear")
				.then(ClientCommands.literal("on").executes(ctx -> {
					config.spearEnabled = true;
					return apply(ctx, config, "Damage swap uses a spear");
				}))
				.then(ClientCommands.literal("off").executes(ctx -> {
					config.spearEnabled = false;
					return apply(ctx, config, "Damage swap uses the mace");
				})))
			.then(ClientCommands.literal("counter")
				.then(ClientCommands.literal("left")
					.then(ClientCommands.literal("on").executes(ctx -> {
						config.counterLeft = true;
						return apply(ctx, config, "Left-click counter shown");
					}))
					.then(ClientCommands.literal("off").executes(ctx -> {
						config.counterLeft = false;
						return apply(ctx, config, "Left-click counter hidden");
					})))
				.then(ClientCommands.literal("right")
					.then(ClientCommands.literal("on").executes(ctx -> {
						config.counterRight = true;
						return apply(ctx, config, "Right-click counter shown");
					}))
					.then(ClientCommands.literal("off").executes(ctx -> {
						config.counterRight = false;
						return apply(ctx, config, "Right-click counter hidden");
					})))
				.then(ClientCommands.literal("pos")
					.then(ClientCommands.argument("x", IntegerArgumentType.integer())
						.then(ClientCommands.argument("y", IntegerArgumentType.integer())
							.executes(ctx -> {
								config.counterX = IntegerArgumentType.getInteger(ctx, "x");
								config.counterY = IntegerArgumentType.getInteger(ctx, "y");
								return apply(ctx, config, "Counter moved to %d, %d".formatted(config.counterX, config.counterY));
							}))))
				.then(ClientCommands.literal("move")
					.then(nudge("up", 0, -1, config))
					.then(nudge("down", 0, 1, config))
					.then(nudge("left", -1, 0, config))
					.then(nudge("right", 1, 0, config)))
				.then(ClientCommands.literal("size")
					.then(ClientCommands.argument("scale", IntegerArgumentType.integer(1, 10))
						.executes(ctx -> {
							config.counterSize = IntegerArgumentType.getInteger(ctx, "scale");
							return apply(ctx, config, "Counter size set to " + config.counterSize);
						})))));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> nudge(String name, int stepX, int stepY, ShieldBreakerConfig config) {
		return ClientCommands.literal(name)
			.then(ClientCommands.argument("amount", IntegerArgumentType.integer(1))
				.executes(ctx -> {
					int amount = IntegerArgumentType.getInteger(ctx, "amount");
					config.counterX += stepX * amount;
					config.counterY += stepY * amount;
					return apply(ctx, config, "Counter moved to %d, %d".formatted(config.counterX, config.counterY));
				}));
	}

	private static int apply(CommandContext<FabricClientCommandSource> ctx, ShieldBreakerConfig config, String message) {
		config.save();
		ctx.getSource().sendFeedback(Component.literal(message));
		return Command.SINGLE_SUCCESS;
	}
}
