package com.theconstant.dontstravemc.entity.animal;

import com.theconstant.dontstravemc.entity.util.stateutil.Eater;
import com.theconstant.dontstravemc.entity.util.stateutil.Sleepable;
import com.theconstant.dontstravemc.entity.ai.goal.GenericSleepGoal;
import com.theconstant.dontstravemc.entity.ai.goal.target.EatItemGoal;
import com.theconstant.dontstravemc.entity.ai.goal.animal.RabbitAvoidPlayerGoal;
import com.theconstant.dontstravemc.entity.ai.goal.animal.RabbitSocialPanicGoal;
import com.theconstant.dontstravemc.entity.ai.goal.animal.RabbitHurtFleeGoal;
import com.theconstant.dontstravemc.entity.ai.goal.animal.RabbitAlertPlayerGoal;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class RabbitEntity extends PathfinderMob implements GeoEntity, Eater, Sleepable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_ALERT = SynchedEntityData.defineId(RabbitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(RabbitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> EATING_ITEM = SynchedEntityData.defineId(RabbitEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> SLEEP_PHASE = SynchedEntityData.defineId(RabbitEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE2_ANIM = RawAnimation.begin().thenPlay("idle2");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation EAT_ANIM = RawAnimation.begin().thenLoop("eat");
    private static final RawAnimation LOOK_PRE = RawAnimation.begin().thenPlay("look_pre");
    private static final RawAnimation LOOK_LOOP = RawAnimation.begin().thenLoop("look_loop");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");
    private static final RawAnimation SLEEP_PRE = RawAnimation.begin().thenPlay("sleep_pre");
    private static final RawAnimation SLEEP_LOOP = RawAnimation.begin().thenLoop("sleep_loop");
    private static final RawAnimation SLEEP_PST = RawAnimation.begin().thenPlay("sleep_pst");

    private int idleCount = 0;
    private int nextSpecialIdle = 5;
    private int sleepTick = 0;
    private int lookPreTick = -100;
    private boolean wasAlert = false;
    private boolean wasMoving = false;
    private int stepSoundCooldown = 0;
    private int screamCooldown = 0;
    private int sprintParticleTick = 0;
    private int hopCooldown = 0;

    public RabbitEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.nextSpecialIdle = 5 + this.random.nextInt(6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RabbitSocialPanicGoal(this));
        this.goalSelector.addGoal(2, new GenericSleepGoal(this, mob -> {
            RabbitEntity r = (RabbitEntity) mob;
            return r.wantsNightRest();
        }));
        this.goalSelector.addGoal(3, new RabbitHurtFleeGoal(this, 1.6D));
        this.goalSelector.addGoal(4, new RabbitAvoidPlayerGoal(this, 1.5D));
        this.goalSelector.addGoal(5, new RabbitAlertPlayerGoal(this));
        this.goalSelector.addGoal(6, new EatItemGoal(this, 1.0D, stack ->
                isVegetarian(stack)));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.05F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    /** 夜间且无战斗目标时倾向于休息（原「有洞才睡」在移除兔子洞后改为仅看昼夜）。 */
    private boolean wantsNightRest() {
        if (this.getTarget() != null) return false;
        Level rabbitLevel = this.level();
        long time = rabbitLevel.getDefaultClockTime() % 24000;
        if (ConstantRealmTime.isTheConstant(rabbitLevel)) {
            return ConstantRealmTime.isNight(rabbitLevel, rabbitLevel.getDefaultClockTime());
        }
        return time >= 13000 && time <= 23000;
    }

    private boolean isVegetarian(ItemStack stack) {
        if (!stack.has(DataComponents.FOOD)) return false;
        Item item = stack.getItem();
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        return name.contains("berry") || name.contains("carrot") || name.contains("seed") || name.contains("apple");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<RabbitEntity> controller = new AnimationController<>("controller", 0, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH_ANIM);
            }

            int sleepPhase = this.entityData.get(SLEEP_PHASE);
            if (sleepPhase == 2) return state.setAndContinue(SLEEP_LOOP);
            if (sleepPhase == 1) return state.setAndContinue(SLEEP_PRE);
            if (sleepPhase == 3) return state.setAndContinue(SLEEP_PST);

            if (this.isEating()) {
                return state.setAndContinue(EAT_ANIM);
            }

            if (state.isMoving()) {
                if (!this.wasMoving) {
                    this.idleCount = 0;
                }
                this.wasMoving = true;

                if (this.isAlert() || this.getLastHurtByMob() != null) {
                    state.setControllerSpeed(1.8f);
                    return state.setAndContinue(RUN_ANIM);
                }
                state.setControllerSpeed(1.3f);
                return state.setAndContinue(WALK_ANIM);
            }

            this.wasMoving = false;

            if (this.isAlert()) {
                if ((this.tickCount - this.lookPreTick) < 15) {
                    return state.setAndContinue(LOOK_PRE);
                }
                return state.setAndContinue(LOOK_LOOP);
            }

            if (!state.isMoving() && this.tickCount % 200 == 0) {
                this.idleCount++;

                if (this.idleCount >= this.nextSpecialIdle) {
                    this.idleCount = 0;
                    this.nextSpecialIdle = 5 + this.random.nextInt(6);
                    return state.setAndContinue(IDLE2_ANIM);
                }
            }

            return state.setAndContinue(IDLE_ANIM);
        });

        controllers.add(controller);
    }

    @Override
    public void aiStep() {
        super.aiStep();


        boolean isRunning = this.isAlert() || this.getLastHurtByMob() != null;
        int hopInterval = isRunning ? 8 : 12;

        // 小跳只在服务端、且正在沿路径移动时触发；起跳时把水平速度对准下一个路径点，
        // 避免空中惯性方向与路径方向不一致导致绕路径点转圈。
        if (this.hopCooldown > 0) this.hopCooldown--;
        if (!this.level().isClientSide() && this.onGround() && this.hopCooldown <= 0
                && this.getNavigation().isInProgress()
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
            net.minecraft.world.level.pathfinder.Path path = this.getNavigation().getPath();
            Vec3 motion = this.getDeltaMovement();
            double hSpeed = Math.sqrt(motion.horizontalDistanceSqr());
            double vx = motion.x, vz = motion.z;
            if (path != null && !path.isDone()) {
                Vec3 next = path.getNextEntityPos(this);
                double dx = next.x - this.getX(), dz = next.z - this.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 1.0E-4) {
                    vx = dx / len * hSpeed;
                    vz = dz / len * hSpeed;
                }
            }
            double jumpHeight = isRunning ? 0.12 : 0.1;
            this.setDeltaMovement(vx, jumpHeight, vz);
            this.hopCooldown = hopInterval;
        }

        // Play step sounds when moving
        if (!this.level().isClientSide() && this.onGround()) {
            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
                if (this.stepSoundCooldown <= 0) {
                    this.playSound(ModSoundEvents.RABBIT_HOP.get(), 0.15F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
                    this.stepSoundCooldown = hopInterval;
                }
            }

            if (this.stepSoundCooldown > 0) {
                this.stepSoundCooldown--;
            }
        }


        if (this.level().isClientSide() && this.onGround()) {
            boolean isSprinting = (this.isAlert() || this.getLastHurtByMob() != null) &&this.getDeltaMovement().horizontalDistanceSqr() > 0.01;
            if (isSprinting) {
                this.sprintParticleTick++;
                if (this.sprintParticleTick >= 3) {
                    this.sprintParticleTick = 0;
                    net.minecraft.core.BlockPos blockBelow = this.blockPosition().below();
                    BlockState blockState = this.level().getBlockState(blockBelow);
                    if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                        Vec3 motion = this.getDeltaMovement();
                        this.level().addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + 0.1,
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            motion.x * -1.0,
                            0.0,
                            motion.z * -1.0
                        );
                    }
                }
            }
        }

        if (!this.level().isClientSide()) {
            if (this.screamCooldown > 0) {
                this.screamCooldown--;
            }

            int phase = this.entityData.get(SLEEP_PHASE);
            if (phase == 1 && (this.tickCount - this.sleepTick) > 20) this.entityData.set(SLEEP_PHASE, 2);
            if (phase == 3 && (this.tickCount - this.sleepTick) > 20) this.entityData.set(SLEEP_PHASE, 0);

            if (this.isAlert() && !this.wasAlert) {
                this.lookPreTick = this.tickCount;
            }
            this.wasAlert = this.isAlert();
        }
    }

    public void triggerScream() {
        if (!this.level().isClientSide() && this.screamCooldown <= 0) {
            this.playSound(ModSoundEvents.RABBIT_SCREAM.get(), 0.8F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
            this.screamCooldown = 100;
        }
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.RABBIT_SCREAM.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ALERT, false);
        builder.define(IS_EATING, false);
        builder.define(EATING_ITEM, ItemStack.EMPTY);
        builder.define(SLEEP_PHASE, 0);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
    }

    @Override
    public void setEating(boolean eating) { this.entityData.set(IS_EATING, eating); }

    public boolean isEating() { return this.entityData.get(IS_EATING); }

    @Override
    public void setEatingItem(ItemStack stack) { this.entityData.set(EATING_ITEM, stack); }

    @Override
    public SoundEvent getEatingSound() { return null; }

    public boolean isAlert() { return this.entityData.get(IS_ALERT); }

    public void setAlert(boolean alert) { this.entityData.set(IS_ALERT, alert); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isAsleep() {
        int phase = this.entityData.get(SLEEP_PHASE);
        return phase == 1 || phase == 2;
    }

    @Override
    public void setAsleep(boolean asleep) {
        int phase = this.entityData.get(SLEEP_PHASE);

        if (asleep) {
            if (phase == 0 || phase == 3) {
                this.entityData.set(SLEEP_PHASE, 1);
                this.sleepTick = this.tickCount;
            }
        } else {
            if (phase == 1 || phase == 2) {
                this.entityData.set(SLEEP_PHASE, 3);
                this.sleepTick = this.tickCount;
            }
        }
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEP_PHASE) == 2;
    }

    public boolean isFood(ItemStack stack) {
        return isVegetarian(stack);
    }
}
