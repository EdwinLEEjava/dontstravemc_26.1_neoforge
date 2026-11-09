package com.theconstant.dontstravemc.entity.monster;

import com.theconstant.dontstravemc.damagesource.ModDamageTypes;
import com.theconstant.dontstravemc.entity.component.SanityAura;
import com.theconstant.dontstravemc.entity.component.SanityAuraProvider;
import com.theconstant.dontstravemc.entity.util.stateutil.Eater;
import com.theconstant.dontstravemc.entity.ai.goal.GenericSleepGoal;
import com.theconstant.dontstravemc.entity.ai.goal.attack.MonsterSpiderAttackGoal;
import com.theconstant.dontstravemc.entity.ai.goal.target.EatItemGoal;
import com.theconstant.dontstravemc.entity.ai.sensing.DynamicSensing;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import com.theconstant.dontstravemc.entity.util.stateutil.Sleepable;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;

public class MonsterSpiderEntity extends Monster implements Sleepable, GeoEntity, Eater, SanityAuraProvider<MonsterSpiderEntity> {
    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(MonsterSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ALERT = SynchedEntityData.defineId(MonsterSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_ALERTED_TARGET = SynchedEntityData.defineId(MonsterSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(MonsterSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(MonsterSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private final SanityAura<MonsterSpiderEntity> sanityAura;
    private LivingEntity pendingTarget;

    public boolean isEating() {
        return this.entityData.get(IS_EATING);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }



    private int alertTicks = 0;
    private int attackAnimationTick = 0;
    private int ticksUntilImpact = -1;
    private static final int ATTACK_IMPACT_DELAY = 10;
    private static final EntityDataAccessor<ItemStack> EATING_ITEM =
            SynchedEntityData.defineId(MonsterSpiderEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("spider_walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("spider_attack"); // 播放一�?
    private static final RawAnimation ALERT_ANIM = RawAnimation.begin().thenPlay("spider_alert"); // 播放一�?
    private static final RawAnimation GO_TO_SLEEP_ANIM = RawAnimation.begin().thenPlay("spider_sleep").thenLoop("spider_sleeping");
    private static final RawAnimation EAT_ANIM = RawAnimation.begin().thenLoop("spider_eat");
    private static final RawAnimation WAKE_UP_ANIM = RawAnimation.begin().thenPlay("spider_awake");
    private static final RawAnimation DEAD_ANIM = RawAnimation.begin().thenPlayAndHold("spider_dead");


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    public int getAttackDuration() {

        return 45;
    }


    public MonsterSpiderEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.sanityAura = new SanityAura<>(this);
        // -25 per minute = -25 / 60 per second
        this.sanityAura.setAura(-25.0 / 60.0);
    }

    public static AttributeSupplier.Builder createSpiderAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 6.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        AnimationController<MonsterSpiderEntity> baseController = new AnimationController<>("Base", 5, state -> {

            if (this.isDeadOrDying()) {
                state.setControllerSpeed(1.0F);
                return state.setAndContinue(DEAD_ANIM);
            }

            if (this.isAttacking()) {
                state.setControllerSpeed(1.0F);
                return state.setAndContinue(ATTACK_ANIM);
            }

            if (this.isEating()) {
                state.setControllerSpeed(1.0F);
                return state.setAndContinue(EAT_ANIM);
            }


            if (this.isAsleep()) {
                state.setControllerSpeed(1.0F);
                return state.setAndContinue(GO_TO_SLEEP_ANIM);
            }

            if (state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
                state.setControllerSpeed(2.0F);
                return state.setAndContinue(WALK_ANIM);
            }
            if (this.isAlert()) {
                state.setControllerSpeed(1.0F);
                return state.setAndContinue(ALERT_ANIM);
            }

            return PlayState.STOP;
        });

        baseController.setSoundKeyframeHandler(event -> {
            String soundName = event.keyframeData().getSound();

            // 播放声音逻辑
            this.level().playLocalSound(
                    this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("dontstravemc", soundName)
                    ),
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    1.0F,
                    1.0F,
                    false
            );

        });

        controllers.add(baseController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // --- 同步数据访问�?---
    public boolean isAsleep() {
        return this.entityData.get(IS_SLEEPING);
    }

    public void setAsleep(boolean asleep) {
        this.entityData.set(IS_SLEEPING, asleep);
    }

    public boolean isAlert() {
        return this.entityData.get(IS_ALERT);
    }

    public void setAlert(boolean alert) {
        this.entityData.set(IS_ALERT, alert);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MonsterSpiderAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(7, new GenericSleepGoal(this, mob -> {
            boolean restPeriod = ConstantRealmTime.isTheConstant(mob.level())
                    ? !ConstantRealmTime.isNight(mob.level(), mob.level().getDefaultClockTime())
                    : mob.level().isBrightOutside();
            boolean hasNoTarget = mob.getTarget() == null;
            return restPeriod && hasNoTarget;
        }));
        this.goalSelector.addGoal(8, new EatItemGoal(this, 1.0D, stack -> stack.has(DataComponents.FOOD)));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, IronGolem.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // 仅在服务器端处理逻辑
        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();

            if (target != null && target.isAlive()) {
                if (this.isAsleep()) {
                    this.setAsleep(false);
                }
                if (!this.entityData.get(HAS_ALERTED_TARGET)) {
                    LivingEntity currentTarget = this.getTarget();
                    if (currentTarget != null && this.distanceTo(currentTarget) < 8.0D) {
                        if (currentTarget instanceof Player || currentTarget instanceof IronGolem) {
                            this.setAlert(true);
                            this.entityData.set(HAS_ALERTED_TARGET, true);
                            this.alertTicks = 30;
                        }
                    }
                }
            } else {
                if (this.entityData.get(HAS_ALERTED_TARGET)) {
                    this.entityData.set(HAS_ALERTED_TARGET, false);
                    this.setAlert(false);
                }
            }
            if (this.alertTicks > 0) this.alertTicks--;
            if (this.alertTicks <= 0) this.setAlert(false);

            if (this.ticksUntilImpact > 0) {
                this.ticksUntilImpact--;
                if (this.ticksUntilImpact == 0) this.performActualImpact();
            }

            if (this.attackAnimationTick > 0) {
                this.attackAnimationTick--;
                this.setAttacking(true);
            } else {
                this.setAttacking(false);
            }
        }
        if (this.level().isClientSide()) {
            if (this.isEating()) {
                ItemStack eatingStack = this.getEatingItem();
                if (!eatingStack.isEmpty()) {
                    double px = this.getX() + this.getLookAngle().x * 0.5;
                    double py = this.getY() + 0.2;
                    double pz = this.getZ() + this.getLookAngle().z * 0.5;
                    this.level().addParticle(
                            new net.minecraft.core.particles.ItemParticleOption(
                                    net.minecraft.core.particles.ParticleTypes.ITEM,
                                    ItemStackTemplate.fromNonEmptyStack(eatingStack)),
                            px, py, pz, (this.random.nextFloat() - 0.5) * 0.1, 0.1, (this.random.nextFloat() - 0.5) * 0.1
                    );
                }
            }
        }
    }





    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null && this.isAsleep()) {
            this.setAsleep(false);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_SLEEPING, false);
        builder.define(IS_ALERT, false);
        builder.define(HAS_ALERTED_TARGET, false);
        builder.define(IS_ATTACKING, false);
        builder.define(IS_EATING, false);
        builder.define(EATING_ITEM, ItemStack.EMPTY);
    }


    public void startAttackAnimation(LivingEntity target) {
        this.attackAnimationTick = 25; // 整个动作的时�?
        this.ticksUntilImpact = ATTACK_IMPACT_DELAY; // 伤害判定的倒计�?
        this.pendingTarget = target;

        this.setAttacking(true);
        this.swing(InteractionHand.MAIN_HAND);
    }

    private void performActualImpact() {
        if (this.pendingTarget != null && this.pendingTarget.isAlive() && this.distanceToSqr(this.pendingTarget) < 4.0D) {
            if (this.level() instanceof ServerLevel serverLevel) {
                // 这里才真正调用原版的伤害方法
                super.doHurtTarget(serverLevel, this.pendingTarget);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        // 如果不是通过我们自己的计时器触发的，就拦截并转交给延迟逻辑
        if (this.ticksUntilImpact <= 0 && target instanceof LivingEntity living) {
            this.startAttackAnimation(living);
            return false;
        }
        return super.doHurtTarget(world, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 1. 执行原版的受击逻辑（扣血等）
        boolean hasBeenHurt = super.hurtServer(level, source, amount);

        // 2. 如果攻击者是生物（玩家、铁傀儡等），则发起求�?
        if (hasBeenHurt && source.getEntity() instanceof LivingEntity attacker) {
            this.broadcastCallForHelp(level, attacker);
        }

        return hasBeenHurt;
    }

    /**
     * 核心逻辑：向周围同类发送求救广�?
     */
    private void broadcastCallForHelp(ServerLevel level, LivingEntity attacker) {
        // 寻找周围 32 格内的所�?MonsterSpiderEntity
        List<MonsterSpiderEntity> neighbors = level.getEntitiesOfClass(
                MonsterSpiderEntity.class,
                this.getBoundingBox().inflate(32.0D),
                spider -> spider != this // 排除掉被打的自己
        );

        for (MonsterSpiderEntity neighbor : neighbors) {
            // 如果邻居目前没有目标，或者邻居正在睡�?
            if (neighbor.getTarget() == null || neighbor.isAsleep()) {
                neighbor.setTarget(attacker); // 强制索敌攻击�?
                neighbor.setAsleep(false);   // 如果在睡觉，立即惊醒

                // 可选：触发邻居的警觉动作，让它们整齐划一地看向敌�?
                neighbor.setAlert(true);
            }
        }
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        if (source.is(ModDamageTypes.EATEN_MONSTER_FOOD)) {
            return null;
        }

        return ModSoundEvents.MONSTER_SPIDER_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        // 顺便把死亡音效也接上
        return ModSoundEvents.MONSTER_SPIDER_DEATH.get();
    }


    public ItemStack getEatingItem() {
        return this.entityData.get(EATING_ITEM);
    }

    @Override
    public void setEating(boolean eating) {

        this.entityData.set(IS_EATING, eating);
    }

    @Override
    public void setEatingItem(ItemStack stack) {

        this.entityData.set(EATING_ITEM, stack);
    }

    @Override
    public SoundEvent getEatingSound() {
        return ModSoundEvents.MONSTER_SPIDER_EAT.get();
    }

    @Override
    public int getEatingDuration() {
        // 如果你想让蜘蛛吃得很快（0.5秒），就设为 10
        // 如果想让它吃得慢�?秒），就设为 60
        return 12;
    }

    @Override
    public int getPreEatingDuration() {
        // 设置�?8 �?10 帧�?
        // 这意味着蜘蛛看到肉，开始播放动画（低头/扑咬）�?
        // 在这 10 帧里，玩家还能看到肉在地上�?
        // �?11 帧，肉消失，蜘蛛进入循环咀嚼声音和粒子阶段�?
        return 8;
    }

    @Override
    public int getEatingSoundInterval() {
        // 数字越小，咀嚼声越密集（咔咔咔咔咔）
        // 数字越大，咀嚼声越缓慢（�?..�?..咔）
        return 10;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        // 删除原来�?ValueOutput.store("HomeDenPos"...)

        this.sanityAura.toNBT(valueOutput);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        // 删除原来�?ValueInput.read("HomeDenPos"...)

        this.sanityAura.fromNBT(valueInput);
    }

    public SanityAura<MonsterSpiderEntity> getSanityAura() {
        return this.sanityAura;
    }
    
    /**
     * 检查物品是否是蜘蛛的食物（用于陷阱诱饵判定�?
     * 蜘蛛吃所有食�?
     */
    public boolean isFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }
}
