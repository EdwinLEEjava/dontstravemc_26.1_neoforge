package com.theconstant.dontstravemc.entity.neutral;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.util.PigmanHouseHelper;
import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.item.ModItemTags;
import com.theconstant.dontstravemc.entity.component.SanityAura;
import com.theconstant.dontstravemc.entity.component.SanityAuraProvider;
import com.theconstant.dontstravemc.entity.util.stateutil.Eater;
import com.theconstant.dontstravemc.entity.util.stateutil.Sleepable;
import com.theconstant.dontstravemc.entity.ai.navigation.PigmanGroundPathNavigation;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.tag.ModBlockTags;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PigmanEntity extends PathfinderMob implements GeoEntity, Eater, Sleepable, SanityAuraProvider<PigmanEntity> {

    // �??�?? Animations �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static final String IDLE_HAPPY_ANIMATION_NAME = "idle_happy";
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE_CREEPY_ANIM = RawAnimation.begin().thenLoop("idle_creepy");
    private static final RawAnimation IDLE_HAPPY_ANIM = RawAnimation.begin().thenLoop(IDLE_HAPPY_ANIMATION_NAME);
    private static final RawAnimation IDLE_ANGRY_ANIM = RawAnimation.begin().thenLoop("idle_angry");
    private static final RawAnimation IDLE_SCARED_ANIM = RawAnimation.begin().thenLoop("idle_scared");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk_loop");
    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run_loop");
    private static final RawAnimation EAT_ANIM = RawAnimation.begin().thenPlay("eat");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation HUNGRY_ANIM = RawAnimation.begin().thenPlay("hungry");
    private static final RawAnimation TAKE_ANIM = RawAnimation.begin().thenPlay("take");
    private static final RawAnimation REJECT_ANIM = RawAnimation.begin().thenPlay("reject");
    private static final RawAnimation SLEEP_ANIM = RawAnimation.begin().thenPlay("sleep_pre").thenLoop("sleep_loop");
    private static final RawAnimation INBED_ANIM = RawAnimation.begin().thenLoop("inbed");

    // �??�?? Synched data �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> EATING_ITEM = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> EAT_TICKS = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICKS = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_HIRED = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TAKE_TICKS = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REJECT_TICKS = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_RUNNING = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SCARED = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_BED = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SPEECH_TEXT = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SPEECH_TICKS = SynchedEntityData.defineId(PigmanEntity.class, EntityDataSerializers.INT);

    // �??�?? Constants �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static final String NBT_HIRE_TICKS = "PigmanHireTicks";
    private static final String NBT_VEG_COOLDOWN_TICKS = "PigmanVegCooldownTicks";
    private static final String NBT_TOO_CLOSE_COOLDOWN_TICKS = "PigmanTooCloseSpeechCooldownTicks";
    private static final String NBT_HOME_X = "PigmanHomeX";
    private static final String NBT_HOME_Y = "PigmanHomeY";
    private static final String NBT_HOME_Z = "PigmanHomeZ";
    private static final String NBT_HAS_HOME = "PigmanHasHome";

    /** Game ticks granted per 4 nutrition points when fed tagged meat (10 minutes at 20 TPS). */
    private static final int HIRE_TICKS_PER_4_NUTRITION = 10 * 60 * 20;
    private static final int HIRE_MAX_TICKS = 20 * 60 * 50;
    private static final int VEGETARIAN_ACCEPT_COOLDOWN_TICKS = 20 * 15;
    private static final int AVOID_PLAYER_RUN_TICKS = 10;
    private static final int ATTACK_ANIM_TICKS = 13;
    private static final int ATTACK_DAMAGE_DELAY_TICKS = 8; // 0.38s ~= 7.6 ticks
    private static final int ATTACK_EVADE_START_TICKS = 9;
    private static final int TAKE_ANIM_TICKS = 15;
    private static final int REJECT_ANIM_TICKS = 20;
    private static final int HIRE_WARNING_TICKS = 20 * 90;
    private static final float CHASE_SPEED = 1.3333334F;
    private static final double FIGHT_SHARED_RANGE = 30.0D;
    private static final float FOLLOW_SPEED = 1.0F;
    private static final double FOLLOW_START_DIST = 6.0D;
    private static final double TELEPORT_DIST = 24.0D;
    private static final int WORK_SEARCH_RADIUS = 8;
    private static final int WORK_BREAK_TICKS = 50;
    private static final int SPEECH_COOLDOWN = 200;
    private static final int TOO_CLOSE_SPEECH_COOLDOWN_TICKS = 20; // 1s
    private static final int SPEECH_DISPLAY_TICKS = 80;
    private static final double AVOID_PLAYER_DIST = 3.0D;
    private static final double AVOID_PLAYER_WALK_DIST = 6.0D;
    private static final double PIGMAN_ATTACK_REACH = 2.5D;
    private static final double PIGMAN_EVADE_DIST = 5.0D;
    private static final int COMBAT_EVADE_TICKS = 25;
    private static final int NIGHT_SLEEP_LIGHT_THRESHOLD = 5;
    private static final int NIGHT_SLEEP_CHECK_INTERVAL_TICKS = 20 * 60;
    private static final float NIGHT_SLEEP_CHANCE = 0.30F;
    private static final int SCARED_MOOD_TICKS = 20 * 20;
    private static final int PANIC_LIGHT_SEARCH_RADIUS = 10;
    private static final float PANIC_MOVE_SPEED = 1.2F;
    private static final float HOME_RETURN_RUN_SPEED = 1.25F;
    private static final int WAKE_FROM_DAMAGE_COOLDOWN_TICKS = 80;
    private static final float DAY_GO_MEETING_SPEED = 0.85F;
    private static final double MEETING_ARRIVE_DIST_SQR = 9.0D;
    /**
     * �??家�?�?�?�此水平距离平�?��??�?��?�许 MoveControl �?��?��?约 3 格�?�?�?��?�?�?走导�?��?便�?跳台�?��?InteractWithDoor�??     * �?�??导�?�已�?�?�??卡住�?�?�??�?�?��?��?�?线�?�?��?��?��?? MOB 可�?�?��?��??     */
    private static final double GO_HOME_MOVE_CONTROL_MAX_DIST_SQ = 9.0D;
    /** �??家�?可尝�? snap �?��?�?�??水平距离平�?��?约 1.5 格�? */
    private static final double GO_HOME_SNAP_HORIZONTAL_DIST_SQ = 2.25D;
    /** �?�睡 snap �?��?�?�对�?�??仍尝�?�?��??�??�?��?�??�?? InteractWithDoor �?�?��?�?�?��?��?3 格�?�?��?��?�?�常�?��?�? */
    private static final double SLEEP_DOOR_CLOSE_MAX_DIST_TO_FOOT = 16.0D;

    // �??�?? Custom Activities �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    public static final Activity FOLLOW_OWNER = Registry.register(BuiltInRegistries.ACTIVITY, "dontstravemc:follow_owner", new Activity("dontstravemc:follow_owner"));
    public static final Activity WORK = Registry.register(BuiltInRegistries.ACTIVITY, "dontstravemc:work", new Activity("dontstravemc:work"));
    public static final Activity PANIC_DARK = Registry.register(BuiltInRegistries.ACTIVITY, "dontstravemc:panic_dark", new Activity("dontstravemc:panic_dark"));
    public static final Activity SLEEP = Registry.register(BuiltInRegistries.ACTIVITY, "dontstravemc:sleep", new Activity("dontstravemc:sleep"));
    public static final Activity DAY_SOCIAL = Registry.register(BuiltInRegistries.ACTIVITY, "dontstravemc:day_social", new Activity("dontstravemc:day_social"));

    // �??�?? Brain config �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.HOME,
            MemoryModuleType.MEETING_POINT
    );

    private static final ImmutableList<? extends SensorType<? extends Sensor<? super PigmanEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_ITEMS,
            SensorType.HURT_BY
    );

    // �??�?? Instance fields �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SanityAura<PigmanEntity> sanityAura = new SanityAura<>(this);
    private UUID ownerUuid;
    private int hireTicks;
    private int vegetarianAcceptCooldownTicks;
    private int avoidPlayerRunTicks;
    private int tooCloseSpeechCooldownTicks;


    private BlockPos homePos;

    // Work system
    private WorkMode workMode = WorkMode.NONE;
    private BlockPos workTargetPos;
    private int workBreakProgress;

    // Speech cooldown (server-side)
    private int speechCooldown;
    private String lastSpeechCategory = "";
    /** After receive_item bubble ends, play one hired line (first hire from meat only). */
    private boolean pendingHireSpeechAfterReceive;

    // Client-side idle variant tracking
    private boolean wasMovingLastTick;
    private float lastIdleRoll;
    private long nextSleepCheckGameTime = -1L;
    private boolean sleepRollPassed;
    private int scaredMoodTicks;
    private int wakeFromDamageCooldownTicks;
    /** �?�天小�?�?�?话�?�?gameTime�? 表示�?��?�?*/
    private long daySocialSessionEndGameTime;
    /** �?次�?�许尝�?�?起/�?��?��?�天交流�??gameTime */
    private long daySocialNextTryGameTime;
    /** �??家贴�?�?�?�? tick 水平距离平�?��?�?距离 MoveControl �?��? */
    private double goHomeLastHorizDistSq = Double.MAX_VALUE;

    public enum WorkMode { NONE, CHOP, MINE }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Construction & Attributes
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    public PigmanEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(true);
        // +25 sanity per minute while hired (same units as MonsterSpiderEntity negative aura)
        this.sanityAura.setAuraFn((pig, observer) -> pig.isHired() ? 25.0 / 60.0 : 0.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        PigmanGroundPathNavigation nav = new PigmanGroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Brain Setup
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    public Brain<? extends LivingEntity> getBrain() {
        return super.getBrain();
    }

    @SuppressWarnings("unchecked")
    private Brain<PigmanEntity> pigBrain() {
        return (Brain<PigmanEntity>) (Brain<?>) super.getBrain();
    }

    @Override
    protected Brain<? extends LivingEntity> makeBrain(Brain.Packed packed) {
        Brain.Provider<PigmanEntity> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES, PigmanEntity::collectActivityData);
        Brain<PigmanEntity> brain = provider.makeBrain(this, packed);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        return brain;
    }

    private static List<ActivityData<PigmanEntity>> collectActivityData(PigmanEntity pigman) {
        return ImmutableList.of(
                ActivityData.create(Activity.CORE, 0, ImmutableList.of(
                        new Swim<>(0.8F),
                        InteractWithDoor.create(),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new AvoidTooClosePlayerBehavior()
                )),

                ActivityData.create(Activity.IDLE, ImmutableList.of(
                        Pair.of(0, StartAttacking.create(PigmanEntity::findPriorityAttackTarget)),
                        Pair.of(1, new GateBehavior<>(
                                ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.SHUFFLED,
                                GateBehavior.RunningPolicy.RUN_ONE,
                                ImmutableList.of(
                                        Pair.of(SetEntityLookTargetSometimes.create(EntityType.PLAYER, 8.0F, UniformInt.of(40, 80)), 1),
                                        Pair.of(SetEntityLookTargetSometimes.create(ModEntities.PIGMAN.get(), 4.0F, UniformInt.of(40, 80)), 1)
                                )
                        )),
                        Pair.of(2, GoToWantedItem.create((PigmanEntity p) -> p.getTarget() == null, 1.0F, true, 8)),
                        Pair.of(8, new PigmanOutdoorStrollBehavior()),
                        Pair.of(10, new GateBehavior<>(
                                ImmutableMap.of(),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.SHUFFLED,
                                GateBehavior.RunningPolicy.RUN_ONE,
                                ImmutableList.of(
                                        Pair.of(RandomStroll.stroll(0.8F, false), 2),
                                        Pair.of(new DoNothing(60, 120), 3)
                                )
                        ))
                )),
                ActivityData.create(
                        Activity.FIGHT,
                        0,
                        ImmutableList.of(
                                StopAttackingOutOfRange.create(),
                                new PigmanCombatBehavior(),
                                EraseMemoryIf.create(PigmanEntity::shouldClearAttackTarget, MemoryModuleType.ATTACK_TARGET)
                        ),
                        MemoryModuleType.ATTACK_TARGET
                ),
                ActivityData.create(FOLLOW_OWNER, ImmutableList.of(
                        Pair.of(0, StartAttacking.create(PigmanEntity::findPriorityAttackTarget)),
                        Pair.of(1, new FollowOwnerBehavior()),
                        Pair.of(5, new GateBehavior<>(
                                ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.SHUFFLED,
                                GateBehavior.RunningPolicy.RUN_ONE,
                                ImmutableList.of(
                                        Pair.of(SetEntityLookTargetSometimes.create(EntityType.PLAYER, 8.0F, UniformInt.of(30, 60)), 1),
                                        Pair.of(new DoNothing(20, 40), 1)
                                )
                        )),
                        Pair.of(6, GoToWantedItem.create((PigmanEntity p) -> p.getTarget() == null, 1.0F, true, 8)),
                        Pair.of(10, RandomStroll.stroll(0.6F, false))
                )),
                ActivityData.create(WORK, ImmutableList.of(
                        Pair.of(0, StartAttacking.create(PigmanEntity::findPriorityAttackTarget)),
                        Pair.of(1, new FindAndBreakBlockBehavior())
                )),
                ActivityData.create(PANIC_DARK, ImmutableList.of(
                        Pair.of(0, StartAttacking.create(PigmanEntity::findPriorityAttackTarget)),
                        Pair.of(1, new PanicDarkBehavior())
                )),
                ActivityData.create(SLEEP, ImmutableList.of(
                        Pair.of(0, new GoToHomeBehavior()),
                        Pair.of(1, new SleepBehavior())
                )),
                ActivityData.create(DAY_SOCIAL, ImmutableList.of(
                        Pair.of(0, new PigmanGoToMeetingPointBehavior()),
                        Pair.of(1, new PigmanSocializeBehavior())
                ))
        );
    }

    @Override
    protected void registerGoals() {
        // Brain system is used instead
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Server AI Tick
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.pigBrain().tick(level, this);
        this.pigBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(t -> {
            if (!this.isValidBrainAttackTarget(level, t)) {
                this.pigBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                Entity cur = this.getTarget();
                if (cur != null && cur.getUUID().equals(t.getUUID())) {
                    this.setTarget(null);
                }
            }
        });
        if (this.shouldLockMovementForAction()) {
            this.getNavigation().stop();
        }
        if (this.shouldClearWalkTargetForActionLock()) {
            this.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        this.broadcastTargetToNearbyPigmen();

        if (this.wakeFromDamageCooldownTicks > 0) {
            this.wakeFromDamageCooldownTicks--;
        }

        // �??�?? Activity priority: FIGHT > WORK > FOLLOW_OWNER > DAY_SOCIAL > SLEEP > IDLE �??�??
        boolean hired = this.entityData.get(IS_HIRED);
        boolean isDay = ConstantRealmTime.isDay(level, level.getDefaultClockTime());
        boolean isNight = ConstantRealmTime.isNight(level, level.getDefaultClockTime());
        int localLight = level.getMaxLocalRawBrightness(this.blockPosition());
        boolean panicDark = isNight && localLight < NIGHT_SLEEP_LIGHT_THRESHOLD;
        boolean hasThreat = this.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
        boolean sleeping = this.entityData.get(IS_SLEEPING);
        long gameTime = level.getGameTime();
        boolean canTryNightSleep = this.canTryNightSleep(level, gameTime);
        this.updateScaredMood(isNight, localLight, hasThreat, sleeping);

        if (isDay) {
            if (hired) {
                this.endDaySocialSession(level, false);
            } else {
                this.tickDaySocial(level, gameTime);
            }
        } else {
            this.endDaySocialSession(level, true);
        }

        ImmutableList.Builder<Activity> priorities = ImmutableList.builder();
        priorities.add(Activity.FIGHT);
        if (hired && this.workMode != WorkMode.NONE) {
            priorities.add(WORK);
        }
        if (hired) {
            priorities.add(FOLLOW_OWNER);
        }
        if (isDay && !hired && !hasThreat && this.pigBrain().hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
            priorities.add(DAY_SOCIAL);
        }
        boolean sleepActivityAtNight = false;
        if (isNight && !hasThreat) {
            if (sleeping) {
                sleepActivityAtNight = true;
            } else if (this.wantsReturnHomeForNightSleep()) {
                sleepActivityAtNight = true;
            } else {
                sleepActivityAtNight = canTryNightSleep && this.wakeFromDamageCooldownTicks <= 0;
            }
        }
        if (sleepActivityAtNight) {
            priorities.add(SLEEP);
        }
        if (panicDark && !hasThreat && this.homePos == null) {
            priorities.add(PANIC_DARK);
        }
        priorities.add(Activity.IDLE);
        this.pigBrain().setActiveActivityToFirstValid(priorities.build());

        // �??�?? Hire tick-down �??�??
        if (this.hireTicks > 0) {
            this.hireTicks--;
            if (this.hireTicks == 0) {
                this.entityData.set(IS_HIRED, false);
                this.ownerUuid = null;
                this.pendingHireSpeechAfterReceive = false;
                this.workMode = WorkMode.NONE;
                this.workTargetPos = null;
                this.workBreakProgress = 0;
            }
        }
        if (this.vegetarianAcceptCooldownTicks > 0) {
            this.vegetarianAcceptCooldownTicks--;
        }
        if (this.avoidPlayerRunTicks > 0) {
            this.avoidPlayerRunTicks--;
        }
        if (this.tooCloseSpeechCooldownTicks > 0) {
            this.tooCloseSpeechCooldownTicks--;
        }

        // �??�?? Eat ticks �??�??
        int eatTicks = this.entityData.get(EAT_TICKS);
        if (eatTicks > 0) {
            this.entityData.set(EAT_TICKS, eatTicks - 1);
            if (eatTicks - 1 <= 0) {
                this.setEating(false);
                this.setEatingItem(ItemStack.EMPTY);
            }
        }

        // �??�?? Attack ticks �??�??
        int attackTicks = this.entityData.get(ATTACK_TICKS);
        if (attackTicks > 0) {
            this.entityData.set(ATTACK_TICKS, attackTicks - 1);
        }

        // �??�??accept item �??then eat if food) �??�??
        int takeTicks = this.entityData.get(TAKE_TICKS);
        if (takeTicks > 0) {
            this.entityData.set(TAKE_TICKS, takeTicks - 1);
            if (takeTicks - 1 <= 0 && this.entityData.get(IS_EATING)) {
                this.entityData.set(EAT_TICKS, 20);
            }
        }

        // �??�?? Reject ticks �??�??
        int rejectTicks = this.entityData.get(REJECT_TICKS);
        if (rejectTicks > 0) {
            this.entityData.set(REJECT_TICKS, rejectTicks - 1);
        }

        // �??�?? Running state �??�??
        boolean shouldRun = this.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || this.avoidPlayerRunTicks > 0
                || (panicDark && !sleeping && this.homePos == null)
                || (this.wantsReturnHomeForNightSleep()
                        && !sleeping
                        && !this.isCloseEnoughToSleepAtHome(level));
        if (!shouldRun && hired && this.ownerUuid != null) {
            Player owner = level.getPlayerByUUID(this.ownerUuid);
            if (owner != null) {
                double dist = this.distanceTo(owner);
                shouldRun = dist > FOLLOW_START_DIST * 2 && dist < TELEPORT_DIST;
            }
        }
        this.entityData.set(IS_RUNNING, shouldRun);

        // �??�?? Detect owner activity for work �??�??
        if (hired && this.ownerUuid != null && this.tickCount % 20 == 0) {
            detectOwnerWorkActivity(level);
        }

        // �??�?? Speech tick-down �??�??
        int spTicks = this.entityData.get(SPEECH_TICKS);
        if (spTicks > 0) {
            this.entityData.set(SPEECH_TICKS, spTicks - 1);
            if (spTicks - 1 <= 0) {
                this.entityData.set(SPEECH_TEXT, "");
                if (this.pendingHireSpeechAfterReceive) {
                    this.pendingHireSpeechAfterReceive = false;
                    speakRandomForced("hired", 4);
                }
            }
        }
        if (this.speechCooldown > 0) this.speechCooldown--;

        // �??�?? Validate home (bed must still exist) �??�??
        if (this.homePos != null && this.tickCount % 20 == 0) {
            if (!level.getBlockState(this.homePos).is(ModBlocks.PIGMAN_BED.get())) {
                this.homePos = null;
                if (sleeping) {
                    this.setAsleep(false);
                }
            }
        }

        // �??�?? Wake up if day, hurt, or in total darkness (not when valid in-bed at home) �??�??
        if (sleeping) {
            if (ConstantRealmTime.isDay(level, level.getDefaultClockTime()) || hasThreat) {
                this.setAsleep(false);
            } else if (level.getMaxLocalRawBrightness(this.blockPosition()) < NIGHT_SLEEP_LIGHT_THRESHOLD
                    && !this.isSleepingInValidHomeBed(level)) {
                this.setAsleep(false);
            }
        }

        // �??�?? Trigger speech �??�??
        trySpeech(level);

        tryEatGroundFood(level);
        super.customServerAiStep(level);
    }

    private boolean shouldLockMovementForAction() {
        return this.entityData.get(ATTACK_TICKS) > 0
                || this.entityData.get(TAKE_TICKS) > 0
                || this.entityData.get(REJECT_TICKS) > 0
                || this.entityData.get(EAT_TICKS) > 0
                || this.entityData.get(IS_EATING);
    }

    private boolean shouldClearWalkTargetForActionLock() {
        return this.entityData.get(TAKE_TICKS) > 0
                || this.entityData.get(REJECT_TICKS) > 0
                || this.entityData.get(EAT_TICKS) > 0
                || this.entityData.get(IS_EATING);
    }

    private boolean isInItemInteractionAction() {
        return this.entityData.get(TAKE_TICKS) > 0
                || this.entityData.get(REJECT_TICKS) > 0
                || this.entityData.get(EAT_TICKS) > 0
                || this.entityData.get(IS_EATING);
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Work: Detect owner activity
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    private void detectOwnerWorkActivity(ServerLevel level) {
        Player owner = level.getPlayerByUUID(this.ownerUuid);
        if (owner == null || owner.distanceTo(this) > FIGHT_SHARED_RANGE) return;

        BlockPos destroyingPos = getPlayerBreakingBlock(owner);
        if (destroyingPos == null) return;

        BlockState state = level.getBlockState(destroyingPos);
        if (state.is(ModBlockTags.PIGMAN_CHOPPABLE)) {
            this.workMode = WorkMode.CHOP;
            if (!lastSpeechCategory.equals("work_chop")) {
                speakRandom("work_chop", 3);
            }
        } else if (state.is(ModBlockTags.PIGMAN_MINEABLE)) {
            this.workMode = WorkMode.MINE;
            if (!lastSpeechCategory.equals("work_mine")) {
                speakRandom("work_mine", 1);
            }
        }
    }

    private static BlockPos getPlayerBreakingBlock(Player player) {
        if (player.swinging && player.getMainHandItem().isCorrectToolForDrops(player.level().getBlockState(
                BlockPos.containing(player.getEyePosition().add(player.getLookAngle().scale(4.0)))))) {
            return BlockPos.containing(player.getEyePosition().add(player.getLookAngle().scale(4.0)));
        }
        return null;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Speech system
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    private static final String SP = "speech.dontstravemc.pigman.";

    private void trySpeech(ServerLevel level) {
        if (this.speechCooldown > 0) return;
        if (this.entityData.get(SPEECH_TICKS) > 0) return;
        if (this.isSleepingForSpeech()) return;

        // On fire
        if (this.isOnFire()) {
            speakRandom("on_fire", 3);
            return;
        }

        // Fighting�?�?�?�??中�?��?仍�?�可追�?��??�??对�?��?�?避�?��?�??记�?�??广�?�误�?步导�?��?��?仍�??�??�?
        Optional<LivingEntity> atk = this.pigBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (atk.isPresent() && this.isValidBrainAttackTarget(level, atk.get())) {
            if (this.random.nextFloat() < 0.05F) {
                speakRandom("fight", 3);
            }
            return;
        }

        boolean nightTime = ConstantRealmTime.isNight(level, level.getDefaultClockTime());
        if (nightTime) {
            int lightLevel = level.getMaxLocalRawBrightness(this.blockPosition());
            if (lightLevel <= 3) {
                if (this.random.nextFloat() < 0.04F) {
                    speakRandom("in_darkness", 3);
                }
                return;
            }
            if (this.random.nextFloat() < 0.01F) {
                speakRandom("night_near_fire", 5);
                return;
            }
        }

        if (ConstantRealmTime.isNight(level, level.getDefaultClockTime()) && this.homePos != null
                && !this.isCloseEnoughToSleepAtHome(level)) {
            if (this.random.nextFloat() < 0.025F && !lastSpeechCategory.equals("dusk_go_home")) {
                speakRandom("dusk_go_home", 2);
                return;
            }
        }

        // Currently eating
        if (this.entityData.get(EAT_TICKS) > 0) {
            if (this.random.nextFloat() < 0.08F) {
                speakRandom("eating", 2);
            }
            return;
        }

        // Low health / hungry
        if (this.getHealth() < this.getMaxHealth() * 0.3F) {
            if (this.random.nextFloat() < 0.03F) {
                speakRandom("find_food", 4);
            }
            return;
        }

        // Observe nearby player: inspect when idle; hired lines while employed (replaces inspect)
        Player nearestPlayer = level.getNearestPlayer(this, 8.0);
        if (nearestPlayer != null && this.distanceTo(nearestPlayer) < 6.0 && this.random.nextFloat() < 0.01F) {
            if (this.entityData.get(IS_HIRED)) {
                speakRandom("hired", 4);
            } else {
                speakRandom("inspect", 3);
            }
            return;
        }
    }

    public void speakRandom(String category, int count) {
        if (this.speechCooldown > 0) return;
        int idx = this.random.nextInt(count);
        String key = SP + category + "." + idx;
        this.setSpeechState(key, category);
    }

    public void speakRandomForced(String category, int count) {
        int idx = this.random.nextInt(count);
        String key = SP + category + "." + idx;
        this.setSpeechState(key, category);
    }

    public void speak(String translationKey) {
        if (this.speechCooldown > 0) return;
        this.setSpeechState(translationKey, translationKey);
    }

    private void setSpeechState(String speechKey, String lastCategory) {
        this.entityData.set(SPEECH_TEXT, speechKey);
        this.entityData.set(SPEECH_TICKS, SPEECH_DISPLAY_TICKS);
        this.speechCooldown = SPEECH_COOLDOWN;
        this.lastSpeechCategory = lastCategory;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Eating
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    private void tryEatGroundFood(ServerLevel level) {
        if (this.entityData.get(EAT_TICKS) > 0 || this.getTarget() != null || this.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return;
        }

        List<ItemEntity> nearby = level.getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(1.25D),
                item -> item.isAlive() && !item.hasPickUpDelay() && item.getItem().has(DataComponents.FOOD)
        );
        if (nearby.isEmpty()) return;
        this.speakRandomForced("find_food", 4);

        nearby.sort(Comparator.comparingDouble(this::distanceToSqr));
        ItemEntity item = nearby.get(0);
        ItemStack stack = item.getItem();
        if (stack.isEmpty()) return;

        ItemStack bite = stack.split(1);
        if (stack.isEmpty()) item.discard();

        FoodProperties food = bite.get(DataComponents.FOOD);
        if (food != null) {
            ItemStack remainder = bite.finishUsingItem(this.level(), this);
            this.heal(food.nutrition() * 1.5F);
            this.ate();
            if (!remainder.isEmpty()) {
                this.spawnAtLocation(level, remainder);
            }
        }

        this.setEatingItem(bite);
        this.setEating(true);
        this.entityData.set(EAT_TICKS, 20);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Target selection
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    private static Optional<? extends LivingEntity> findPriorityAttackTarget(ServerLevel level, PigmanEntity pigman) {
        LivingEntity hurtBy = pigman.pigBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).orElse(null);
        if (hurtBy == null) hurtBy = pigman.getLastHurtByMob();
        if (hurtBy != null && hurtBy.isAlive() && pigman.canAttack(hurtBy)
                && pigman.distanceTo(hurtBy) <= FIGHT_SHARED_RANGE) {
            return Optional.of(hurtBy);
        }

        LivingEntity highest = null;
        LivingEntity lowest = null;
        double bestHigh = Double.MAX_VALUE;
        double bestLow = Double.MAX_VALUE;

        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                pigman.getBoundingBox().inflate(FIGHT_SHARED_RANGE),
                entity -> entity != pigman && entity.isAlive() && pigman.canAttack(entity)
        );

        for (LivingEntity entity : nearby) {
            double dist = pigman.distanceToSqr(entity);

            boolean attacksPigman = false;
            if (entity instanceof Mob mob) {
                attacksPigman = mob.getTarget() instanceof PigmanEntity;
            }
            if (attacksPigman && dist < bestHigh) {
                bestHigh = dist;
                highest = entity;
            }

            if (entity.getType().getCategory() == MobCategory.MONSTER && dist < bestLow) {
                bestLow = dist;
                lowest = entity;
            }
        }

        if (highest != null) return Optional.of(highest);
        if (lowest != null) return Optional.of(lowest);
        return Optional.empty();
    }

    private void broadcastTargetToNearbyPigmen() {
        Optional<LivingEntity> opt = this.pigBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (opt.isEmpty()) return;

        LivingEntity target = opt.get();
        if (!target.isAlive()) return;

        List<PigmanEntity> nearby = this.level().getEntitiesOfClass(
                PigmanEntity.class,
                this.getBoundingBox().inflate(FIGHT_SHARED_RANGE),
                pig -> pig != this && pig.isAlive() && pig.distanceTo(this) <= FIGHT_SHARED_RANGE
        );

        for (PigmanEntity pig : nearby) {
            if (pig.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                continue;
            }
            if (!pig.canAttack(target) || pig.distanceTo(target) > FIGHT_SHARED_RANGE) {
                continue;
            }
            pig.pigBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            pig.setTarget(target);
        }
    }

    /** �?�??�??AI / 广�?��?��?��?�??�?ATTACK_TARGET �?�否仍�?��?��?�可追�?��??�?�?�??*/
    private boolean isValidBrainAttackTarget(ServerLevel level, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (level.getEntity(target.getUUID()) != target) {
            return false;
        }
        if (!this.canAttack(target)) {
            return false;
        }
        return this.distanceTo(target) <= FIGHT_SHARED_RANGE;
    }

    private static boolean shouldClearAttackTarget(PigmanEntity pigman) {
        Optional<LivingEntity> opt = pigman.pigBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (opt.isEmpty()) return true;
        LivingEntity target = opt.get();
        if (!(pigman.level() instanceof ServerLevel sl)) {
            return true;
        }
        return !pigman.isValidBrainAttackTarget(sl, target);
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Synched Data
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_EATING, false);
        builder.define(EATING_ITEM, ItemStack.EMPTY);
        builder.define(EAT_TICKS, 0);
        builder.define(ATTACK_TICKS, 0);
        builder.define(IS_HIRED, false);
        builder.define(IS_SLEEPING, false);
        builder.define(TAKE_TICKS, 0);
        builder.define(REJECT_TICKS, 0);
        builder.define(IS_RUNNING, false);
        builder.define(IS_SCARED, false);
        builder.define(IN_BED, false);
        builder.define(SPEECH_TEXT, "");
        builder.define(SPEECH_TICKS, 0);
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Interaction (hire via food)
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Hire / food / stack changes must run only on the server entity; hireTicks and ownerUuid are not synced.
        if (this.level().isClientSide()) {
            ItemStack preview = player.getItemInHand(hand);
            Equippable eq = preview.get(DataComponents.EQUIPPABLE);
            if (eq != null && eq.slot() == EquipmentSlot.HEAD && eq.canBeEquippedBy(this.getType().builtInRegistryHolder())) {
                return InteractionResult.SUCCESS;
            }
            if (preview.has(DataComponents.FOOD)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (this.isSleepingForSpeech()) {
            Equippable headPreview = stack.get(DataComponents.EQUIPPABLE);
            boolean canHead = headPreview != null
                    && headPreview.slot() == EquipmentSlot.HEAD
                    && headPreview.canBeEquippedBy(this.getType().builtInRegistryHolder());
            if (!stack.has(DataComponents.FOOD) && !canHead) {
                return InteractionResult.PASS;
            }
            this.wakeFromDamage(serverLevel);
        }

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null
                && equippable.slot() == EquipmentSlot.HEAD
                && equippable.canBeEquippedBy(this.getType().builtInRegistryHolder())) {
            ItemStack oldHead = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!oldHead.isEmpty()) {
                this.spawnAtLocation(serverLevel, oldHead.copy());
            }

            this.setItemSlot(EquipmentSlot.HEAD, stack.copyWithCount(1));
            this.setGuaranteedDrop(EquipmentSlot.HEAD);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            this.playSound(equippable.equipSound().value(), 1.0F, 1.0F);
            this.entityData.set(TAKE_TICKS, TAKE_ANIM_TICKS);
            speakRandom("receive_item", 2);
            return InteractionResult.SUCCESS;
        }

        FoodProperties food = stack.get(DataComponents.FOOD);
        boolean isMeatFood = stack.is(ModItemTags.MEAT_FOODS);
        boolean isVegetarianFood = stack.is(ModItemTags.VEGETARIAN_FOODS);

        if (food == null) {
            this.entityData.set(REJECT_TICKS, REJECT_ANIM_TICKS);
            return InteractionResult.SUCCESS;
        }
        if (isVegetarianFood && this.vegetarianAcceptCooldownTicks > 0) {
            this.entityData.set(REJECT_TICKS, REJECT_ANIM_TICKS);
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) stack.shrink(1);
        this.heal(2.0F);
        this.setEatingItem(stack.copyWithCount(1));
        this.setEating(true);
        this.entityData.set(TAKE_TICKS, TAKE_ANIM_TICKS);

        if (isVegetarianFood) {
            this.vegetarianAcceptCooldownTicks = VEGETARIAN_ACCEPT_COOLDOWN_TICKS;
        }

        boolean firstMeatHire = false;
        if (isMeatFood) {
            int nutrition = food.nutrition();
            int add = nutrition * HIRE_TICKS_PER_4_NUTRITION / 4;
            if (add <= 0) {
                add = HIRE_TICKS_PER_4_NUTRITION / 4;
            }
            this.hireTicks = Math.min(HIRE_MAX_TICKS, this.hireTicks + add);
            boolean wasHired = this.entityData.get(IS_HIRED);
            if (this.hireTicks > 0) {
                this.ownerUuid = player.getUUID();
                this.entityData.set(IS_HIRED, true);
                this.setPersistenceRequired();
                if (!wasHired) {
                    this.pendingHireSpeechAfterReceive = true;
                    firstMeatHire = true;
                }
            } else {
                this.entityData.set(IS_HIRED, false);
                this.ownerUuid = null;
            }
        }

        if (firstMeatHire) {
            speakRandomForced("receive_item", 2);
        } else {
            speakRandom("receive_item", 2);
        }

        return InteractionResult.SUCCESS;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Save / Load
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    public void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt(NBT_HIRE_TICKS, this.hireTicks);
        out.putInt(NBT_VEG_COOLDOWN_TICKS, this.vegetarianAcceptCooldownTicks);
        out.putInt(NBT_TOO_CLOSE_COOLDOWN_TICKS, this.tooCloseSpeechCooldownTicks);
        if (this.ownerUuid != null) out.putString("PigmanOwnerUUID", this.ownerUuid.toString());
        out.putBoolean("PigmanIsHired", this.entityData.get(IS_HIRED));
        out.putBoolean("PigmanIsSleeping", this.entityData.get(IS_SLEEPING));
        if (this.homePos != null) {
            out.putBoolean(NBT_HAS_HOME, true);
            out.putInt(NBT_HOME_X, this.homePos.getX());
            out.putInt(NBT_HOME_Y, this.homePos.getY());
            out.putInt(NBT_HOME_Z, this.homePos.getZ());
        } else {
            out.putBoolean(NBT_HAS_HOME, false);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.hireTicks = Math.max(0, Math.min(HIRE_MAX_TICKS, in.getIntOr(NBT_HIRE_TICKS, 0)));
        this.vegetarianAcceptCooldownTicks = Math.max(0, in.getIntOr(NBT_VEG_COOLDOWN_TICKS, 0));
        this.tooCloseSpeechCooldownTicks = Math.max(0, in.getIntOr(NBT_TOO_CLOSE_COOLDOWN_TICKS, 0));
        String raw = in.getStringOr("PigmanOwnerUUID", "");
        this.ownerUuid = raw.isEmpty() ? null : UUID.fromString(raw);
        boolean hired = in.getBooleanOr("PigmanIsHired", false);
        this.entityData.set(IS_HIRED, hired && this.hireTicks > 0 && this.ownerUuid != null);
        this.entityData.set(IS_SLEEPING, in.getBooleanOr("PigmanIsSleeping", false));
        if (in.getBooleanOr(NBT_HAS_HOME, false)) {
            this.homePos = new BlockPos(
                    in.getIntOr(NBT_HOME_X, 0),
                    in.getIntOr(NBT_HOME_Y, 0),
                    in.getIntOr(NBT_HOME_Z, 0)
            );
        }
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Combat
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        super.actuallyHurt(level, source, amount);

        // Wake immediately when hurt during sleep, then retaliate.
        if (this.entityData.get(IS_SLEEPING) || this.entityData.get(IN_BED)) {
            this.wakeFromDamage(level);
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living && living.isAlive() && this.canAttack(living)) {
            this.pigBrain().setMemory(MemoryModuleType.ATTACK_TARGET, living);
            this.setTarget(living);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.entityData.set(ATTACK_TICKS, ATTACK_ANIM_TICKS);
        return this.applyAttackDamage(level, target);
    }

    private boolean applyAttackDamage(ServerLevel level, Entity target) {
        this.playSound(ModSoundEvents.PIGMAN_ATTACK.get(), 1.0F, 1.0F);
        return target instanceof LivingEntity living && super.doHurtTarget(level, living);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.shouldLockMovementForAction()) {
            this.getNavigation().stop();
            Vec3 delta = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, delta.y, 0.0D);
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Sounds
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.entityData.get(IS_SLEEPING)) {
            return ModSoundEvents.PIGMAN_SLEEP.get();
        }
        return ModSoundEvents.PIGMAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.PIGMAN_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Animations (GeckoLib)
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // GeckoLib 5.5: empty timeline + transitionTicks > 0 throws in AnimationTimeline.create (NoSuchElementException).
        controllers.add(new AnimationController<>("controller", 0, state -> {
            boolean sleeping = this.entityData.get(IS_SLEEPING);

            // 1. Sleep in bed
            if (sleeping && this.entityData.get(IN_BED)) {
                return state.setAndContinue(INBED_ANIM);
            }

            // 2. Sleep on ground (sleep_pre then auto-transitions to sleep_loop)
            if (sleeping) {
                return state.setAndContinue(SLEEP_ANIM);
            }

            // 4. Attack / chop / mine
            if (this.entityData.get(ATTACK_TICKS) > 0) {
                return state.setAndContinue(ATTACK_ANIM);
            }

            // 5. Take (accept item)
            if (this.entityData.get(TAKE_TICKS) > 0) {
                return state.setAndContinue(TAKE_ANIM);
            }

            // 6. Reject (reject item)
            if (this.entityData.get(REJECT_TICKS) > 0) {
                return state.setAndContinue(REJECT_ANIM);
            }

            // 7. Eat
            if (this.entityData.get(EAT_TICKS) > 0 || this.entityData.get(IS_EATING)) {
                return state.setAndContinue(EAT_ANIM);
            }

            // 8-9. Moving: run or walk
            if (state.isMoving()) {
                this.wasMovingLastTick = true;
                if (this.entityData.get(IS_RUNNING)) {
                    return state.setAndContinue(RUN_ANIM);
                }
                return state.setAndContinue(WALK_ANIM);
            }

            // Transition from moving to idle: roll for variant
            if (this.wasMovingLastTick) {
                this.wasMovingLastTick = false;
                this.lastIdleRoll = this.getRandom().nextFloat();
            }

            boolean hired = this.entityData.get(IS_HIRED);
            boolean hasFightTarget = this.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);

            // 10. Combat idle: 50% idle_angry
            if (hasFightTarget) {
                return state.setAndContinue(lastIdleRoll < 0.5F ? IDLE_ANGRY_ANIM : IDLE_ANIM);
            }

            if (this.entityData.get(IS_SCARED)) {
                return state.setAndContinue(lastIdleRoll < 0.35F ? IDLE_SCARED_ANIM : IDLE_ANIM);
            }

            // 11. Hired time running low (< 1.5 min): 30% hungry
            if (hired && this.hireTicks > 0 && this.hireTicks < HIRE_WARNING_TICKS) {
                if (lastIdleRoll < 0.3F) {
                    return state.setAndContinue(HUNGRY_ANIM);
                }
            }

            // 12. Hired idle: 30% idle_happy
            if (hired) {
                return state.setAndContinue(lastIdleRoll < 0.3F ? IDLE_HAPPY_ANIM : IDLE_ANIM);
            }

            // 13. Default idle: 30% idle_creepy
            return state.setAndContinue(lastIdleRoll < 0.3F ? IDLE_CREEPY_ANIM : IDLE_ANIM);
        }));
    }

    /**
     * True when the main GeckoLib controller is playing the hired smile idle ({@code idle_happy}).
     * Client renderer uses this to swap the entity texture to {@code pigman_smile.png}.
     */
    public boolean isIdleHappyAnimationPlaying() {
        AnimatableManager<PigmanEntity> manager = this.getAnimatableInstanceCache().getManagerForId(this.getId());
        if (manager == null) {
            return false;
        }
        AnimationController<PigmanEntity> controller = manager.getAnimationControllers().get("controller");
        if (controller == null) {
            return false;
        }
        RawAnimation raw = controller.getCurrentRawAnimation();
        if (raw == null) {
            return false;
        }
        return raw.getAnimationStages().stream().anyMatch(s -> IDLE_HAPPY_ANIMATION_NAME.equals(s.animationName()));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Eater / Sleepable interfaces
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    @Override
    public SoundEvent getEatingSound() {
        return ModSoundEvents.PIGMAN_EAT.get();
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
    public boolean isAsleep() {
        return this.entityData.get(IS_SLEEPING);
    }

    @Override
    public void setAsleep(boolean asleep) {
        this.entityData.set(IS_SLEEPING, asleep);
        if (asleep) {
            this.getNavigation().stop();
            BlockPos below = this.blockPosition().below();
            boolean onBed = this.level().getBlockState(below).is(ModBlocks.PIGMAN_BED.get())
                    || this.level().getBlockState(this.blockPosition()).is(ModBlocks.PIGMAN_BED.get());
            this.entityData.set(IN_BED, onBed);
        } else {
            this.entityData.set(IN_BED, false);
        }
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Getters for renderer
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    public String getSpeechText() {
        return this.entityData.get(SPEECH_TEXT);
    }

    public int getSpeechTicks() {
        return this.entityData.get(SPEECH_TICKS);
    }

    public boolean isHired() {
        return this.entityData.get(IS_HIRED);
    }

    @Override
    public SanityAura<PigmanEntity> getSanityAura() {
        return this.sanityAura;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Inner Behavior classes
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    // �??�?? StopAttackingOutOfRange �??�??
    private static class StopAttackingOutOfRange {
        private static BehaviorControl<? super PigmanEntity> create() {
            return StopAttackingIfTargetInvalid.create(
                    (serverLevel, target) -> !target.isAlive() || serverLevel.getEntity(target.getUUID()) == null,
                    (serverLevel, pig, target) -> pig.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET),
                    false
            );
        }
    }

    // �??�?? PigmanCombatBehavior (hit �??evade �??approach �??hit cycle) �??�??
    private static class PigmanCombatBehavior extends Behavior<PigmanEntity> {
        private enum Phase { APPROACH, ATTACK_PAUSE, EVADE }
        private Phase phase = Phase.APPROACH;
        private int phaseTicks = 0;
        private boolean damageApplied;

        PigmanCombatBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
            ), 1, 600);
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            phase = Phase.APPROACH;
            phaseTicks = 0;
            damageApplied = false;
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            LivingEntity target = pigman.pigBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (target == null || !target.isAlive()) return;

            pigman.pigBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            double dist = pigman.distanceTo(target);
            phaseTicks++;

            switch (phase) {
                case APPROACH -> {
                    if (dist <= PIGMAN_ATTACK_REACH) {
                        pigman.swing(InteractionHand.MAIN_HAND);
                        pigman.entityData.set(ATTACK_TICKS, ATTACK_ANIM_TICKS);
                        phase = Phase.ATTACK_PAUSE;
                        phaseTicks = 0;
                        damageApplied = false;
                    } else {
                        pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET,
                                new WalkTarget(new EntityTracker(target, false), CHASE_SPEED, 0));
                    }
                }
                case ATTACK_PAUSE -> {
                    if (!damageApplied && phaseTicks >= ATTACK_DAMAGE_DELAY_TICKS) {
                        if (dist <= PIGMAN_ATTACK_REACH + 0.35D) {
                            pigman.applyAttackDamage(level, target);
                        }
                        pigman.pigBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COMBAT_EVADE_TICKS);
                        damageApplied = true;
                    }

                    if (phaseTicks >= ATTACK_EVADE_START_TICKS && pigman.entityData.get(ATTACK_TICKS) <= 0) {
                        Vec3 away = pigman.position().subtract(target.position());
                        if (away.lengthSqr() < 1.0E-6) {
                            away = new Vec3(pigman.getRandom().nextDouble() - 0.5, 0.0, pigman.getRandom().nextDouble() - 0.5);
                        }
                        Vec3 dest = pigman.position().add(away.normalize().scale(PIGMAN_EVADE_DIST));
                        pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(dest, CHASE_SPEED, 0));
                        phase = Phase.EVADE;
                        phaseTicks = 0;
                    }
                }
                case EVADE -> {
                    if (phaseTicks >= COMBAT_EVADE_TICKS || pigman.getNavigation().isDone()) {
                        phase = Phase.APPROACH;
                        phaseTicks = 0;
                    }
                }
            }
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
        }

        @Override
        protected void stop(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            damageApplied = false;
        }
    }

    // �??�?? FollowOwnerBehavior �??�??
    private static class FollowOwnerBehavior extends Behavior<PigmanEntity> {
        FollowOwnerBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
            ), 20, 100);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            return pigman.entityData.get(IS_HIRED) && pigman.ownerUuid != null;
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            followOrTeleport(level, pigman);
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            followOrTeleport(level, pigman);
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return pigman.entityData.get(IS_HIRED) && pigman.ownerUuid != null;
        }

        private void followOrTeleport(ServerLevel level, PigmanEntity pigman) {
            Player owner = level.getPlayerByUUID(pigman.ownerUuid);
            if (owner == null) return;
            if (pigman.shouldLockMovementForAction()) {
                pigman.getNavigation().stop();
                pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                return;
            }

            double dist = pigman.distanceTo(owner);

            if (dist > TELEPORT_DIST) {
                teleportNearOwner(pigman, owner);
                return;
            }

            if (dist > FOLLOW_START_DIST) {
                pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(owner.position(), FOLLOW_SPEED, 2));
            }
        }

        private void teleportNearOwner(PigmanEntity pigman, Player owner) {
            for (int i = 0; i < 10; i++) {
                double x = owner.getX() + (pigman.getRandom().nextDouble() - 0.5) * 6.0;
                double z = owner.getZ() + (pigman.getRandom().nextDouble() - 0.5) * 6.0;
                double y = owner.getY();
                BlockPos target = BlockPos.containing(x, y, z);
                if (pigman.level().getBlockState(target).isAir() && pigman.level().getBlockState(target.above()).isAir()) {
                    pigman.teleportTo(x, y, z);
                    pigman.getNavigation().stop();
                    return;
                }
            }
            pigman.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    // �??�?? FindAndBreakBlockBehavior (combined work behavior) �??�??
    private static class FindAndBreakBlockBehavior extends Behavior<PigmanEntity> {
        FindAndBreakBlockBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
            ), 20, 200);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            return pigman.workMode != WorkMode.NONE;
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            findTarget(level, pigman);
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (pigman.workTargetPos == null) {
                findTarget(level, pigman);
                return;
            }

            if (!isValidWorkTarget(level, pigman)) {
                pigman.workTargetPos = null;
                pigman.workBreakProgress = 0;
                findTarget(level, pigman);
                return;
            }

            double dist = pigman.distanceToSqr(Vec3.atCenterOf(pigman.workTargetPos));
            if (dist > 2.5 * 2.5) {
                pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(Vec3.atCenterOf(pigman.workTargetPos), FOLLOW_SPEED, 1));
                return;
            }

            pigman.getNavigation().stop();
            pigman.getLookControl().setLookAt(Vec3.atCenterOf(pigman.workTargetPos));
            pigman.workBreakProgress++;

            int breakId = pigman.getId();
            int stage = (int) ((float) pigman.workBreakProgress / WORK_BREAK_TICKS * 10.0F);
            level.destroyBlockProgress(breakId, pigman.workTargetPos, Math.min(stage, 9));

            if (pigman.workBreakProgress % 10 == 0) {
                pigman.entityData.set(ATTACK_TICKS, ATTACK_ANIM_TICKS);
            }

            if (pigman.workBreakProgress >= WORK_BREAK_TICKS) {
                level.destroyBlockProgress(breakId, pigman.workTargetPos, -1);
                level.destroyBlock(pigman.workTargetPos, true, pigman);
                pigman.workTargetPos = null;
                pigman.workBreakProgress = 0;
            }
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return pigman.workMode != WorkMode.NONE;
        }

        @Override
        protected void stop(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (pigman.workTargetPos != null) {
                level.destroyBlockProgress(pigman.getId(), pigman.workTargetPos, -1);
            }
            pigman.workBreakProgress = 0;
        }

        private void findTarget(ServerLevel level, PigmanEntity pigman) {
            TagKey<Block> tag = pigman.workMode == WorkMode.CHOP ? ModBlockTags.PIGMAN_CHOPPABLE : ModBlockTags.PIGMAN_MINEABLE;
            BlockPos center = pigman.blockPosition();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;

            for (int dx = -WORK_SEARCH_RADIUS; dx <= WORK_SEARCH_RADIUS; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -WORK_SEARCH_RADIUS; dz <= WORK_SEARCH_RADIUS; dz++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (level.getBlockState(pos).is(tag)) {
                            double d = center.distSqr(pos);
                            if (d < bestDist) {
                                bestDist = d;
                                best = pos;
                            }
                        }
                    }
                }
            }
            pigman.workTargetPos = best;
            pigman.workBreakProgress = 0;
            if (best == null) {
                pigman.workMode = WorkMode.NONE;
            }
        }

        private boolean isValidWorkTarget(ServerLevel level, PigmanEntity pigman) {
            if (pigman.workTargetPos == null) return false;
            TagKey<Block> tag = pigman.workMode == WorkMode.CHOP ? ModBlockTags.PIGMAN_CHOPPABLE : ModBlockTags.PIGMAN_MINEABLE;
            return level.getBlockState(pigman.workTargetPos).is(tag);
        }
    }

    // �??�?? AvoidTooClosePlayerBehavior (CORE) �??�??
    private static class AvoidTooClosePlayerBehavior extends Behavior<PigmanEntity> {
        AvoidTooClosePlayerBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                    MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT
            ), 20, 40);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (pigman.entityData.get(IS_SLEEPING)) return false;
            if (pigman.entityData.get(IN_BED)) return false;
            if (pigman.isInItemInteractionAction()) return false;
            Player nearest = pigman.pigBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
            return nearest != null && pigman.distanceTo(nearest) < AVOID_PLAYER_DIST;
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            Player nearest = pigman.pigBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
            if (nearest == null) return;

            Vec3 away = pigman.position().subtract(nearest.position());
            if (away.lengthSqr() < 1.0E-6) {
                away = new Vec3(pigman.getRandom().nextDouble() - 0.5, 0, pigman.getRandom().nextDouble() - 0.5);
            }
            Vec3 dest = pigman.position().add(away.normalize().scale(AVOID_PLAYER_WALK_DIST));
            pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(dest, CHASE_SPEED, 0));
            pigman.avoidPlayerRunTicks = AVOID_PLAYER_RUN_TICKS;
            if (pigman.tooCloseSpeechCooldownTicks <= 0) {
                pigman.speakRandomForced("too_close", 4);
                pigman.tooCloseSpeechCooldownTicks = TOO_CLOSE_SPEECH_COOLDOWN_TICKS;
            }
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (!pigman.getNavigation().isDone()) {
                pigman.avoidPlayerRunTicks = AVOID_PLAYER_RUN_TICKS;
            }
        }

        @Override
        protected void stop(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pigman.avoidPlayerRunTicks = 0;
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (pigman.entityData.get(IS_SLEEPING) || pigman.entityData.get(IN_BED)) {
                return false;
            }
            return !pigman.getNavigation().isDone();
        }
    }

    // �??�?? PanicDarkBehavior �??�??
    private static class PanicDarkBehavior extends Behavior<PigmanEntity> {
        private int randomMoveCooldown;

        PanicDarkBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
            ), 20, 120);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            if (!isNight(level)) return false;
            if (pigman.entityData.get(IS_SLEEPING)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            return level.getMaxLocalRawBrightness(pigman.blockPosition()) < NIGHT_SLEEP_LIGHT_THRESHOLD;
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            randomMoveCooldown = 0;
            moveTowardsLightOrWander(level, pigman);
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            moveTowardsLightOrWander(level, pigman);
            if (!pigman.getNavigation().isDone()) {
                pigman.avoidPlayerRunTicks = AVOID_PLAYER_RUN_TICKS;
            }
        }

        @Override
        protected void stop(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return checkExtraStartConditions(level, pigman);
        }

        private void moveTowardsLightOrWander(ServerLevel level, PigmanEntity pigman) {
            BlockPos lightPos = findNearbyLightSpot(level, pigman.blockPosition(), PANIC_LIGHT_SEARCH_RADIUS);
            if (lightPos != null) {
                Vec3 lightCenter = Vec3.atCenterOf(lightPos);
                if (pigman.distanceToSqr(lightCenter) > 1.5D * 1.5D) {
                    pigman.pigBrain().setMemory(
                            MemoryModuleType.WALK_TARGET,
                            new WalkTarget(lightCenter, PANIC_MOVE_SPEED, 1)
                    );
                } else {
                    pigman.getNavigation().stop();
                    pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                }
                return;
            }

            if (randomMoveCooldown > 0) {
                randomMoveCooldown--;
                return;
            }

            Vec3 randomDest = DefaultRandomPos.getPos(pigman, 12, 5);
            if (randomDest != null) {
                pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(randomDest, PANIC_MOVE_SPEED, 1));
                pigman.avoidPlayerRunTicks = AVOID_PLAYER_RUN_TICKS;
                randomMoveCooldown = 10 + pigman.getRandom().nextInt(20);
            } else {
                randomMoveCooldown = 5;
            }
        }
    }

    // �??�?? PigmanOutdoorStrollBehavior�?�?�天�?�?偏置游荡�?�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static class PigmanOutdoorStrollBehavior extends Behavior<PigmanEntity> {
        PigmanOutdoorStrollBehavior() {
            super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 80, 120);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            if (!ConstantRealmTime.isDay(level, level.getDefaultClockTime())) return false;
            if (pigman.entityData.get(IS_HIRED)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (pigman.homePos == null) return false;
            if (pigman.getRandom().nextInt(100) != 0) return false;
            return true;
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            Vec3 outdoor = findOutdoorStrollPos(level, pigman);
            if (outdoor != null) {
                pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(outdoor, 0.75F, 1));
            }
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return !pigman.getNavigation().isDone();
        }

        private static Vec3 findOutdoorStrollPos(ServerLevel level, PigmanEntity pigman) {
            Vec3 homeCenter = Vec3.atBottomCenterOf(pigman.homePos);
            for (int i = 0; i < 14; i++) {
                Vec3 v = DefaultRandomPos.getPosTowards(pigman, 14, 5, homeCenter, Math.PI / 2);
                if (v != null && level.canSeeSky(BlockPos.containing(v))) {
                    return v;
                }
            }
            return DefaultRandomPos.getPosTowards(pigman, 10, 4, homeCenter, Math.PI / 2);
        }
    }

    // �??�?? PigmanGoToMeetingPointBehavior �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static class PigmanGoToMeetingPointBehavior extends Behavior<PigmanEntity> {
        PigmanGoToMeetingPointBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                    MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT
            ), 20, 80);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            if (!ConstantRealmTime.isDay(level, level.getDefaultClockTime())) return false;
            if (pigman.entityData.get(IS_HIRED)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (pigman.daySocialSessionEndGameTime <= level.getGameTime()) return false;
            Optional<GlobalPos> opt = pigman.pigBrain().getMemory(MemoryModuleType.MEETING_POINT);
            if (opt.isEmpty()) return false;
            GlobalPos g = opt.get();
            if (g.dimension() != level.dimension()) return false;
            Vec3 dest = Vec3.atBottomCenterOf(g.pos());
            return pigman.distanceToSqr(dest) > MEETING_ARRIVE_DIST_SQR;
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            setMeetingWalkTarget(level, pigman);
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (pigman.tickCount % 40 == 0) {
                setMeetingWalkTarget(level, pigman);
            }
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (!ConstantRealmTime.isDay(level, level.getDefaultClockTime())) return false;
            if (pigman.entityData.get(IS_HIRED)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (pigman.daySocialSessionEndGameTime <= level.getGameTime()) return false;
            Optional<GlobalPos> opt = pigman.pigBrain().getMemory(MemoryModuleType.MEETING_POINT);
            if (opt.isEmpty()) return false;
            GlobalPos g = opt.get();
            if (g.dimension() != level.dimension()) return false;
            Vec3 dest = Vec3.atBottomCenterOf(g.pos());
            return pigman.distanceToSqr(dest) > 4.0D;
        }

        private static Vec3 meetingDestination(ServerLevel level, PigmanEntity pigman) {
            return pigman.pigBrain().getMemory(MemoryModuleType.MEETING_POINT)
                    .filter(g -> g.dimension() == level.dimension())
                    .map(g -> Vec3.atBottomCenterOf(g.pos()))
                    .orElse(null);
        }

        private static void setMeetingWalkTarget(ServerLevel level, PigmanEntity pigman) {
            Vec3 dest = meetingDestination(level, pigman);
            if (dest != null) {
                pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(dest, DAY_GO_MEETING_SPEED, 0));
            }
        }
    }

    // �??�?? PigmanSocializeBehavior �??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??�??
    private static class PigmanSocializeBehavior extends Behavior<PigmanEntity> {
        PigmanSocializeBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                    MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT
            ), 20, 80);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            if (!ConstantRealmTime.isDay(level, level.getDefaultClockTime())) return false;
            if (pigman.entityData.get(IS_HIRED)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (pigman.daySocialSessionEndGameTime <= level.getGameTime()) return false;
            Optional<GlobalPos> opt = pigman.pigBrain().getMemory(MemoryModuleType.MEETING_POINT);
            if (opt.isEmpty()) return false;
            GlobalPos g = opt.get();
            if (g.dimension() != level.dimension()) return false;
            Vec3 center = Vec3.atBottomCenterOf(g.pos());
            if (pigman.distanceToSqr(center) > 144.0D) return false;
            List<PigmanEntity> nearby = level.getEntitiesOfClass(
                    PigmanEntity.class,
                    pigman.getBoundingBox().inflate(8.0D),
                    p -> p != pigman && p.isAlive() && !p.entityData.get(IS_HIRED)
            );
            return !nearby.isEmpty();
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pickSocialTarget(level, pigman);
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (pigman.tickCount % 25 == 0) {
                pickSocialTarget(level, pigman);
            }
            if (pigman.tickCount % 45 == 0 && pigman.getRandom().nextFloat() < 0.12F) {
                pigman.playSound(ModSoundEvents.PIGMAN_AMBIENT.get(), 0.75F, 0.85F + pigman.getRandom().nextFloat() * 0.25F);
            }
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return checkExtraStartConditions(level, pigman);
        }

        private static void pickSocialTarget(ServerLevel level, PigmanEntity pigman) {
            List<PigmanEntity> nearby = level.getEntitiesOfClass(
                    PigmanEntity.class,
                    pigman.getBoundingBox().inflate(8.0D),
                    p -> p != pigman && p.isAlive()
            );
            if (nearby.isEmpty()) return;
            nearby.sort(Comparator.comparingDouble(pigman::distanceToSqr));
            pigman.pigBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(nearby.get(0), true));
        }
    }

    // �??�?? GoToHomeBehavior �??�??
    private static class GoToHomeBehavior extends Behavior<PigmanEntity> {
        private int repathCooldown;

        GoToHomeBehavior() {
            super(ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
            ), 20, 100);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            return isNight(level)
                    && !pigman.entityData.get(IS_SLEEPING)
                    && !pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && pigman.homePos != null
                    && level.getBlockState(pigman.homePos).is(ModBlocks.PIGMAN_BED.get());
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            this.repathCooldown = 0;
            pigman.resetGoHomePathingState();
            setBedWalkTarget(level, pigman);
        }

        @Override
        protected void stop(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pigman.resetGoHomePathingState();
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (pigman.homePos == null || pigman.entityData.get(IS_SLEEPING)) {
                return;
            }
            if (!level.getBlockState(pigman.homePos).is(ModBlocks.PIGMAN_BED.get())) {
                return;
            }
            Vec3 dest = bedWalkDestination(level, pigman);
            double dx = pigman.getX() - dest.x;
            double dz = pigman.getZ() - dest.z;
            double horizDistSqr = dx * dx + dz * dz;
            double snapBodySq = Math.pow(pigman.getBbWidth() * 0.5 + 0.6, 2);

            if (horizDistSqr <= snapBodySq || horizDistSqr <= GO_HOME_SNAP_HORIZONTAL_DIST_SQ) {
                pigman.getNavigation().stop();
                pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                pigman.trySleepOnPigmanBed(level, pigman.homePos);
                return;
            }

            boolean navIdle = pigman.getNavigation().isDone() || pigman.getNavigation().isStuck();
            if (horizDistSqr <= GO_HOME_MOVE_CONTROL_MAX_DIST_SQ
                    && navIdle
                    && !closedInteractableDoorBlocksPath(level, pigman, dest)) {
                pigman.getNavigation().stop();
                pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                pigman.getMoveControl().setWantedPosition(dest.x, dest.y, dest.z, HOME_RETURN_RUN_SPEED);
                pigman.goHomeLastHorizDistSq = horizDistSqr;
                return;
            }

            pigman.goHomeLastHorizDistSq = horizDistSqr;
            if (pigman.tickCount % 40 == 0) {
                setBedWalkTarget(level, pigman);
            }
            boolean navDone = pigman.getNavigation().isDone();
            if (navDone && horizDistSqr > GO_HOME_SNAP_HORIZONTAL_DIST_SQ) {
                if (--this.repathCooldown <= 0) {
                    this.repathCooldown = 20;
                    setBedWalkTarget(level, pigman);
                }
            }
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            return isNight(level)
                    && !pigman.entityData.get(IS_SLEEPING)
                    && pigman.homePos != null
                    && level.getBlockState(pigman.homePos).is(ModBlocks.PIGMAN_BED.get())
                    && !pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
        }

        private static Vec3 bedWalkDestination(ServerLevel level, PigmanEntity pigman) {
            BlockPos foot = pigman.homePos;
            Vec3 side = PigmanHouseHelper.findSpawnBesideBed(level, foot);
            return side != null ? side : Vec3.atBottomCenterOf(foot);
        }

        private static void setBedWalkTarget(ServerLevel level, PigmanEntity pigman) {
            Vec3 dest = bedWalkDestination(level, pigman);
            pigman.pigBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(dest, HOME_RETURN_RUN_SPEED, 2));
        }
    }

    // �??�?? SleepBehavior �??�??
    private static class SleepBehavior extends Behavior<PigmanEntity> {
        SleepBehavior() {
            super(ImmutableMap.of(), 20, 600);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, PigmanEntity pigman) {
            if (!isNight(level)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (pigman.wakeFromDamageCooldownTicks > 0) return false;
            if (pigman.entityData.get(IS_SLEEPING)) return true;
            if (pigman.homePos != null) return false;
            if (!pigman.canTryNightSleep(level, level.getGameTime())) return false;
            return hasEnoughLight(level, pigman);
        }

        @Override
        protected void start(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (!pigman.entityData.get(IS_SLEEPING)) {
                pigman.setAsleep(true);
            }
        }

        @Override
        protected void tick(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pigman.getNavigation().stop();
            pigman.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }

        @Override
        protected boolean canStillUse(ServerLevel level, PigmanEntity pigman, long gameTime) {
            if (!isNight(level)) return false;
            if (pigman.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
            if (!pigman.entityData.get(IS_SLEEPING)) return false;
            return hasEnoughLight(level, pigman);
        }

        @Override
        protected void stop(ServerLevel level, PigmanEntity pigman, long gameTime) {
            pigman.setAsleep(false);
        }

        private static boolean hasEnoughLight(ServerLevel level, PigmanEntity pigman) {
            if (pigman.entityData.get(IN_BED)
                    && pigman.homePos != null
                    && level.getBlockState(pigman.homePos).is(ModBlocks.PIGMAN_BED.get())) {
                return true;
            }
            return level.getMaxLocalRawBrightness(pigman.blockPosition()) >= NIGHT_SLEEP_LIGHT_THRESHOLD;
        }
    }

    private boolean canTryNightSleep(ServerLevel level, long gameTime) {
        if (!isNight(level)) {
            this.sleepRollPassed = false;
            this.nextSleepCheckGameTime = gameTime;
            return false;
        }
        if (this.entityData.get(IS_SLEEPING)) {
            return true;
        }
        if (this.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            this.sleepRollPassed = false;
            return false;
        }

        if (this.nextSleepCheckGameTime < 0L || gameTime >= this.nextSleepCheckGameTime) {
            this.nextSleepCheckGameTime = gameTime + NIGHT_SLEEP_CHECK_INTERVAL_TICKS;
            int light = level.getMaxLocalRawBrightness(this.blockPosition());
            this.sleepRollPassed = light >= NIGHT_SLEEP_LIGHT_THRESHOLD
                    && this.getRandom().nextFloat() < NIGHT_SLEEP_CHANCE;
        }
        return this.sleepRollPassed;
    }

    private void updateScaredMood(boolean isNight, int localLight, boolean hasThreat, boolean sleeping) {
        if (!isNight || hasThreat || sleeping) {
            this.scaredMoodTicks = 0;
            this.entityData.set(IS_SCARED, false);
            return;
        }

        if (this.homePos != null) {
            this.scaredMoodTicks = 0;
            this.entityData.set(IS_SCARED, false);
            return;
        }

        if (localLight < NIGHT_SLEEP_LIGHT_THRESHOLD) {
            this.scaredMoodTicks = SCARED_MOOD_TICKS;
        } else if (this.scaredMoodTicks > 0) {
            this.scaredMoodTicks--;
        }
        this.entityData.set(IS_SCARED, this.scaredMoodTicks > 0);
    }

    private static BlockPos findNearbyLightSpot(ServerLevel level, BlockPos center, int radius) {
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getMaxLocalRawBrightness(pos) < NIGHT_SLEEP_LIGHT_THRESHOLD) continue;
                    if (!level.getBlockState(pos).isAir()) continue;
                    if (!level.getBlockState(pos.above()).isAir()) continue;

                    double dist = center.distSqr(pos);
                    if (dist < bestDistance) {
                        bestDistance = dist;
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private static boolean isNight(ServerLevel level) {
        return ConstantRealmTime.isNight(level, level.getDefaultClockTime());
    }

    /** Return to bound bed at night when not fighting (caller gates with isNight && !hasThreat). */
    private boolean wantsReturnHomeForNightSleep() {
        return this.homePos != null && !this.pigBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    /** �?GoToHomeBehavior 中�??�?��?�?�睡�?��?��?�?�?��??*/
    private boolean isCloseEnoughToSleepAtHome(ServerLevel level) {
        if (this.homePos == null || !level.getBlockState(this.homePos).is(ModBlocks.PIGMAN_BED.get())) {
            return true;
        }
        Vec3 side = PigmanHouseHelper.findSpawnBesideBed(level, this.homePos);
        Vec3 dest = side != null ? side : Vec3.atBottomCenterOf(this.homePos);
        double dx = this.getX() - dest.x;
        double dz = this.getZ() - dest.z;
        double horizDistSqr = dx * dx + dz * dz;
        return horizDistSqr < GO_HOME_SNAP_HORIZONTAL_DIST_SQ;
    }

    private void resetGoHomePathingState() {
        this.goHomeLastHorizDistSq = Double.MAX_VALUE;
    }

    private void tickDaySocial(ServerLevel level, long gameTime) {
        if (this.daySocialSessionEndGameTime > 0L && gameTime >= this.daySocialSessionEndGameTime) {
            this.endDaySocialSession(level, false);
        }
        if (this.pigBrain().hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
            return;
        }
        if (this.daySocialNextTryGameTime == 0L) {
            this.daySocialNextTryGameTime = gameTime + 2000 + this.random.nextInt(6000);
            return;
        }
        if (gameTime < this.daySocialNextTryGameTime) {
            return;
        }
        if (this.random.nextInt(500) != 0) {
            return;
        }
        this.tryBeginDaySocialSession(level, gameTime);
    }

    private void tryBeginDaySocialSession(ServerLevel level, long gameTime) {
        BlockPos anchor = this.homePos != null ? this.homePos : this.blockPosition();
        BlockPos bell = findNearestBell(level, anchor, 40);
        BlockPos meet = bell != null ? bell : computeFallbackMeetingPoint(level, anchor);

        int activeNear = 0;
        AABB countBox = new AABB(meet).inflate(10.0D);
        for (PigmanEntity p : level.getEntitiesOfClass(PigmanEntity.class, countBox, LivingEntity::isAlive)) {
            if (p.entityData.get(IS_HIRED)) {
                continue;
            }
            if (p.daySocialSessionEndGameTime <= gameTime) {
                continue;
            }
            Optional<GlobalPos> mem = p.pigBrain().getMemory(MemoryModuleType.MEETING_POINT);
            if (mem.isEmpty()) {
                continue;
            }
            if (mem.get().pos().distSqr(meet) > 36) {
                continue;
            }
            activeNear++;
        }
        if (activeNear >= 3) {
            this.daySocialNextTryGameTime = gameTime + 300 + this.random.nextInt(500);
            return;
        }

        this.pigBrain().setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(level.dimension(), meet));
        this.daySocialSessionEndGameTime = gameTime + 120 + this.random.nextInt(100);
        this.daySocialNextTryGameTime = gameTime + 8000 + this.random.nextInt(12000);
    }

    private void endDaySocialSession(ServerLevel level, boolean nightTransition) {
        this.pigBrain().eraseMemory(MemoryModuleType.MEETING_POINT);
        this.daySocialSessionEndGameTime = 0L;
        if (nightTransition) {
            this.daySocialNextTryGameTime = 0L;
        } else if (this.isAlive()) {
            this.daySocialNextTryGameTime = level.getGameTime() + 5000 + this.random.nextInt(12000);
        }
    }

    private boolean isSleepingForSpeech() {
        return this.entityData.get(IS_SLEEPING) || this.entityData.get(IN_BED);
    }

    private void wakeFromDamage(ServerLevel level) {
        this.wakeFromDamageCooldownTicks = WAKE_FROM_DAMAGE_COOLDOWN_TICKS;
        this.getNavigation().stop();
        this.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.setAsleep(false);
    }

    private static BlockPos findNearestBell(ServerLevel level, BlockPos center, int radius) {
        int minY = Math.max(level.getMinY() + 1, center.getY() - 6);
        int maxY = Math.min(level.getMaxY() - 2, center.getY() + 10);
        for (int dist = 0; dist <= radius; dist += 2) {
            for (int dx = -dist; dx <= dist; dx += 2) {
                for (int dz = -dist; dz <= dist; dz += 2) {
                    if (Math.abs(dx) != dist && Math.abs(dz) != dist && dist != 0) {
                        continue;
                    }
                    for (int y = maxY; y >= minY; y--) {
                        BlockPos p = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                        if (level.getBlockState(p).is(Blocks.BELL)) {
                            return p;
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockPos computeFallbackMeetingPoint(ServerLevel level, BlockPos anchor) {
        AABB box = new AABB(anchor).inflate(48.0D, 20.0D, 48.0D);
        List<PigmanEntity> list = level.getEntitiesOfClass(PigmanEntity.class, box, LivingEntity::isAlive);
        if (list.size() >= 2) {
            double x = 0.0D;
            double y = 0.0D;
            double z = 0.0D;
            for (PigmanEntity p : list) {
                x += p.getX();
                y += p.getY();
                z += p.getZ();
            }
            int n = list.size();
            return BlockPos.containing(x / n, y / n, z / n);
        }
        return anchor;
    }

    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??    // Home pos helper
    // �?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��?��??
    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }

    /** True when in bed at the bound home foot so low local light should not force wake. */
    private boolean isSleepingInValidHomeBed(ServerLevel level) {
        if (!this.entityData.get(IN_BED) || this.homePos == null) {
            return false;
        }
        return level.getBlockState(this.homePos).is(ModBlocks.PIGMAN_BED.get());
    }

    private static boolean closedInteractableDoorBlocksPath(ServerLevel level, PigmanEntity pigman, Vec3 dest) {
        Vec3 from = pigman.position();
        double dx = dest.x - from.x;
        double dz = dest.z - from.z;
        double horizLen = Math.sqrt(dx * dx + dz * dz);
        if (horizLen < 1.0e-4) {
            return false;
        }
        double ix = dx / horizLen;
        double iz = dz / horizLen;
        double baseY = from.y;
        for (double t = 0; t <= horizLen; t += 0.35) {
            double x = from.x + ix * t;
            double z = from.z + iz * t;
            for (int dy = 0; dy <= 2; dy++) {
                BlockPos pos = BlockPos.containing(x, baseY + dy, z);
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockTags.MOB_INTERACTABLE_DOORS) && state.getBlock() instanceof DoorBlock doorBlock
                        && !doorBlock.isOpen(state)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 沿�?��??�??�?��?��?��?�?�?�?�位置�??�?�?�?�?�对仍�??�?��??�?�??�?靠�?�?�??�??�?�补�?��?避�??snap �?�?�?�??�??3 格距离�??�?��?�??�?�??�??�??却不�?��??     */
    private void closeBrainDoorsBeforeBedSnap(ServerLevel level, BlockPos foot) {
        Optional<Set<GlobalPos>> doorsOpt = this.pigBrain().getMemory(MemoryModuleType.DOORS_TO_CLOSE);
        if (doorsOpt.isEmpty()) {
            return;
        }
        Set<GlobalPos> doors = doorsOpt.get();
        if (doors.isEmpty()) {
            this.pigBrain().eraseMemory(MemoryModuleType.DOORS_TO_CLOSE);
            return;
        }
        Optional<List<LivingEntity>> nearest = this.pigBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough(level, this, null, null, doors, nearest);
        Vec3 bedCenter = Vec3.atBottomCenterOf(foot);
        Iterator<GlobalPos> it = doors.iterator();
        while (it.hasNext()) {
            GlobalPos g = it.next();
            if (g.dimension() != level.dimension()) {
                it.remove();
                continue;
            }
            BlockPos p = g.pos();
            if (!p.closerToCenterThan(bedCenter, SLEEP_DOOR_CLOSE_MAX_DIST_TO_FOOT)) {
                continue;
            }
            BlockState st = level.getBlockState(p);
            if (!st.is(BlockTags.MOB_INTERACTABLE_DOORS, s -> s.getBlock() instanceof DoorBlock)) {
                it.remove();
                continue;
            }
            DoorBlock doorBlock = (DoorBlock) st.getBlock();
            if (!doorBlock.isOpen(st)) {
                it.remove();
                continue;
            }
            if (nearest.isPresent() && areOtherPigmenCrowdingDoor(this, p, nearest.get())) {
                continue;
            }
            doorBlock.setOpen(this, level, st, p, false);
            it.remove();
        }
        this.pigBrain().eraseMemory(MemoryModuleType.DOORS_TO_CLOSE);
    }

    private static boolean areOtherPigmenCrowdingDoor(PigmanEntity self, BlockPos doorPos, List<LivingEntity> nearby) {
        for (LivingEntity other : nearby) {
            if (other == self || other.getType() != ModEntities.PIGMAN.get() || !other.isAlive()) {
                continue;
            }
            if (!doorPos.closerToCenterThan(other.position(), 2.0D)) {
                continue;
            }
            Optional<Path> pathOpt = other.getBrain().getMemory(MemoryModuleType.PATH);
            if (pathOpt.isEmpty()) {
                continue;
            }
            Path path = pathOpt.get();
            if (path.isDone()) {
                continue;
            }
            Node prev = path.getPreviousNode();
            Node next = path.getNextNode();
            if (prev != null && next != null
                    && (doorPos.equals(prev.asBlockPos()) || doorPos.equals(next.asBlockPos()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Snap onto the pigman bed foot so {@link #setAsleep(boolean)} sets {@link #IN_BED} and the client plays {@code inbed}.
     */
    private void trySleepOnPigmanBed(ServerLevel level, BlockPos foot) {
        if (foot == null) {
            return;
        }
        BlockState st = level.getBlockState(foot);
        if (!st.is(ModBlocks.PIGMAN_BED.get()) || !st.hasProperty(BedBlock.PART) || st.getValue(BedBlock.PART) != BedPart.FOOT) {
            return;
        }
        this.closeBrainDoorsBeforeBedSnap(level, foot);
        Direction facing = st.getValue(BedBlock.FACING);
        Vec3 center = Vec3.atBottomCenterOf(foot);
        // Air cell above foot: blockPosition().below() is the bed block for IN_BED.
        this.setPos(center.x, foot.getY() + 1.0D, center.z);
        float yaw = facing.toYRot();
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        this.pigBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.setAsleep(true);
        if (!this.entityData.get(IN_BED)) {
            this.entityData.set(IN_BED, true);
        }
    }

    public BlockPos getHomePos() {
        return this.homePos;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
    }
}
