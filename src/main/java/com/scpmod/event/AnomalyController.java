package com.scpmod.event;

import com.scpmod.ScpMod;
import com.scpmod.config.AnomalyConfig;
import com.scpmod.content.ModEntities;
import com.scpmod.content.ModSounds;
import com.scpmod.entity.Scp173Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side behaviour of the released anomaly (SCP-173 prototype).
 *
 * <p>Source (SCP-173): "该物体在直接视线内无法移动", attacks by snapping the neck. Observed = the
 * anomaly is inside the player's field of view (configurable, default 70°) with an unobstructed
 * line; a very close anomaly in front of the player always counts as observed.
 *
 * <p>Targeting (L3 feedback): never target dead players, creative players or players still inside
 * their post-respawn protection window; the anomaly does not chase those either.
 */
public final class AnomalyController {
	private static final Map<UUID, PlayerState> PLAYER_STATE = new HashMap<>();
	private static final Map<UUID, Boolean> NEAR_STATE = new HashMap<>();

	private static Scp173Entity body;
	private static long lastAttackTick;
	private static List<BlockPos> path = new ArrayList<>();
	private static int pathIndex;
	private static long lastPathTick;
	private static long stallUntilTick;
	private static long lastAdoptScanTick;
	private static long nextScrapeTick;
	private static final java.util.Random RANDOM = new java.util.Random();

	private record PlayerState(long lastSeenTick, long spawnAtTick, boolean alive) {
	}

	private AnomalyController() {
	}

	public static boolean isActive() {
		return body != null && !body.isRemoved();
	}

	public static void spawn(ServerLevel level, BlockPos origin) {
		despawn();
		Scp173Entity entity = new Scp173Entity(ModEntities.SCP_173, level);
		entity.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
		entity.setInvulnerable(true);
		entity.setCustomName(Component.translatable("entity.scpmod.scp_173"));
		level.addFreshEntity(entity);
		body = entity;
		lastAttackTick = 0L;
		PLAYER_STATE.clear();
		NEAR_STATE.clear();
		path = new ArrayList<>();
		pathIndex = 0;
		ScpMod.LOGGER.info("[scpmod] anomaly body spawned at {}", origin);
	}

	/** 世界重载后接管已存在的 173 实体（静态引用在重进世界后会丢失）。 */
	public static void ensureAdopted(ServerLevel level) {
		if (isActive()) {
			return;
		}
		long now = level.getGameTime();
		if (now - lastAdoptScanTick < 20L) {
			return;
		}
		lastAdoptScanTick = now;
		java.util.List<Scp173Entity> found = level.getEntities(
				EntityTypeTest.forClass(Scp173Entity.class),
				AABB.ofSize(new Vec3(0.0D, 64.0D, 0.0D), 6.0E7D, 6.0E7D, 6.0E7D),
				entity -> !entity.isRemoved());
		if (!found.isEmpty()) {
			adopt(found.get(0));
		}
	}

	/** 把已有实体设为当前活动异常。 */
	public static void adopt(Scp173Entity entity) {
		if (entity == null || entity.isRemoved()) {
			return;
		}
		entity.setInvulnerable(true);
		entity.setNoGravity(false);
		body = entity;
		lastAttackTick = 0L;
		PLAYER_STATE.clear();
		NEAR_STATE.clear();
		path = new ArrayList<>();
		pathIndex = 0;
		stallUntilTick = 0L;
		ScpMod.LOGGER.info("[scpmod] adopted existing anomaly body at {}", entity.blockPosition());
	}

	public static void despawn() {
		if (body != null) {
			body.discard();
			body = null;
			PLAYER_STATE.clear();
			NEAR_STATE.clear();
			ScpMod.LOGGER.info("[scpmod] anomaly body removed");
		}
	}

	public static void tick() {
		if (body == null || body.isRemoved()) {
			body = null;
			return;
		}
		if (!(body.level() instanceof ServerLevel level)) {
			return;
		}
		AnomalyConfig cfg = AnomalyConfig.get();
		long now = level.getGameTime();
		updateAmbientMessages(level, cfg);

		// 生成出来且没有任何视线接触时：小幅度移动 + 刮石声（右下角字幕显示 SCP173：刮石）。
		boolean observed = isObservedByAnyone(level);
		if (!observed) {
			if (now >= nextScrapeTick) {
				nextScrapeTick = now + 100L + RANDOM.nextInt(100);
				level.playSound(null, body.blockPosition(), ModSounds.SCRAPE, SoundSource.AMBIENT, 1.0F, 0.7F);
			}
		}

		ServerPlayer target = null;
		double best = Double.MAX_VALUE;
		for (ServerPlayer player : level.players()) {
			PlayerState state = refreshPlayerState(player, now);
			if (!isTargetable(player, state, now)) {
				continue;
			}
			double distance = body.distanceTo(player);
			if (distance < best) {
				best = distance;
				target = player;
			}
		}
		if (target == null || best > cfg.viewDistance) {
			return;
		}

		if (observed) {
			// 被盯着的时候一动不动（也不转头）。
			return;
		}

		// 没被注视时才转向玩家。
		faceTarget(target);

		if (best > cfg.killRange && now >= stallUntilTick) {
			// Collision-aware pursuit: never phase through walls; take a local detour when blocked.
			Vec3 beforePos = body.position();
			moveTowards(level, target, now);
			double moved = body.position().distanceTo(beforePos);
			if (moved < 0.02D && !hasClearLine(level, body.getEyePosition(), target.getEyePosition())) {
				// 被墙挡住且无视线：原地等待，避免贴着墙来回抽搐。
				stallUntilTick = now + 20L;
			}
			if (now % 3L == 0L) {
				level.playSound(null, body.blockPosition(), SoundEvents.STONE_STEP, SoundSource.HOSTILE, 0.9F, 0.5F);
			}
		}

		if (body.distanceTo(target) <= cfg.killRange) {
			// 不允许隔墙击杀：攻击前必须满足实体间无遮挡的直线视线。
			if (!hasClearLine(level, body.getEyePosition(), target.getEyePosition())) {
				return;
			}
			if (now - lastAttackTick < Math.max(1, cfg.attackCooldownTicks)) {
				return;
			}
			lastAttackTick = now;
			// 击杀瞬间面向玩家。
			faceTarget(target);
			boolean hurt = target.hurtServer(level, neckSnapSource(level), cfg.neckSnapDamage);
			if (hurt) {
				// 击杀时用非常轻的石头敲击声，避免突兀的攻击音效。
				level.playSound(null, body.blockPosition(), SoundEvents.STONE_HIT, SoundSource.HOSTILE, 0.35F, 0.8F);
				ScpMod.LOGGER.info("[scpmod] anomaly snapped the neck of {}", target.getName().getString());
			}
		}
	}

	/** 玩家重生回调：从重生这一刻重新计算保护期。 */
	public static void noteRespawn(ServerPlayer player) {
		if (player == null || !(player.level() instanceof ServerLevel level)) {
			return;
		}
		long now = level.getGameTime();
		PLAYER_STATE.put(player.getUUID(), new PlayerState(now, now, true));
		path = new ArrayList<>();
		pathIndex = 0;
		stallUntilTick = 0L;
	}

	private static PlayerState refreshPlayerState(ServerPlayer player, long now) {
		PlayerState previous = PLAYER_STATE.get(player.getUUID());
		boolean alive = player.isAlive();
		// 之前没记录 / 刚死亡 / 刚重生 / 离开太久：都从"现在"重新开始计算重生保护。
		boolean fresh = previous == null || !alive || !previous.alive() || now - previous.lastSeenTick() > 40L;
		long spawnAt = fresh ? now : previous.spawnAtTick();
		PlayerState state = new PlayerState(now, spawnAt, alive);
		PLAYER_STATE.put(player.getUUID(), state);
		return state;
	}

	private static boolean isTargetable(ServerPlayer player, PlayerState state, long now) {
		AnomalyConfig cfg = AnomalyConfig.get();
		if (!player.isAlive() || player.isSpectator()) {
			return false;
		}
		if (cfg.ignoreCreativePlayers && player.isCreative()) {
			return false;
		}
		long protectionTicks = Math.max(0, cfg.respawnProtectionSeconds) * 20L;
		return now - state.spawnAtTick() >= protectionTicks;
	}

	/** Enter/leave subtitle: "something concrete is nearby" / "the air feels less oppressive". */
	private static void updateAmbientMessages(ServerLevel level, AnomalyConfig cfg) {
		if (!cfg.ambientMessagesEnabled) {
			return;
		}
		double range = Math.max(1.0D, cfg.ambientWarningRange);
		double leaveRange = range + Math.max(0.0D, cfg.ambientWarningHysteresis);
		for (ServerPlayer player : level.players()) {
			double distance = body.distanceTo(player);
			boolean wasNear = Boolean.TRUE.equals(NEAR_STATE.get(player.getUUID()));
			if (!wasNear && distance <= range) {
				NEAR_STATE.put(player.getUUID(), Boolean.TRUE);
				sendSubtitle(player, "message.scpmod.ambient.near");
			} else if (wasNear && distance > leaveRange) {
				NEAR_STATE.put(player.getUUID(), Boolean.FALSE);
				sendSubtitle(player, "message.scpmod.ambient.far");
			}
		}
	}

	/** Shows a subtitle-only cinematic line (empty title + subtitle + animation). */
	private static void sendSubtitle(ServerPlayer player, String translationKey) {
		player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
		player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(translationKey)));
		player.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
	}

	private static boolean isObservedByAnyone(ServerLevel level) {
		AnomalyConfig cfg = AnomalyConfig.get();
		double fov = Math.max(10.0D, Math.min(170.0D, cfg.observationFovDegrees));
		double cosLimit = Math.cos(Math.toRadians(fov * 0.5D));
		AABB box = body.getBoundingBox();
		// 采样实体包围盒的 8 个角 + 中心：只要任意一点在视线内且无遮挡，就算被观察到。
		double[][] offsets = {
				{ 0.5D, 0.5D, 0.5D },
				{ 0.0D, 0.0D, 0.0D }, { 1.0D, 0.0D, 0.0D }, { 0.0D, 0.0D, 1.0D }, { 1.0D, 0.0D, 1.0D },
				{ 0.0D, 1.0D, 0.0D }, { 1.0D, 1.0D, 0.0D }, { 0.0D, 1.0D, 1.0D }, { 1.0D, 1.0D, 1.0D },
		};
		for (ServerPlayer player : level.players()) {
			// 旁观者不算观察者（旁观模式下能看到它移动）。
			if (player.isSpectator()) {
				continue;
			}
			if (body.distanceTo(player) > cfg.viewDistance) {
				continue;
			}
			Vec3 eye = player.getEyePosition();
			Vec3 view = player.getViewVector(1.0F).normalize();
			for (double[] offset : offsets) {
				Vec3 point = new Vec3(
						box.minX + offset[0] * box.getXsize(),
						box.minY + offset[1] * box.getYsize(),
						box.minZ + offset[2] * box.getZsize());
				Vec3 toPoint = point.subtract(eye);
				double distance = toPoint.length();
				if (distance < 0.01D) {
					return true;
				}
				double dot = view.dot(toPoint.normalize());
				boolean onScreen = dot >= cosLimit;
				boolean rightInFront = distance <= Math.max(0.0D, cfg.closeRangeObserved) && dot > 0.0D;
				if (!onScreen && !rightInFront) {
					continue;
				}
				BlockHitResult hit = level.clip(new ClipContext(eye, point,
						ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
				if (hit.getType() == HitResult.Type.MISS) {
					return true;
				}
			}
		}
		return false;
	}

	private static DamageSource neckSnapSource(ServerLevel level) {
		Holder<DamageType> holder = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ScpMod.NECK_SNAP);
		return new DamageSource(holder, body);
	}

	/** 让雕像面向目标玩家。 */
	private static void faceTarget(ServerPlayer target) {
		double dx = target.getX() - body.getX();
		double dz = target.getZ() - body.getZ();
		float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
		body.setYRot(yaw);
		body.setYHeadRot(yaw);
		body.yBodyRot = yaw;
		body.yBodyRotO = yaw;
	}

	/** Moves toward the player with collision + simple grid detours (no wall phasing). */
	private static void moveTowards(ServerLevel level, ServerPlayer target, long now) {
		AnomalyConfig cfg = AnomalyConfig.get();
		double remaining = Math.max(0.5D, cfg.maxStep);
		double chunk = 0.5D;
		for (int guard = 0; guard < 64 && remaining > 0.001D; guard++) {
			Vec3 from = body.position();
			Vec3 towards = target.position().subtract(from);
			if (Math.hypot(towards.x, towards.z) <= cfg.killRange) {
				return;
			}
			Vec3 direction = new Vec3(towards.x, 0.0D, towards.z).normalize();
			Vec3 candidate = from.add(direction.scale(Math.min(chunk, remaining)));
			if (!isPathClear(level, from, candidate)) {
				// 遇到一格高的方块/台阶：抬升一格跨过去（不穿墙，只垂直抬升）。
				if (tryStepUp(level, from, direction)) {
					continue;
				}
				Vec3 waypoint = nextWaypoint(level, target, now);
				if (waypoint == null) {
					return;
				}
				Vec3 delta = new Vec3(waypoint.x - from.x, 0.0D, waypoint.z - from.z);
				if (delta.length() < 0.05D) {
					return;
				}
				candidate = from.add(delta.normalize().scale(Math.min(chunk, remaining)));
				if (!isPathClear(level, from, candidate)) {
					return;
				}
			}
			body.move(MoverType.SELF, new Vec3(candidate.x - from.x, 0.0D, candidate.z - from.z));
			double moved = body.position().distanceTo(from);
			remaining -= Math.max(0.05D, moved);
			if (moved < 1.0E-4D) {
				return;
			}
		}
	}

	/** 尝试跨过一格高的障碍：只做垂直抬升，不改变水平位置（避免穿墙）。 */
	private static boolean tryStepUp(ServerLevel level, Vec3 from, Vec3 direction) {
		Vec3 ahead = from.add(direction.scale(0.6D));
		BlockPos aheadPos = BlockPos.containing(ahead);
		// 前方脚下是实心方块、其上方两格是空气 = 一格高的台阶
		if (level.getBlockState(aheadPos).isAir()) {
			return false;
		}
		if (!level.getBlockState(aheadPos.above()).isAir() || !level.getBlockState(aheadPos.above(2)).isAir()) {
			return false;
		}
		BlockPos current = BlockPos.containing(from);
		for (int i = 1; i <= 3; i++) {
			if (!level.getBlockState(current.above(i)).isAir()) {
				return false;
			}
		}
		body.setPos(from.x, current.getY() + 1.0D, from.z);
		return true;
	}

	private static boolean isPathClear(ServerLevel level, Vec3 from, Vec3 to) {
		BlockPos feet = BlockPos.containing(to);
		if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) {
			return false;
		}
		return hasClearLine(level, from.add(0.0D, 0.5D, 0.0D), to.add(0.0D, 0.5D, 0.0D))
				&& hasClearLine(level, from.add(0.0D, 1.4D, 0.0D), to.add(0.0D, 1.4D, 0.0D));
	}

	private static boolean hasClearLine(ServerLevel level, Vec3 from, Vec3 to) {
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, body));
		return hit.getType() == HitResult.Type.MISS;
	}

	/** Local breadth-first path over walkable block cells; cached for a few ticks. */
	private static Vec3 nextWaypoint(ServerLevel level, ServerPlayer target, long now) {
		if (path.isEmpty() || pathIndex >= path.size() || now - lastPathTick > 10L) {
			path = findPath(level, body.blockPosition(), target.blockPosition());
			pathIndex = 0;
			lastPathTick = now;
		}
		if (pathIndex >= path.size()) {
			return null;
		}
		BlockPos next = path.get(pathIndex);
		if (body.blockPosition().distManhattan(next) <= 1) {
			pathIndex++;
			if (pathIndex >= path.size()) {
				return null;
			}
			next = path.get(pathIndex);
		}
		return new Vec3(next.getX() + 0.5D, next.getY(), next.getZ() + 0.5D);
	}

	private static List<BlockPos> findPath(ServerLevel level, BlockPos start, BlockPos goal) {
		int radius = 12;
		Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(start);
		cameFrom.put(start, null);
		int steps = 0;
		while (!queue.isEmpty() && steps++ < 800) {
			BlockPos current = queue.poll();
			if (current.distManhattan(goal) <= 1) {
				List<BlockPos> result = new ArrayList<>();
				BlockPos node = current;
				while (node != null && !node.equals(start)) {
					result.add(0, node);
					node = cameFrom.get(node);
				}
				return result;
			}
			for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
				BlockPos next = current.relative(direction);
				if (cameFrom.containsKey(next) || next.distManhattan(start) > radius) {
					continue;
				}
				if (!isWalkable(level, next)) {
					continue;
				}
				cameFrom.put(next, current);
				queue.add(next);
			}
		}
		return new ArrayList<>();
	}

	private static boolean isWalkable(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos).isAir()
				&& level.getBlockState(pos.above()).isAir()
				&& level.getBlockState(pos.above(2)).isAir()
				&& level.getBlockState(pos.below()).isSolid();
	}
}
