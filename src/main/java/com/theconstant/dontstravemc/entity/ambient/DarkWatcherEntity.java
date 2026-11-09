package com.theconstant.dontstravemc.entity.ambient;

import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class DarkWatcherEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Integer> DATA_ANIM_PHASE =
            SynchedEntityData.defineId(DarkWatcherEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation APPEAR_ANIM = RawAnimation.begin().thenPlay("appear");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DISAPPEAR_ANIM = RawAnimation.begin().thenPlay("disappear");

    private static final int APPEAR_DURATION_TICKS = 10;
    private static final int DISAPPEAR_DURATION_TICKS = 5;
    private static final int MAX_LIFETIME_TICKS = 2400; // 2 分钟

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private Player targetPlayer;
    private int despawnCheckTicks = 0;
    private int animPhaseTicks = 0;

    private enum AnimPhase {
        APPEAR,
        IDLE,
        DISAPPEAR
    }

    public DarkWatcherEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new WatcherMoveControl(this);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM_PHASE, AnimPhase.APPEAR.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DarkWatcherFollowGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;

        if (!this.level().isClientSide()) {
            // 每帧强制朝向玩家
            if (targetPlayer != null && targetPlayer.isAlive()) {
                double dx = targetPlayer.getX() - this.getX();
                double dy = targetPlayer.getEyeY() - this.getEyeY();
                double dz = targetPlayer.getZ() - this.getZ();
                double hDist = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float)(Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
                float pitch = (float)(-(Mth.atan2(dy, hDist) * 180.0 / Math.PI));
                this.setYRot(yaw);
                this.setXRot(pitch);
            }

            updateAnimationPhase();

            if (getAnimPhase() == AnimPhase.DISAPPEAR) {
                return;
            }

            despawnCheckTicks++;
            if (despawnCheckTicks >= 20) {
                despawnCheckTicks = 0;
                checkDespawnConditions();
            }
        }
    }

    private void updateAnimationPhase() {
        animPhaseTicks++;

        if (getAnimPhase() == AnimPhase.APPEAR && animPhaseTicks >= APPEAR_DURATION_TICKS) {
            setAnimPhase(AnimPhase.IDLE);
            animPhaseTicks = 0;
            return;
        }

        if (getAnimPhase() == AnimPhase.DISAPPEAR && animPhaseTicks >= DISAPPEAR_DURATION_TICKS) {
            this.discard();
        }
    }

    private void startDisappear() {
        if (getAnimPhase() == AnimPhase.DISAPPEAR) {
            return;
        }
        setAnimPhase(AnimPhase.DISAPPEAR);
        animPhaseTicks = 0;
        this.getNavigation().stop();
    }

    private void checkDespawnConditions() {
        if (this.tickCount >= MAX_LIFETIME_TICKS) {
            startDisappear();
            return;
        }

        Player player = this.level().getNearestPlayer(this, 100);
        if (player == null) {
            startDisappear();
            return;
        }

        if (this.distanceToSqr(player) <= 25.0) {
            startDisappear();
            return;
        }

        if (ConstantRealmTime.isDay(this.level(), this.level().getDefaultClockTime())) {
            startDisappear();
            return;
        }

        int lightLevel = this.level().getBrightness(LightLayer.BLOCK, this.blockPosition());
        if (lightLevel > 5) {
            startDisappear();
        }
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DarkWatcherEntity> controller = new AnimationController<>("controller", 0, state -> {
            return switch (getAnimPhase()) {
                case APPEAR -> state.setAndContinue(APPEAR_ANIM);
                case DISAPPEAR -> state.setAndContinue(DISAPPEAR_ANIM);
                case IDLE -> state.setAndContinue(IDLE_ANIM);
            };
        });

        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }

    public Player getTargetPlayer() {
        return this.targetPlayer;
    }

    private AnimPhase getAnimPhase() {
        int ordinal = this.entityData.get(DATA_ANIM_PHASE);
        if (ordinal < 0 || ordinal >= AnimPhase.values().length) {
            return AnimPhase.IDLE;
        }
        return AnimPhase.values()[ordinal];
    }

    private void setAnimPhase(AnimPhase phase) {
        this.entityData.set(DATA_ANIM_PHASE, phase.ordinal());
    }

    class DarkWatcherFollowGoal extends Goal {
        private final DarkWatcherEntity watcher;
        private static final double MIN_DISTANCE = 12.0;
        private static final double MAX_DISTANCE = 15.0;
        private static final double FOLLOW_SPEED = 0.5;

        private int repathCooldown = 0;

        DarkWatcherFollowGoal(DarkWatcherEntity watcher) {
            this.watcher = watcher;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (watcher.getAnimPhase() == AnimPhase.DISAPPEAR) {
                return false;
            }

            Player player = watcher.level().getNearestPlayer(watcher, 100);
            if (player == null) return false;

            watcher.setTargetPlayer(player);
            return true;
        }

        @Override
        public void tick() {
            Player player = watcher.getTargetPlayer();
            if (player == null || !player.isAlive()) return;

            if (repathCooldown > 0) {
                repathCooldown--;
            }

            Vec3 playerPos = player.position();
            Vec3 watcherPos = watcher.position();
            Vec3 outward = new Vec3(watcherPos.x - playerPos.x, 0, watcherPos.z - playerPos.z);

            double horizontalDist = outward.length();
            if (horizontalDist < 1.0E-4) {
                double angle = watcher.getRandom().nextDouble() * Math.PI * 2.0;
                outward = new Vec3(Math.cos(angle), 0, Math.sin(angle));
                horizontalDist = 1.0;
            }

            Vec3 outwardDir = outward.normalize();

            // 超出环带：仅做径向修正，回到中间距离
            if (horizontalDist < MIN_DISTANCE || horizontalDist > MAX_DISTANCE) {
                double targetRadius = (MIN_DISTANCE + MAX_DISTANCE) * 0.5;
                Vec3 targetPos = new Vec3(
                        playerPos.x + outwardDir.x * targetRadius,
                        playerPos.y,
                        playerPos.z + outwardDir.z * targetRadius
                );

                watcher.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, FOLLOW_SPEED);
                repathCooldown = 8;
                return;
            }

            // 在环带内：沿环侧向偏移，制造“徘徊”而不是冲�?
            if (repathCooldown <= 0 || watcher.getNavigation().isDone()) {
                double baseAngle = Math.atan2(outwardDir.z, outwardDir.x);
                double angleOffset = (watcher.getRandom().nextDouble() - 0.5) * 1.2; // ±0.6 弧度
                double targetRadius = MIN_DISTANCE + watcher.getRandom().nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
                double targetAngle = baseAngle + angleOffset;

                Vec3 targetPos = new Vec3(
                        playerPos.x + Math.cos(targetAngle) * targetRadius,
                        playerPos.y,
                        playerPos.z + Math.sin(targetAngle) * targetRadius
                );

                watcher.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, FOLLOW_SPEED * 0.9);
                repathCooldown = 20 + watcher.getRandom().nextInt(15);
            }
        }
    }

    class WatcherMoveControl extends FlyingMoveControl {
        public WatcherMoveControl(Mob mob) {
            super(mob, 20, true);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                this.operation = MoveControl.Operation.WAIT;
                this.mob.setNoGravity(true);
                double d = this.wantedX - this.mob.getX();
                double e = this.wantedY - this.mob.getY();
                double f = this.wantedZ - this.mob.getZ();
                double g = d * d + e * e + f * f;
                if (g < 2.5000003E-7F) {
                    this.mob.setYya(0.0F);
                    this.mob.setZza(0.0F);
                    return;
                }

                // 注意：这里跳过了 setYRot，让朝向�?tick() 方法强制控制
                float i;
                if (this.mob.onGround()) {
                    i = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                } else {
                    i = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
                }

                this.mob.setSpeed(i);
                double j = Math.sqrt(d * d + f * f);
                if (Math.abs(e) > 1.0E-5F || Math.abs(j) > 1.0E-5F) {
                    float k = (float)(-(Mth.atan2(e, j) * 180.0F / (float)Math.PI));
                    this.mob.setXRot(Mth.rotLerp(this.mob.getXRot(), k, 20));
                    this.mob.setYya(e > 0.0 ? i : -i);
                }
            } else {
                this.mob.setNoGravity(false);
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
            }
        }
    }
}
