package com.theconstant.dontstravemc.entity;

import com.theconstant.dontstravemc.TheConstant;

import com.theconstant.dontstravemc.entity.ambient.CreepyEyesEntity;
import com.theconstant.dontstravemc.entity.ambient.DarkWatcherEntity;
import com.theconstant.dontstravemc.entity.animal.ButterflyEntity;
import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import com.theconstant.dontstravemc.entity.monster.BirchFruitSpriteEntity;
import com.theconstant.dontstravemc.entity.monster.MonsterSpiderEntity;
import com.theconstant.dontstravemc.entity.neutral.PigmanEntity;
import com.theconstant.dontstravemc.entity.trap.TrapEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {

    public static final ResourceKey<EntityType<?>> MONSTER_SPIDER_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "monster_spider"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<MonsterSpiderEntity>> MONSTER_SPIDER =
            TheConstant.ENTITY_TYPES.register("monster_spider", () ->
                    EntityType.Builder.of(MonsterSpiderEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .build(MONSTER_SPIDER_KEY)
            );

    public static final ResourceKey<EntityType<?>> BIRCH_FRUIT_SPRITE_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "birch_fruit_sprite"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<BirchFruitSpriteEntity>> BIRCH_FRUIT_SPRITE =
            TheConstant.ENTITY_TYPES.register("birch_fruit_sprite", () ->
                    EntityType.Builder.of(BirchFruitSpriteEntity::new, MobCategory.MONSTER)
                            .sized(0.65F, 1.1F)
                            .build(BIRCH_FRUIT_SPRITE_KEY)
            );

    public static final ResourceKey<EntityType<?>> BUTTERFLY_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "butterfly"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<ButterflyEntity>> BUTTERFLY =
            TheConstant.ENTITY_TYPES.register("butterfly", () ->
                    EntityType.Builder.of(ButterflyEntity::new, MobCategory.AMBIENT)
                            .sized(0.5F, 0.5F)
                            .build(BUTTERFLY_KEY)
            );

    public static final ResourceKey<EntityType<?>> RABBIT_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "rabbit"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<RabbitEntity>> RABBIT = TheConstant.ENTITY_TYPES.register(
            "rabbit",
            () -> EntityType.Builder.of(RabbitEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.5f)
                    .build(RABBIT_KEY)
    );

    public static final ResourceKey<EntityType<?>> TRAP_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "trap"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TrapEntity>> TRAP = TheConstant.ENTITY_TYPES.register(
            "trap",
            () -> EntityType.Builder.<TrapEntity>of(TrapEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.5f)
                    .build(TRAP_KEY)
    );

    public static final ResourceKey<EntityType<?>> MOLE_WORM_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "mole_worm"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<MoleWormEntity>> MOLE_WORM = TheConstant.ENTITY_TYPES.register(
            "mole_worm",
            () -> EntityType.Builder.of(MoleWormEntity::new, MobCategory.CREATURE)
                    .sized(0.8f, 0.6f)
                    .build(MOLE_WORM_KEY)
    );

    public static final ResourceKey<EntityType<?>> PIGMAN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "pigman"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<PigmanEntity>> PIGMAN = TheConstant.ENTITY_TYPES.register(
            "pigman",
            () -> EntityType.Builder.of(PigmanEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build(PIGMAN_KEY)
    );

    public static final ResourceKey<EntityType<?>> DARK_WATCHER_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "dark_watcher"
                    )
            );

    public static final ResourceKey<EntityType<?>> CREEPY_EYES_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            TheConstant.MODID,
                            "creepyeyes"
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CreepyEyesEntity>> CREEPY_EYES = TheConstant.ENTITY_TYPES.register(
            "creepyeyes",
            () -> EntityType.Builder.of(CreepyEyesEntity::new, MobCategory.AMBIENT)
                    .sized(0.8f, 0.8f)
                    .build(CREEPY_EYES_KEY)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<DarkWatcherEntity>> DARK_WATCHER = TheConstant.ENTITY_TYPES.register(
            "dark_watcher",
            () -> EntityType.Builder.of(DarkWatcherEntity::new, MobCategory.AMBIENT)
                    .sized(0.6f, 1.8f)
                    .build(DARK_WATCHER_KEY)
    );

    public static void register() {
        TheConstant.LOGGER.info("Entity types registered via DeferredRegister for " + TheConstant.MODID);
    }
}
