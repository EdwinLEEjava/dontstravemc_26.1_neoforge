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

public class CreepyEyesEntity extends PathfinderMob implements GeoEntity {

    // --- Synced data ---
    private static final EntityDataAccessor<Integer> DATA_ANIM_PHASE =
            SynchedEntityData.defineId(CreepyEyesEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE_INDEX =
            SynchedEntityData.defineId(CreepyEyesEntity.class, EntityDataSerializers.INT);

    // --- Animations ---
    private static final RawAnimation APPEAR_ANIM    = RawAnimation.begin().thenPlay("appear");
    private static final RawAnimation BLINK_ANIM     = RawAnimation.begin().thenLoop("blink");
    private static final RawAnimation DISAPPEAR_ANIM = RawAnimation.begin().thenPlay("disappear");

    // ticks for anim phase transitions
    private static final int APPEAR_TICKS     = 10; // 0.5 s
    private static final int DISAPPEAR_TICKS  = 5;  // 0.25 s

    private enum AnimPhase { APPEAR, IDLE, DISAPPEAR }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    /** Target player �?set at spawn time. */
    private Player targetPlayer;

    /** How many ticks the current animation phase has been running. */
    private int animPhaseTicks = 0;

    /** Total lifetime in ticks (randomised at spawn: 20-30 s). */
    private int maxLifetimeTicks = 400;

    /** Despawn check accumulator (every 20 ticks / 1 s). */
    private int despawnCheckTicks = 0;

    // -------------------------------------------------------------------------

    public CreepyEyesEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl  = new FlyingMoveControl(this, 20, true);
        this.noPhysics    = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM_PHASE,    AnimPhase.APPEAR.ordinal());
        builder.define(DATA_TEXTURE_INDEX, 1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,     20.0)
                .add(Attributes.FLYING_SPEED,   0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    // -------------------------------------------------------------------------
    // Core tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        // Kill any residual Y velocity so the entity truly hovers in place
        Vec3 vel = this.getDeltaMovement();
        if (vel.y != 0.0) {
            this.setDeltaMovement(vel.x, 0.0, vel.z);
        }

        if (!this.level().isClientSide()) {
            updateAnimPhase();

            if (getAnimPhase() == AnimPhase.DISAPPEAR) return;

            // Always face the target player
            facingTowardPlayer();

            // Teleport if player moved too far
            checkTeleport();

            // Periodic despawn checks
            despawnCheckTicks++;
            if (despawnCheckTicks >= 20) {
                despawnCheckTicks = 0;
                checkDespawnConditions();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Animation phase
    // -------------------------------------------------------------------------

    private void updateAnimPhase() {
        animPhaseTicks++;
        AnimPhase phase = getAnimPhase();

        if (phase == AnimPhase.APPEAR && animPhaseTicks >= APPEAR_TICKS) {
            setAnimPhase(AnimPhase.IDLE);
            animPhaseTicks = 0;
        } else if (phase == AnimPhase.DISAPPEAR && animPhaseTicks >= DISAPPEAR_TICKS) {
            this.discard();
        }
    }

    private void startDisappear() {
        if (getAnimPhase() == AnimPhase.DISAPPEAR) return;
        setAnimPhase(AnimPhase.DISAPPEAR);
        animPhaseTicks = 0;
        this.getNavigation().stop();
    }

    // -------------------------------------------------------------------------
    // Behaviour
    // -------------------------------------------------------------------------

    /** Rotate to face the target player every tick (yaw only). */
    private void facingTowardPlayer() {
        if (targetPlayer == null || !targetPlayer.isAlive()) {
            targetPlayer = this.level().getNearestPlayer(this, 100);
            if (targetPlayer == null) return;
        }
        Vec3 diff = targetPlayer.position().subtract(this.position());
        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        this.setYRot((float) yaw);
        this.yHeadRot = this.getYRot();
        this.yBodyRot  = this.getYRot();
    }

    /** If the target player is more than 20 blocks away (horizontal), teleport
     *  to a random horizontal position within ~12 blocks of the player,
     *  staying roughly within the player's field of view. */
    private void checkTeleport() {
        if (targetPlayer == null || !targetPlayer.isAlive()) return;

        Vec3 playerPos  = targetPlayer.position();
        Vec3 watcherPos = this.position();

        double dx   = watcherPos.x - playerPos.x;
        double dz   = watcherPos.z - playerPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        if (hDist > 20.0) {
            teleportNearPlayer(playerPos);
        }
    }

    private void teleportNearPlayer(Vec3 playerPos) {
        // Pick a random angle, slightly biased toward the player's look direction
        float playerYaw = targetPlayer.getYRot();
        double baseAngle = Math.toRadians(playerYaw);
        // random spread in front: ±70°
        double spreadRad = Math.toRadians(70.0);
        double angle = baseAngle + (this.getRandom().nextDouble() * 2.0 - 1.0) * spreadRad;
        double radius = 11.0 + this.getRandom().nextDouble() * 2.0; // 11-13 blocks

        double newX = playerPos.x + Math.sin(angle) * radius;
        double newZ = playerPos.z + Math.cos(angle) * radius;
        double newY = playerPos.y + (this.getRandom().nextDouble() * 4.0 - 1.0); // ±1-3 blocks height

        this.teleportTo(newX, newY, newZ);
    }

    // -------------------------------------------------------------------------
    // Despawn conditions
    // -------------------------------------------------------------------------

    private void checkDespawnConditions() {
        // 1. Max lifetime
        if (this.tickCount >= maxLifetimeTicks) {
            startDisappear();
            return;
        }

        // 2. Player lookup
        Player nearest = this.level().getNearestPlayer(this, 100);
        if (nearest == null) {
            startDisappear();
            return;
        }

        // 3. Within 5 blocks of player
        if (this.distanceToSqr(nearest) <= 25.0) {
            startDisappear();
            return;
        }

        // 4. Daytime
        if (ConstantRealmTime.isDay(this.level(), this.level().getDefaultClockTime())) {
            startDisappear();
            return;
        }

        // 5. Block light > 5
        int blockLight = this.level().getBrightness(LightLayer.BLOCK, this.blockPosition());
        if (blockLight > 5) {
            startDisappear();
        }
    }

    // -------------------------------------------------------------------------
    // Immunity & physics
    // -------------------------------------------------------------------------

    @Override
    public void push(Entity entity) { }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean isPushable() { return false; }

    // -------------------------------------------------------------------------
    // GeckoLib
    // -------------------------------------------------------------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<CreepyEyesEntity> controller =
                new AnimationController<>("controller", 0, state -> switch (getAnimPhase()) {
                    case APPEAR     -> state.setAndContinue(APPEAR_ANIM);
                    case DISAPPEAR  -> state.setAndContinue(DISAPPEAR_ANIM);
                    case IDLE       -> state.setAndContinue(BLINK_ANIM);
                });
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public void setTargetPlayer(Player player) { this.targetPlayer = player; }
    public Player getTargetPlayer()            { return this.targetPlayer; }

    public void setMaxLifetimeTicks(int ticks) { this.maxLifetimeTicks = ticks; }

    /** Texture index: 1, 2 or 3 */
    public void setTextureIndex(int idx) {
        this.entityData.set(DATA_TEXTURE_INDEX, Mth.clamp(idx, 1, 3));
    }
    public int getTextureIndex() {
        return this.entityData.get(DATA_TEXTURE_INDEX);
    }

    private AnimPhase getAnimPhase() {
        int ord = this.entityData.get(DATA_ANIM_PHASE);
        if (ord < 0 || ord >= AnimPhase.values().length) return AnimPhase.IDLE;
        return AnimPhase.values()[ord];
    }
    private void setAnimPhase(AnimPhase phase) {
        this.entityData.set(DATA_ANIM_PHASE, phase.ordinal());
    }
}
