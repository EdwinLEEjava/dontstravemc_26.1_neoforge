package com.theconstant.dontstravemc.entity.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

/**
 * 桦栗果精（Don't Starve 的 Birchnutter）：小型跳咬怪。
 * 攻击时先播放跳扑动画，在落地合嘴（0.4s，第 8 tick）时才结算伤害；
 * 死亡动画 1.25s，因此把移除时间从原版 20 tick 延到 {@link #DEATH_REMOVE_TICKS}。
 */
public class BirchFruitSpriteEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(BirchFruitSpriteEntity.class, EntityDataSerializers.BOOLEAN);

    /** 与 attack 动画长度一致：1.1s = 22 tick。 */
    private static final int ATTACK_ANIMATION_TICKS = 22;
    /** 动画在 0.4s 落地合嘴，此时结算伤害。 */
    private static final int ATTACK_IMPACT_DELAY = 8;
    /** death 动画 1.25s = 25 tick，再停留 5 tick 后移除。 */
    private static final int DEATH_REMOVE_TICKS = 30;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTick = 0;
    private int ticksUntilImpact = -1;
    @Nullable
    private LivingEntity pendingTarget;

    public BirchFruitSpriteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ATTACKING, false);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    private void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.ticksUntilImpact > 0) {
            this.ticksUntilImpact--;
            if (this.ticksUntilImpact == 0) {
                this.performActualImpact();
            }
        }
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
            this.setAttacking(true);
        } else if (this.isAttacking()) {
            this.setAttacking(false);
        }
    }

    /** 近战目标触发的攻击：先播放动画，延迟到落地时结算。 */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.attackAnimationTick > 0) {
            return false;
        }
        if (target instanceof LivingEntity living) {
            this.attackAnimationTick = ATTACK_ANIMATION_TICKS;
            this.ticksUntilImpact = ATTACK_IMPACT_DELAY;
            this.pendingTarget = living;
            this.setAttacking(true);
            this.swing(InteractionHand.MAIN_HAND);
            return false;
        }
        return super.doHurtTarget(level, target);
    }

    private void performActualImpact() {
        LivingEntity target = this.pendingTarget;
        this.pendingTarget = null;
        if (target != null && target.isAlive() && this.isAlive()
                && this.distanceToSqr(target) < 4.0D
                && this.level() instanceof ServerLevel serverLevel) {
            super.doHurtTarget(serverLevel, target);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_REMOVE_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<BirchFruitSpriteEntity>("Base", 3, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH_ANIM);
            }
            if (this.isAttacking()) {
                return state.setAndContinue(ATTACK_ANIM);
            }
            if (state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
                return state.setAndContinue(WALK_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
