package com.theconstant.dontstravemc;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.entity.ambient.CreepyEyesEntity;
import com.theconstant.dontstravemc.entity.ambient.DarkWatcherEntity;
import com.theconstant.dontstravemc.entity.animal.ButterflyEntity;
import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import com.theconstant.dontstravemc.entity.monster.BirchFruitSpriteEntity;
import com.theconstant.dontstravemc.entity.monster.MonsterSpiderEntity;
import com.theconstant.dontstravemc.entity.neutral.PigmanEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 注册阶段之后的补丁：实体默认属性、床 BE 合法方块。
 */
@EventBusSubscriber(modid = TheConstant.MODID)
public final class ModRegistryEvents {

    private ModRegistryEvents() {
    }

    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MONSTER_SPIDER.get(), MonsterSpiderEntity.createSpiderAttributes().build());
        event.put(ModEntities.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        event.put(ModEntities.RABBIT.get(), RabbitEntity.createAttributes().build());
        event.put(ModEntities.MOLE_WORM.get(), MoleWormEntity.createAttributes().build());
        event.put(ModEntities.PIGMAN.get(), PigmanEntity.createAttributes().build());
        event.put(ModEntities.DARK_WATCHER.get(), DarkWatcherEntity.createAttributes().build());
        event.put(ModEntities.CREEPY_EYES.get(), CreepyEyesEntity.createAttributes().build());
        event.put(ModEntities.BIRCH_FRUIT_SPRITE.get(), BirchFruitSpriteEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onBedBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BED, ModBlocks.PIGMAN_BED.get());
    }
}
