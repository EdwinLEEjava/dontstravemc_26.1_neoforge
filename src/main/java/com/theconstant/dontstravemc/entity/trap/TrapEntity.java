package com.theconstant.dontstravemc.entity.trap;

import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
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

import java.util.List;

public class TrapEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private static final EntityDataAccessor<Boolean> IS_TRIGGERED = SynchedEntityData.defineId(TrapEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> BAIT_ITEM = SynchedEntityData.defineId(TrapEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> USES_LEFT = SynchedEntityData.defineId(TrapEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BAIT_CONSUMED = SynchedEntityData.defineId(TrapEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_PLAYED_TRIGGER_PRE = SynchedEntityData.defineId(TrapEntity.class, EntityDataSerializers.BOOLEAN);
    
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("trap_idle");
    private static final RawAnimation TRIGGER_PRE = RawAnimation.begin().thenPlay("trap_pre");
    private static final RawAnimation TRIGGER_LOOP = RawAnimation.begin().thenLoop("trap_loop");
    private static final RawAnimation TRIGGER_PRE_HOLD = RawAnimation.begin().thenPlayAndHold("trap_pre");

    private static final int MAX_USES = 8;
    private static final int BAIT_ATTRACT_INTERVAL = 20; // 每 20 tick 刷新一次诱饵吸引
    private int baitAttractTimer = 0;

    private CompoundTag capturedEntityData;

    public TrapEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.setBoundingBox(new AABB(
            this.getX() - 0.4, this.getY(), this.getZ() - 0.4,
            this.getX() + 0.4, this.getY() + 0.5, this.getZ() + 0.4
        ));
    }

    /**
     * 朝向指定位置，类似原版 Minecraft 的方向判定
     */
    public void lookAt(Vec3 targetPos) {
        double dx = targetPos.x - this.getX();
        double dy = targetPos.y - (this.getY() + this.getEyeHeight());
        double dz = targetPos.z - this.getZ();
        
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        
        // 计算水平旋转角度（yaw）
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        
        // 计算垂直旋转角度（pitch）
        float pitch = (float) (-(Math.atan2(dy, horizontalDistance) * 180.0 / Math.PI));
        
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_TRIGGERED, false);
        builder.define(BAIT_ITEM, ItemStack.EMPTY);
        builder.define(USES_LEFT, MAX_USES);
        builder.define(BAIT_CONSUMED, false);
        builder.define(HAS_PLAYED_TRIGGER_PRE, false);
    }
    
    @Override
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.refreshDimensions();
        this.setBoundingBox(new AABB(
            x - 0.4, y, z - 0.4,
            x + 0.4, y + 0.5, z + 0.4
        ));
    }

    @Override
    public void tick() {
        super.tick();

        // 应用重力和碰撞检测（super.tick() 已经调用过 move，这里只修改 deltaMovement，由原版系统处理实际移动）
        if (!this.level().isClientSide()) {
            BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.1, this.getZ());
            boolean hasSupport = this.level().getBlockState(below).isSolid();

            if (!hasSupport) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y - 0.08, motion.z);

                // 如果掉落速度过快，破坏陷阱
                if (this.getDeltaMovement().y < -2.0) {
                    this.dropAsItem((ServerLevel) this.level());
                    this.discard();
                    return;
                }
            } else {
                Vec3 motion = this.getDeltaMovement();
                if (motion.y < 0) {
                    // 停止垂直移动，减少水平滑动
                    this.setDeltaMovement(motion.x * 0.8, 0, motion.z * 0.8);
                }
            }
        }
        
        if (!this.level().isClientSide() && !this.isTriggered()) {
            AABB captureBox = this.getBoundingBox().inflate(0.8);
            List<LivingEntity> nearbyForCapture = this.level().getEntitiesOfClass(LivingEntity.class, captureBox, this::canCapture);
            
            if (!nearbyForCapture.isEmpty()) {
                LivingEntity closest = nearbyForCapture.get(0);
                double closestDist = closest.distanceTo(this);
                for (LivingEntity entity : nearbyForCapture) {
                    double dist = entity.distanceTo(this);
                    if (dist < closestDist) {
                        closest = entity;
                        closestDist = dist;
                    }
                }
                this.captureEntity(closest);
                return;
            }
            
            // 如果有诱饵，检查是否有生物靠近消耗诱饵
            if (!this.getBaitItem().isEmpty()) {
                // 先检查是否有不可捕获的生物直接踩上诱饵范围（每 tick 检查，响应及时）
                AABB baitBox = this.getBoundingBox().inflate(0.8);
                List<LivingEntity> nearbyForBait = this.level().getEntitiesOfClass(LivingEntity.class, baitBox,
                    entity -> !this.canCapture(entity) && this.isAttractedByBait(entity));

                if (!nearbyForBait.isEmpty()) {
                    this.triggerBaitConsumed();
                    return;
                }

                // 诱饵吸引导航每 20 tick 刷新一次，避免干扰生物自身 AI
                baitAttractTimer++;
                if (baitAttractTimer >= BAIT_ATTRACT_INTERVAL) {
                    baitAttractTimer = 0;

                    AABB attractBox = this.getBoundingBox().inflate(8.0);
                    List<LivingEntity> nearbyForAttract = this.level().getEntitiesOfClass(LivingEntity.class, attractBox,
                        entity -> this.isAttractedByBait(entity));

                    for (LivingEntity target : nearbyForAttract) {
                        if (target instanceof PathfinderMob mob) {
                            // 仅在该生物当前没有更高优先级导航目标时才导引（不强制覆盖）
                            if (!mob.getNavigation().isInProgress()) {
                                mob.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 0.8);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void dropAsItem(ServerLevel level) {
        int usesLeft = this.getUsesLeft();
        if (usesLeft > 0) {
            ItemStack trapItem = new ItemStack(com.theconstant.dontstravemc.item.ModItems.TRAP.get());
            int damage = MAX_USES - usesLeft;
            trapItem.setDamageValue(damage);
            this.spawnAtLocation(level, trapItem);
        }
        
        if (this.isTriggered() && this.capturedEntityData != null) {
            ItemStack spawnEgg = this.createSpawnEggFromEntity();
            if (!spawnEgg.isEmpty()) {
                this.spawnAtLocation(level, spawnEgg);
            }
        }
        
        if (!this.getBaitItem().isEmpty()) {
            this.spawnAtLocation(level, this.getBaitItem().copy());
        }
    }

    private boolean canCapture(LivingEntity entity) {
        if (entity instanceof Player) return false;
        if (this.isTriggered()) return false;

        // 所有幼年生物都可捕获
        if (entity.isBaby()) return true;

        EntityType<?> type = entity.getType();
        return type == net.minecraft.world.entity.EntityType.FROG
            || type == net.minecraft.world.entity.EntityType.AXOLOTL
            || type == net.minecraft.world.entity.EntityType.ARMADILLO
            || type == net.minecraft.world.entity.EntityType.CHICKEN
            || type == net.minecraft.world.entity.EntityType.CAT
            || type == net.minecraft.world.entity.EntityType.ENDERMITE
            || type == net.minecraft.world.entity.EntityType.SILVERFISH
            || type == net.minecraft.world.entity.EntityType.RABBIT
            || type == com.theconstant.dontstravemc.entity.ModEntities.MONSTER_SPIDER.get()
            || type == com.theconstant.dontstravemc.entity.ModEntities.RABBIT.get();
    }
    
    /**
     * 检查生物是否会被当前诱饵吸引
     */
    private boolean isAttractedByBait(LivingEntity entity) {
        ItemStack bait = this.getBaitItem();
        if (bait.isEmpty()) return false;

        // 原版 Animal：使用 isFood() 判断
        if (entity instanceof Animal animal) {
            return animal.isFood(bait);
        }

        // 自定义兔子：直接调用 isFood()
        if (entity instanceof com.theconstant.dontstravemc.entity.animal.RabbitEntity rabbit) {
            return rabbit.isFood(bait);
        }

        // 自定义蜘蛛：直接调用 isFood()
        if (entity instanceof com.theconstant.dontstravemc.entity.monster.MonsterSpiderEntity spider) {
            return spider.isFood(bait);
        }

        return false;
    }

    private void captureEntity(LivingEntity entity) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 只保存必要字段：实体类型ID、年龄、变种
        ProblemReporter.Collector problemCollector = new ProblemReporter.Collector();
        TagValueOutput fullOutput =
            TagValueOutput.createWithContext(problemCollector, serverLevel.registryAccess());
        entity.saveWithoutId(fullOutput);
        CompoundTag fullTag = fullOutput.buildResult();

        // 构建最小化存储 tag
        CompoundTag minimal = new CompoundTag();
        minimal.putString("id", EntityType.getKey(entity.getType()).toString());

        // 年龄（AgeableMob 子类通用，负数表示幼年）
        if (entity instanceof net.minecraft.world.entity.AgeableMob) {
            minimal.putInt("Age", fullTag.getInt("Age").orElse(0));
        }

        // 变种 - Cat 等使用 VariantUtils 写入 "variant" (Identifier string)
        fullTag.getString("variant").ifPresent(v -> minimal.putString("variant", v));

        // 变种 - 原版 Rabbit 使用 "RabbitType" (int)
        fullTag.getInt("RabbitType").ifPresent(v -> minimal.putInt("RabbitType", v));

        this.capturedEntityData = minimal;
        entity.discard();
        this.setBaitItem(ItemStack.EMPTY);
        this.setTriggered(true);
        this.entityData.set(HAS_PLAYED_TRIGGER_PRE, false);
        
        // 减少耐久度
        int usesLeft = this.getUsesLeft();
        if (usesLeft > 0) {
            this.setUsesLeft(usesLeft - 1);
            
            // 如果耐久度用完，破坏陷阱
            if (usesLeft - 1 <= 0) {
                this.dropAsItem(serverLevel);
                this.discard();
            }
        }
    }
    
    /**
     * 诱饵被不可捕获的生物消耗，陷阱失效
     */
    private void triggerBaitConsumed() {
        if (this.level().isClientSide()) return;

        // 清空诱饵
        this.setBaitItem(ItemStack.EMPTY);

        // 标记为诱饵被消耗（用于动画判断）
        this.entityData.set(BAIT_CONSUMED, true);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        ItemStack heldItem = player.getItemInHand(hand);
        
        if (!this.level().isClientSide()) {
            if (this.isTriggered()) {
                return InteractionResult.PASS;
            }
            
            if (!heldItem.isEmpty() && this.getBaitItem().isEmpty()) {
                ItemStack bait = heldItem.copy();
                bait.setCount(1);
                this.setBaitItem(bait);
                
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            // 播放破坏音效（类似船或矿车）
            this.playSound(net.minecraft.sounds.SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
            
            // 1. 优先尝试收回陷阱到背包
            int usesLeft = this.getUsesLeft();
            if (usesLeft > 0) {
                ItemStack trapItem = new ItemStack(com.theconstant.dontstravemc.item.ModItems.TRAP.get());
                int damage = MAX_USES - usesLeft;
                trapItem.setDamageValue(damage);

                if (!player.getInventory().add(trapItem)) {
                    // 背包满了，掉落陷阱
                    this.spawnAtLocation(level, trapItem);
                }
            }
            
            // 2. 尝试收回捕获的生物
            if (this.isTriggered() && this.capturedEntityData != null) {
                ItemStack spawnEgg = this.createSpawnEggFromEntity();
                if (!spawnEgg.isEmpty()) {
                    if (!player.getInventory().add(spawnEgg)) {
                        // 背包满了，掉落生物蛋
                        this.spawnAtLocation(level, spawnEgg);
                    }
                }
            }

            // 3. 尝试收回诱饵
            if (!this.getBaitItem().isEmpty()) {
                ItemStack bait = this.getBaitItem().copy();
                if (!player.getInventory().add(bait)) {
                    this.spawnAtLocation(level, bait);
                }
            }
            
            this.discard();
            return true;
        }
        return false;
    }

    private ItemStack createSpawnEggFromEntity() {
        if (this.capturedEntityData == null) return ItemStack.EMPTY;
        
        // CompoundTag.getString() 返回 Optional<String>
        String entityId = this.capturedEntityData.getString("id").orElse("");
        if (entityId.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack spawnEgg = switch (entityId) {
            case "dontstravemc:monster_spider" -> new ItemStack(ModItems.MONSTER_SPIDER_SPAWN_EGG.get());
            case "dontstravemc:rabbit" -> new ItemStack(ModItems.DS_RABBIT_SPAWN_EGG.get());
            case "dontstravemc:butterfly" -> new ItemStack(ModItems.BUTTERFLY_SPAWN_EGG.get());
            case "minecraft:chicken" -> new ItemStack(Items.CHICKEN_SPAWN_EGG);
            case "minecraft:cat" -> new ItemStack(Items.CAT_SPAWN_EGG);
            case "minecraft:frog" -> new ItemStack(Items.FROG_SPAWN_EGG);
            case "minecraft:axolotl" -> new ItemStack(Items.AXOLOTL_SPAWN_EGG);
            case "minecraft:armadillo" -> new ItemStack(Items.ARMADILLO_SPAWN_EGG);
            default -> ItemStack.EMPTY;
        };
        
        if (!spawnEgg.isEmpty() && this.capturedEntityData != null) {
            // 获取实体类型 - 使用 getValue() 而不是 get()，getValue() 返回 @Nullable EntityType<?>
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.parse(entityId)
            );
            
            if (entityType != null) {
                TypedEntityData<EntityType<?>> typedData = 
                    TypedEntityData.of(entityType, this.capturedEntityData.copy());
                
                spawnEgg.set(DataComponents.ENTITY_DATA, typedData);
            }
        }
        
        return spawnEgg;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        this.setTriggered(tag.getBooleanOr("Triggered", false));
        this.setUsesLeft(tag.getIntOr("UsesLeft", MAX_USES));
        this.entityData.set(BAIT_CONSUMED, tag.getBooleanOr("BaitConsumed", false));
        
        tag.child("BaitItem").ifPresent(baitInput -> {
            baitInput.read("item", ItemStack.CODEC).ifPresent(this::setBaitItem);
        });
        
        tag.child("CapturedEntity").ifPresent(entityInput -> {
            entityInput.read("EntityData", CompoundTag.CODEC).ifPresent(data -> {
                this.capturedEntityData = data;
            });
        });
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.putBoolean("Triggered", this.isTriggered());
        tag.putInt("UsesLeft", this.getUsesLeft());
        tag.putBoolean("BaitConsumed", this.entityData.get(BAIT_CONSUMED));
        
        if (!this.getBaitItem().isEmpty()) {
            ValueOutput baitOutput = tag.child("BaitItem");
            baitOutput.store("item", ItemStack.CODEC, this.getBaitItem());
        }
        
        if (this.capturedEntityData != null) {
            ValueOutput entityOutput = tag.child("CapturedEntity");
            // 保存完整的实体数据
            entityOutput.store("EntityData", CompoundTag.CODEC, this.capturedEntityData);
        }
    }

    public boolean isTriggered() {
        return this.entityData.get(IS_TRIGGERED);
    }

    public void setTriggered(boolean triggered) {
        this.entityData.set(IS_TRIGGERED, triggered);
    }

    public ItemStack getBaitItem() {
        return this.entityData.get(BAIT_ITEM);
    }

    public void setBaitItem(ItemStack item) {
        this.entityData.set(BAIT_ITEM, item);
    }

    public int getUsesLeft() {
        return this.entityData.get(USES_LEFT);
    }

    public void setUsesLeft(int uses) {
        this.entityData.set(USES_LEFT, uses);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<TrapEntity> controller = new AnimationController<>("controller", 5, state -> {
            if (this.isTriggered()) {
                if (this.entityData.get(BAIT_CONSUMED)) {
                    return state.setAndContinue(TRIGGER_PRE_HOLD);
                }
                if (!this.entityData.get(HAS_PLAYED_TRIGGER_PRE)) {
                    this.entityData.set(HAS_PLAYED_TRIGGER_PRE, true);
                    return state.setAndContinue(TRIGGER_PRE);
                }
                return state.setAndContinue(TRIGGER_LOOP);
            }
            return state.setAndContinue(IDLE_ANIM);
        });
        
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}
