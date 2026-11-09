package com.theconstant.dontstravemc.entity.animal;

import com.theconstant.dontstravemc.entity.ai.goal.GenericSleepGoal;
import com.theconstant.dontstravemc.entity.ai.goal.animal.MoleWormBreakFlintGoal;
import com.theconstant.dontstravemc.entity.ai.goal.animal.MoleWormCollectItemGoal;
import com.theconstant.dontstravemc.entity.effect.MoleWormBurrowCrackManager;
import com.theconstant.dontstravemc.entity.util.stateutil.Sleepable;
import com.theconstant.dontstravemc.item.ModItems;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.sounds.SoundEvent;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;

public class MoleWormEntity extends PathfinderMob implements GeoEntity, Sleepable {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_UNDERGROUND = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_STUNNED = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING_DATA = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STUN_PHASE = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SLEEP_PHASE = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_ANIM_TICKS = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRANSITION_ANIM = SynchedEntityData.defineId(MoleWormEntity.class, EntityDataSerializers.INT); // 0=none 1=enter 2=exit

    private ItemStack slot0 = ItemStack.EMPTY;
    private ItemStack slot1 = ItemStack.EMPTY;
    /** Gameplay (not animation): after any mole nearby is hurt, moles stay hidden/awake for this long. */
    public static final int HIT_HIDE_TICKS = 2400;
    private int hitHideTicks = 0;
    private static final int STUN_DURATION = 100;
    private int stunnedTicks = 0;

    // pre/pst anim phase duration (server ticks)
    private static final int STUN_PRE_TICKS  = 15;
    private static final int STUN_PST_TICKS  = 15;
    private static final int SLEEP_PRE_TICKS = 20;
    private static final int SLEEP_PST_TICKS = 25;
    private static final int ENTER_TRANSITION_TICKS = 45;
    private static final int EXIT_TRANSITION_TICKS = 55;
    private int stunPhaseTicks  = 0;
    private int sleepPhaseTicks = 0;
    private int transitionTicks = 0;

    // client-side animation state
    private int walkPhase = 0; // 0=idle 1=pre 2=loop 3=pst

    public boolean isInWalkLoopAnim() {
        return this.level().isClientSide() && this.isUnderground() && this.walkPhase == 2;
    }

    /** 供客户端音效管理器查询当前过渡动画状态（0=none 1=enter 2=exit） */
    public int getTransitionAnimPublic() { return getTransitionAnim(); }

    /** 供客户端音效管理器查询当前眩晕动画阶段（0=none 1=pre 2=loop 3=pst） */
    public int getStunPhasePublic() { return getStunPhase(); }

    /** 当前是否正在播放 idle 动画（在地面且无其他优先动画） */
    public boolean isInIdleAnim() {
        if (!this.level().isClientSide()) return false;
        if (this.isDeadOrDying()) return false;
        if (getStunPhase() != 0) return false;
        if (getSleepPhase() != 0) return false;
        if (getTransitionAnim() != 0) return false;
        if (isUnderground()) return false;
        if (getActionAnimTicks() > 0) return false;
        return true;
    }

    /** 当前是否正在播放 sleep_loop 动画 */
    public boolean isInSleepLoopAnim() {
        if (!this.level().isClientSide()) return false;
        return getSleepPhase() == 2;
    }

    /** 当前是否正在播放 action 动画 */
    public boolean isInActionAnim() {
        if (!this.level().isClientSide()) return false;
        return getActionAnimTicks() > 0;
    }

    private static final RawAnimation IDLE_ANIM       = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE_UNDER_ANIM = RawAnimation.begin().thenLoop("idle_under");
    private static final RawAnimation ENTER_ANIM      = RawAnimation.begin().thenPlay("enter");
    private static final RawAnimation EXIT_ANIM       = RawAnimation.begin().thenPlay("exit");
    private static final RawAnimation WALK_PRE_ANIM   = RawAnimation.begin().thenPlay("walk_pre");
    private static final RawAnimation WALK_LOOP_ANIM  = RawAnimation.begin().thenLoop("walk_loop");
    private static final RawAnimation WALK_PST_ANIM   = RawAnimation.begin().thenPlay("walk_pst");
    private static final RawAnimation ACTION_ANIM     = RawAnimation.begin().thenPlay("action");
    private static final RawAnimation DEATH_ANIM      = RawAnimation.begin().thenPlayAndHold("death");
    private static final RawAnimation SLEEP_PRE       = RawAnimation.begin().thenPlay("sleep_pre");
    private static final RawAnimation SLEEP_LOOP      = RawAnimation.begin().thenLoop("sleep_loop");
    private static final RawAnimation SLEEP_PST       = RawAnimation.begin().thenPlay("sleep_pst");
    private static final RawAnimation STUNNED_PRE     = RawAnimation.begin().thenPlay("stunned_pre");
    private static final RawAnimation STUNNED_LOOP    = RawAnimation.begin().thenLoop("stunned_loop");
    private static final RawAnimation STUNNED_PST     = RawAnimation.begin().thenPlay("stunned_pst");
    public MoleWormEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_UNDERGROUND, true);
        builder.define(IS_STUNNED, false);
        builder.define(IS_SLEEPING_DATA, false);
        builder.define(STUN_PHASE, 0);
        builder.define(SLEEP_PHASE, 0);
        builder.define(ACTION_ANIM_TICKS, 0);
        builder.define(TRANSITION_ANIM, 0);
    }

    public boolean isUnderground() { return this.entityData.get(IS_UNDERGROUND); }
    public void setUnderground(boolean v) {
        boolean prev = isUnderground();
        this.entityData.set(IS_UNDERGROUND, v);
        if (prev != v) {
            this.entityData.set(TRANSITION_ANIM, v ? 2 : 1);
            transitionTicks = v ? EXIT_TRANSITION_TICKS : ENTER_TRANSITION_TICKS;
        }
    }
    public boolean isStunned() { return this.entityData.get(IS_STUNNED); }
    public void setStunned(boolean v) { this.entityData.set(IS_STUNNED, v); }
    @Override public boolean isAsleep() { return this.entityData.get(IS_SLEEPING_DATA); }
    @Override public void setAsleep(boolean v) { this.entityData.set(IS_SLEEPING_DATA, v); }
    private int getStunPhase() { return this.entityData.get(STUN_PHASE); }
    private void setStunPhase(int v) { this.entityData.set(STUN_PHASE, v); }
    private int getSleepPhase() { return this.entityData.get(SLEEP_PHASE); }
    private void setSleepPhase(int v) { this.entityData.set(SLEEP_PHASE, v); }
    private int getActionAnimTicks() { return this.entityData.get(ACTION_ANIM_TICKS); }
    private void setActionAnimTicks(int v) { this.entityData.set(ACTION_ANIM_TICKS, v); }
    private int getTransitionAnim() { return this.entityData.get(TRANSITION_ANIM); }
    private void setTransitionAnim(int v) { this.entityData.set(TRANSITION_ANIM, v); }
    public boolean isTransitioningGroundState() { return getTransitionAnim() != 0; }
    public boolean isMovementLocked() { return isStunned() || isAsleep() || isTransitioningGroundState(); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GenericSleepGoal(this, mob -> {
            boolean isDay = ConstantRealmTime.isDay(mob.level(), mob.level().getDefaultClockTime());
            return isDay && !((MoleWormEntity) mob).isHiding();
        }));
        this.goalSelector.addGoal(5, new MoleWormCollectItemGoal(this));
        this.goalSelector.addGoal(5, new MoleWormBreakFlintGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSoundEvents.MOLE_WORM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.MOLE_WORM_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 0, state -> {
            if (this.isDeadOrDying()) return state.setAndContinue(DEATH_ANIM);

            // stunned state machine
            int stunP = getStunPhase();
            if (stunP == 1) return state.setAndContinue(STUNNED_PRE);  // server timer advances phase
            if (stunP == 2) return state.setAndContinue(STUNNED_LOOP);
            if (stunP == 3) return state.setAndContinue(STUNNED_PST);  // server timer clears phase

            // sleep state machine
            int sleepP = getSleepPhase();
            if (sleepP == 1) return state.setAndContinue(SLEEP_PRE);   // server timer advances phase
            if (sleepP == 2) return state.setAndContinue(SLEEP_LOOP);
            if (sleepP == 3) return state.setAndContinue(SLEEP_PST);   // server timer clears phase

            boolean underground = this.isUnderground();
            int transition = getTransitionAnim();
            if (transition == 1) return state.setAndContinue(ENTER_ANIM); // 地下->地上
            if (transition == 2) return state.setAndContinue(EXIT_ANIM);  // 地上->地下

            if (underground) {
                if (walkPhase == 1) return state.setAndContinue(WALK_PRE_ANIM);
                if (walkPhase == 2) return state.setAndContinue(WALK_LOOP_ANIM);
                if (walkPhase == 3) return state.setAndContinue(WALK_PST_ANIM);
                return state.setAndContinue(IDLE_UNDER_ANIM);
            }

            if (getActionAnimTicks() > 0) return state.setAndContinue(ACTION_ANIM);

            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.isUnderground()) {
            markBurrowCrackTrail((ServerLevel) this.level());
        }

        if (isMovementLocked()) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            if (isStunned()) {
                this.setYRot(this.yRotO);
                this.setXRot(this.xRotO);
                this.yHeadRot = this.yHeadRotO;
                this.yBodyRot = this.yBodyRotO;
            }
        }

        if (this.level().isClientSide()) {
            boolean underground = isUnderground();
            if (underground) {
                boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.000001;
                if (moving) {
                    if (walkPhase == 0) walkPhase = 1;
                    else if (walkPhase == 1) walkPhase = 2;
                } else {
                    if (walkPhase == 2) walkPhase = 3;
                    else if (walkPhase == 3) walkPhase = 0;
                    else if (walkPhase == 1) walkPhase = 0; // pre 后未持续移动，回到 idle_under
                }
            } else {
                walkPhase = 0;
            }
        }
    }

    private void markBurrowCrackTrail(ServerLevel level) {
        MoleWormBurrowCrackManager.markOccupied(level, this.blockPosition());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            if (hitHideTicks > 0) hitHideTicks--;

            // stunned tick + phase (server timer drives pre/pst)
            if (stunnedTicks > 0) {
                stunnedTicks--;
                setStunned(true);
                this.getNavigation().stop();
                if (getStunPhase() == 0) {
                    setStunPhase(1); // enter pre
                    stunPhaseTicks = STUN_PRE_TICKS;
                } else if (getStunPhase() == 1) {
                    if (--stunPhaseTicks <= 0) setStunPhase(2); // pre done -> loop
                }
            } else {
                if (isStunned()) {
                    setStunPhase(3); // enter pst
                    stunPhaseTicks = STUN_PST_TICKS;
                    setStunned(false);
                } else if (getStunPhase() == 3) {
                    if (--stunPhaseTicks <= 0) setStunPhase(0); // pst done -> clear
                }
            }

            // action anim
            if (getActionAnimTicks() > 0) setActionAnimTicks(getActionAnimTicks() - 1);

            // transition anim (enter/exit) auto-clear
            if (transitionTicks > 0) {
                if (--transitionTicks <= 0) setTransitionAnim(0);
            }

            // sleep phase (sleep is considered ground state)
            if (isAsleep()) {
                if (isUnderground()) {
                    setUnderground(false);
                }
                if (getSleepPhase() == 0) {
                    setSleepPhase(1); // enter pre
                    sleepPhaseTicks = SLEEP_PRE_TICKS;
                } else if (getSleepPhase() == 1) {
                    if (--sleepPhaseTicks <= 0) setSleepPhase(2); // pre done -> loop
                }
            } else {
                if (getSleepPhase() == 2) {
                    setSleepPhase(3);
                    sleepPhaseTicks = SLEEP_PST_TICKS;

                    if (!isUnderground()) {
                        setUnderground(true);
                    }
                } else if (getSleepPhase() == 3) {
                    if (--sleepPhaseTicks <= 0) setSleepPhase(0); // pst done -> clear
                }
            }
        }
    }

    public void triggerActionAnim() { setActionAnimTicks(30); }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(net.minecraft.world.entity.Entity entity) {
        // 禁止实体碰撞推动鼹鼠
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (isUnderground()) return true;
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isUnderground()) return false;
        if (source.getDirectEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            if (weapon.is(ModItems.HAMMER.get())) {
                stunnedTicks = STUN_DURATION;
                setStunned(true);
                setStunPhase(1);
                stunPhaseTicks = STUN_PRE_TICKS;
                return false;
            }
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            List<MoleWormEntity> nearby = level.getEntitiesOfClass(MoleWormEntity.class, this.getBoundingBox().inflate(16.0D));
            for (MoleWormEntity mole : nearby) mole.triggerHitHide();
        }
        return hurt;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && isStunned()) {
            ItemStack egg = new ItemStack(ModItems.MOLE_WORM_SPAWN_EGG.get());
            if (!player.getInventory().add(egg)) player.drop(egg, false);
            this.discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void triggerHitHide() { this.hitHideTicks = HIT_HIDE_TICKS; }
    public boolean isHiding() { return hitHideTicks > 0; }

    public boolean tryPickupItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!slot0.isEmpty() && ItemStack.isSameItemSameComponents(slot0, stack)) { slot0.grow(stack.getCount()); return true; }
        if (!slot1.isEmpty() && ItemStack.isSameItemSameComponents(slot1, stack)) { slot1.grow(stack.getCount()); return true; }
        if (slot0.isEmpty()) { slot0 = stack.copy(); return true; }
        if (slot1.isEmpty()) { slot1 = stack.copy(); return true; }
        return false;
    }

    public boolean isInventoryFull() { return !slot0.isEmpty() && !slot1.isEmpty(); }

    @Override
    public void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("HitHideTicks", hitHideTicks);
        out.putInt("StunnedTicks", stunnedTicks);
        if (!slot0.isEmpty()) out.store("Slot0", ItemStack.CODEC, slot0);
        if (!slot1.isEmpty()) out.store("Slot1", ItemStack.CODEC, slot1);
    }

    @Override
    public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        hitHideTicks = in.getIntOr("HitHideTicks", 0);
        stunnedTicks = in.getIntOr("StunnedTicks", 0);
        slot0 = in.read("Slot0", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        slot1 = in.read("Slot1", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}
