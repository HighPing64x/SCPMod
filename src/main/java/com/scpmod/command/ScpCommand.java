package com.scpmod.command;

import com.scpmod.config.AnomalyConfig;
import com.scpmod.content.ModBlocks;
import com.scpmod.content.ModItems;
import com.scpmod.item.ScpDocumentItem;
import com.scpmod.config.EventConfig;
import com.scpmod.config.ScpModConfig;
import com.scpmod.event.AnomalyController;
import com.scpmod.event.BreachEventManager;
import com.scpmod.event.ScpModStats;
import com.scpmod.scp.ScpRegistry;
import com.scpmod.block.entity.ContainmentUnitBlockEntity;
import com.scpmod.block.entity.TerminalBlockEntity;
import com.scpmod.structure.StationBlueprint;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

/** /scp debug and status commands (D20). */
public final class ScpCommand {
	private ScpCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static BlockPos lookedAtBlock(ServerPlayer player) {
		HitResult hit = player.pick(6.0D, 0.0F, false);
		return hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : null;
	}

	/** 定位最近的 SCP-006 不老泉（只搜索已加载区块内的液体方块）。 */
	private static int giveItem(CommandContext<CommandSourceStack> context, ItemStack stack) {
		try {
			ServerPlayer player = context.getSource().getPlayerOrException();
			player.getInventory().placeItemBackInInventory(stack);
			context.getSource().sendSuccess(() -> Component.translatable("command.scpmod.give",
					stack.getCount(), stack.getHoverName()), true);
			return 1;
		} catch (Exception exception) {
			context.getSource().sendFailure(Component.translatable("command.scpmod.give.no_player"));
			return 0;
		}
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("scp")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("status").executes(context -> {
					CommandSourceStack source = context.getSource();
					source.sendSuccess(() -> Component.translatable("command.scpmod.status",
							BreachEventManager.phase().name(),
							BreachEventManager.ticksRemaining(),
							ScpRegistry.size(),
							ScpModConfig.get().breachEventsEnabled ? "on" : "off"), false);
					return 1;
				}))
				.then(Commands.literal("reload").executes(context -> {
					ScpModConfig.reload();
					EventConfig.load();
					AnomalyConfig.load();
					context.getSource().sendSuccess(() -> Component.translatable("command.scpmod.reloaded"), true);
					return 1;
				}))
				.then(Commands.literal("document")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							ScpDocumentItem.recordAll(player);
							return 1;
						}))
				.then(Commands.literal("give")
						.then(Commands.literal("scp_500")
								.executes(context -> giveItem(context, new ItemStack(ModItems.SCP_500, 1)))
								.then(Commands.argument("count", IntegerArgumentType.integer(1, 59))
										.executes(context -> giveItem(context, new ItemStack(ModItems.SCP_500,
												IntegerArgumentType.getInteger(context, "count"))))))
						.then(Commands.literal("scp_005")
								.executes(context -> giveItem(context, new ItemStack(ModItems.SCP_005, 1)))))
				.then(Commands.literal("look").executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					HitResult hit = player.pick(6.0D, 0.0F, false);
					if (hit instanceof BlockHitResult blockHit) {
						CommandSourceStack source = context.getSource();
						source.sendSuccess(() -> Component.translatable("command.scpmod.look",
								blockHit.getBlockPos().toShortString()), false);
						return 1;
					}
					context.getSource().sendFailure(Component.translatable("command.scpmod.look.none"));
					return 0;
				}))
				.then(Commands.literal("station")
						.then(Commands.literal("build").executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							ServerLevel level = context.getSource().getLevel();
							BlockPos origin = player.blockPosition().below();
							StationBlueprint.Site site = StationBlueprint.generate(level.getRandom());
							for (StationBlueprint.Op op : site.ops()) {
								level.setBlock(origin.offset(op.x(), op.y(), op.z()), op.state(), 3);
							}
							for (StationBlueprint.Unit unit : site.units()) {
								BlockPos pos = origin.offset(unit.x(), unit.y(), unit.z());
								level.setBlock(pos, StationBlueprint.containmentUnit(), 3);
								if (level.getBlockEntity(pos) instanceof ContainmentUnitBlockEntity be) {
									be.setContainedScp(unit.scpId());
								}
							}
							for (StationBlueprint.Terminal terminal : site.terminals()) {
								BlockPos pos = origin.offset(terminal.x(), terminal.y(), terminal.z());
								level.setBlock(pos, StationBlueprint.terminal(), 3);
								if (level.getBlockEntity(pos) instanceof TerminalBlockEntity be) {
									be.toggleArmed();
								}
							}
							context.getSource().sendSuccess(() -> Component.translatable("command.scpmod.station.built", origin.toShortString()), true);
							return 1;
						})))
				.then(Commands.literal("unit")
						.then(Commands.literal("set")
								.then(Commands.argument("scpId", com.mojang.brigadier.arguments.StringArgumentType.word())
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											String scpId = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "scpId");
											BlockPos pos = lookedAtBlock(player);
											if (pos == null) {
												context.getSource().sendFailure(Component.translatable("command.scpmod.look.none"));
												return 0;
											}
											if (context.getSource().getLevel().getBlockEntity(pos) instanceof com.scpmod.block.entity.ContainmentUnitBlockEntity unit) {
												unit.setContainedScp(scpId.contains(":") ? scpId : "scpmod:" + scpId);
												context.getSource().sendSuccess(() -> Component.literal("unit " + pos.toShortString() + " now holds " + unit.containedScp()), true);
												return 1;
											}
											context.getSource().sendFailure(Component.literal("not a containment unit"));
											return 0;
										})))
						.then(Commands.literal("empty").executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							BlockPos pos = lookedAtBlock(player);
							if (pos == null) {
								context.getSource().sendFailure(Component.translatable("command.scpmod.look.none"));
								return 0;
							}
							if (context.getSource().getLevel().getBlockEntity(pos) instanceof com.scpmod.block.entity.ContainmentUnitBlockEntity unit) {
								unit.setContainedScp("");
								context.getSource().sendSuccess(() -> Component.literal("unit " + pos.toShortString() + " emptied"), true);
								return 1;
							}
							context.getSource().sendFailure(Component.literal("not a containment unit"));
							return 0;
						})))
				.then(Commands.literal("anomaly")
						.then(Commands.literal("clear").executes(context -> {
							AnomalyController.despawn();
							context.getSource().sendSuccess(() -> Component.translatable("command.scpmod.anomaly.cleared"), true);
							return 1;
						})))
				.then(Commands.literal("event")
						.then(Commands.literal("start").executes(context -> {
							ServerLevel level = context.getSource().getLevel();
							ServerPlayer player = context.getSource().getPlayerOrException();
							String scpId = ScpRegistry.all().stream().findFirst().map(d -> d.id().toString()).orElse("");
							boolean started = BreachEventManager.start(level, player.blockPosition(), scpId);
							context.getSource().sendSuccess(() -> Component.translatable(started ? "command.scpmod.event.started" : "command.scpmod.event.busy"), true);
							return started ? 1 : 0;
						}))
						.then(Commands.literal("stop").executes(context -> {
							boolean stopped = BreachEventManager.stop();
							context.getSource().sendSuccess(() -> Component.translatable(stopped ? "command.scpmod.event.stopped" : "command.scpmod.event.idle"), true);
							return stopped ? 1 : 0;
						})))
				.then(Commands.literal("stats").executes(context -> {
					context.getSource().sendSuccess(() -> Component.literal("[scpmod] " + ScpModStats.summary()), false);
					return 1;
				}))
				.then(Commands.literal("scps").executes(context -> {
					String list = ScpRegistry.all().stream().map(d -> d.id().toString()).reduce((a, b) -> a + ", " + b).orElse("-");
					context.getSource().sendSuccess(() -> Component.literal("registered: " + list), false);
					return 1;
				})));
	}
}
