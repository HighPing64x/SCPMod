package com.scpmod.entity;

import com.scpmod.content.ModSounds;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * SCP-131 眼豆：约 30cm 高的泪滴状生物，身体中央单只蓝眼，底部有轮状突出物。
 * <p>行为：玩家右键抚摸后与其建立亲近关系，之后会一直跟着该玩家；
 * 靠近危险异常（SCP-173）时会绕到同伴脚边尖叫警告，并主动盯着 173（这正是它的看守能力）。
 */
public class Scp131Entity extends PathfinderMob {
	/** 0 = SCP-131-A（燃橙色），1 = SCP-131-B（芥末黄）。 */
	private static final EntityDataAccessor<Integer> DATA_VARIANT =
			SynchedEntityData.defineId(Scp131Entity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_WARNING =
			SynchedEntityData.defineId(Scp131Entity.class, EntityDataSerializers.BOOLEAN);

	/** 建立亲近关系的玩家。 */
	private UUID bondedPlayer;
	private int warnCooldown;
	/** 下一次自行吱呀的倒计时。 */
	private int chirpTimer = 60 + this.random.nextInt(120);

	public Scp131Entity(EntityType<? extends Scp131Entity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_VARIANT, 0);
		builder.define(DATA_WARNING, false);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new Scp131FollowGoal(this, 1.6D, 3.0F, 10.0F));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.2D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}

	public static AttributeSupplier.Builder createAttributes() {
		// 动作极快（原文：数秒内可移动 60m 以上），几乎没有生命值压力（Safe 级）。
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.5D)
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D)
				// 眼豆是 Safe 级无害生物：不会主动攻击，也只受轻微伤害。
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
	}

	public int getVariant() {
		return this.entityData.get(DATA_VARIANT);
	}

	public void setVariant(int variant) {
		this.entityData.set(DATA_VARIANT, Math.max(0, Math.min(1, variant)));
	}

	public boolean isWarning() {
		return this.entityData.get(DATA_WARNING);
	}

	private void setWarning(boolean warning) {
		if (this.entityData.get(DATA_WARNING) != warning) {
			this.entityData.set(DATA_WARNING, warning);
		}
	}

	public UUID getBondedPlayer() {
		return this.bondedPlayer;
	}

	public void setBondedPlayer(UUID uuid) {
		this.bondedPlayer = uuid;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		// 右键抚摸 = 建立亲近关系（原文：对任何给予它们的情感反应良好）。
		if (!this.level().isClientSide()) {
			if (this.bondedPlayer == null || !this.bondedPlayer.equals(player.getUUID())) {
				this.bondedPlayer = player.getUUID();
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.HEART,
							this.getX(), this.getY() + 0.5D, this.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.0D);
					this.level().playSound(null, this.blockPosition(), ModSounds.SCP131_CHIRP,
							SoundSource.NEUTRAL, 0.6F, 1.3F);
				}
				this.setPersistenceRequired();
				com.scpmod.item.ScpDocumentItem.record((net.minecraft.server.level.ServerPlayer) player, "scp_131");
			} else {
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.HEART,
							this.getX(), this.getY() + 0.5D, this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide()) {
			return;
		}
		if (this.warnCooldown > 0) {
			this.warnCooldown--;
		}
		// 平时也会不定时发出尖锐吱呀声（原文：以不能翻译的尖锐吱呀声互相交流）。
		// 为避免一群眼豆同时尖叫导致音量叠加，只有附近「发言者」不超过 2 只时才出声。
		if (--this.chirpTimer <= 0) {
			this.chirpTimer = 300 + this.random.nextInt(400);
			if (scpmod$canSpeak()) {
				this.level().playSound(null, this.blockPosition(), ModSounds.SCP131_CHIRP,
						SoundSource.NEUTRAL, 0.45F, 1.1F + (this.random.nextFloat() - 0.5F) * 0.3F);
			}
		}
		// 检测附近的危险异常（SCP-173）：靠近时尖叫警告并转为「看守」，盯着它不动。
		Scp173Entity danger = this.level().getEntitiesOfClass(Scp173Entity.class,
				this.getBoundingBox().inflate(16.0D)).stream().findFirst().orElse(null);
		if (danger != null) {
			this.getLookControl().setLookAt(danger, 30.0F, 30.0F);
			this.setWarning(true);
			this.getNavigation().stop();
			if (this.warnCooldown <= 0) {
				// 冷却 + 随机抖动，避免多只眼豆的尖叫完全同步叠加。
				this.warnCooldown = 30 + this.random.nextInt(25);
				if (scpmod$canSpeak()) {
					this.level().playSound(null, this.blockPosition(), ModSounds.SCP131_ALARM,
							SoundSource.NEUTRAL, 0.7F, 1.2F + (this.random.nextFloat() - 0.5F) * 0.3F);
				}
			}
		} else if (this.isWarning()) {
			this.setWarning(false);
		}
	}

	/** 附近同类过多时只有少数发声，防止声音同步叠加。 */
	public boolean scpmod$canSpeak() {
		java.util.List<Scp131Entity> nearby = this.level().getEntitiesOfClass(Scp131Entity.class,
				this.getBoundingBox().inflate(6.0D));
		if (nearby.size() <= 2) {
			return true;
		}
		int better = 0;
		for (Scp131Entity other : nearby) {
			if (other == this) {
				continue;
			}
			// tickCount 早的优先（稳定、不会每 tick 抖动）
			if (other.tickCount < this.tickCount
					|| (other.tickCount == this.tickCount && other.getId() < this.getId())) {
				better++;
			}
		}
		return better < 2;
	}

	/** 眼豆是否正在盯着给定的 173（用于让 173 无法移动）。 */
	public boolean isWatching(LivingEntity target) {
		if (target == null || !this.isAlive()) {
			return false;
		}
		if (this.distanceToSqr(target) > 24.0D * 24.0D) {
			return false;
		}
		// 视线方向朝向目标即可（眼豆没有「眨眼」，一直盯着看）。
		net.minecraft.world.phys.Vec3 toTarget = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
				.subtract(this.getEyePosition()).normalize();
		return this.getViewVector(1.0F).dot(toTarget) > 0.6D;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		// 被打时发出恐慌尖叫；如果是被亲近的玩家打的，会解除亲近关系（记仇）。
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt) {
			this.level().playSound(null, this.blockPosition(), ModSounds.SCP131_ALARM,
					SoundSource.NEUTRAL, 0.8F, 0.95F);
			level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
					this.getX(), this.getY() + 0.4D, this.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);
			if (source.getEntity() instanceof Player player) {
				if (this.bondedPlayer != null && this.bondedPlayer.equals(player.getUUID())) {
					this.bondedPlayer = null;
					player.sendSystemMessage(Component.translatable("message.scpmod.scp131.bond_broken"));
				}
			}
		}
		return hurt;
	}

	@Override
	protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Variant", this.getVariant());
		if (this.bondedPlayer != null) {
			output.putString("Bonded", this.bondedPlayer.toString());
		}
	}

	@Override
	protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setVariant(input.getIntOr("Variant", 0));
		String bonded = input.getStringOr("Bonded", "");
		if (!bonded.isEmpty()) {
			try {
				this.bondedPlayer = UUID.fromString(bonded);
			} catch (IllegalArgumentException ignored) {
				this.bondedPlayer = null;
			}
		}
	}

	/** 跟随亲近玩家（比原版 FollowOwnerGoal 更宽松：也会跟着跑）。 */
	static class Scp131FollowGoal extends net.minecraft.world.entity.ai.goal.Goal {
		private final Scp131Entity bean;
		private final double speed;
		private final float startDistance;
		private final float stopDistance;
		private Player owner;

		Scp131FollowGoal(Scp131Entity bean, double speed, float startDistance, float stopDistance) {
			this.bean = bean;
			this.speed = speed;
			this.startDistance = startDistance;
			this.stopDistance = stopDistance;
		}

		@Override
		public boolean canUse() {
			UUID uuid = this.bean.getBondedPlayer();
			if (uuid == null) {
				return false;
			}
			Player player = this.bean.level().getPlayerByUUID(uuid);
			if (player == null || !player.isAlive() || player.isSpectator()) {
				return false;
			}
			// 有 173 在附近时优先「看守」，不跟着跑。
			if (this.bean.isWarning()) {
				return false;
			}
			if (this.bean.distanceToSqr(player) < (double) (this.startDistance * this.startDistance)) {
				return false;
			}
			this.owner = player;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return this.owner != null && this.owner.isAlive() && !this.bean.isWarning()
					&& this.bean.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
		}

		@Override
		public void start() {
			this.bean.getNavigation().moveTo(this.owner, this.speed);
		}

		@Override
		public void stop() {
			this.owner = null;
			this.bean.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (this.owner != null && this.bean.tickCount % 10 == 0) {
				this.bean.getNavigation().moveTo(this.owner, this.speed);
			}
		}
	}
}
