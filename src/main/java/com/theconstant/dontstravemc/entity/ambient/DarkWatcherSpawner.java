package com.theconstant.dontstravemc.entity.ambient;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DarkWatcherSpawner {

    private static final double SPAWN_RADIUS = 30.0;
    private static final double MIN_SPAWN_HEIGHT = 5.0;
    private static final double MAX_SPAWN_HEIGHT = 20.0;
    private static final float SANITY_THRESHOLD = 130.0f; // 65% of 200
    private static final float SPAWN_CHANCE = 0.3f; // 30%

    /**
     * 检查是否应该为玩家生成暗影观察�?
     */
    public static void checkAndSpawn(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();

        // 永恒领域：仅真夜生成；其他维�?tick�?2000 视为夜晚半段
        if (!ConstantRealmTime.isNight(level, level.getDefaultClockTime())) {
            return;
        }

        // 检查玩�?sanity 是否低于 65%
        float playerSanity = ModAttachments.getSanity(player).getSanity();
        if (playerSanity >= SANITY_THRESHOLD) {
            return;
        }

        // 检查世界中是否已存在暗影观察�?
        if (hasExistingDarkWatcher(level, player)) {
            return;
        }

        // 30% 概率生成
        if (player.getRandom().nextFloat() > SPAWN_CHANCE) {
            return;
        }

        // 生成暗影观察�?
        spawnDarkWatcher(player, level);
    }

    /**
     * 检查世界中是否已存在暗影观察�?
     */
    private static boolean hasExistingDarkWatcher(ServerLevel level, ServerPlayer player) {
        return !level.getEntitiesOfClass(
                DarkWatcherEntity.class,
                player.getBoundingBox().inflate(100),
                e -> e.isAlive()
        ).isEmpty();
    }

    /**
     * 在玩家周围生成暗影观察�?
     */
    private static void spawnDarkWatcher(ServerPlayer player, ServerLevel level) {
        Vec3 playerPos = player.position();

        // 随机选择方向（水平）
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        double distance = MIN_SPAWN_HEIGHT + player.getRandom().nextDouble() * (MAX_SPAWN_HEIGHT - MIN_SPAWN_HEIGHT);

        // 计算生成位置
        double spawnX = playerPos.x + Math.cos(angle) * SPAWN_RADIUS;
        double spawnY = playerPos.y + distance;
        double spawnZ = playerPos.z + Math.sin(angle) * SPAWN_RADIUS;

        // 创建实体
        DarkWatcherEntity darkWatcher = new DarkWatcherEntity(
                com.theconstant.dontstravemc.entity.ModEntities.DARK_WATCHER.get(),
                level
        );

        darkWatcher.setPos(spawnX, spawnY, spawnZ);
        darkWatcher.setTargetPlayer(player);

        // 添加到世�?
        level.addFreshEntity(darkWatcher);
    }
}
