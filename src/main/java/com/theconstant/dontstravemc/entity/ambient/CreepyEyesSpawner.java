package com.theconstant.dontstravemc.entity.ambient;

import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import com.theconstant.dontstravemc.attachment.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class CreepyEyesSpawner {

    private static final float SANITY_THRESHOLD = 160.0f; // 80% of 200
    private static final double MIN_SPAWN_RADIUS = 10.0;
    private static final double MAX_SPAWN_RADIUS = 15.0;

    /** Called by the spawn listener at the player-specific interval. */
    public static void checkAndSpawn(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();

        // 永恒领域：仅真夜；其他维�?tick�?2000
        if (!ConstantRealmTime.isNight(level, level.getDefaultClockTime())) return;

        // sanity < 80%
        float sanity = ModAttachments.getSanity(player).getSanity();
        if (sanity >= SANITY_THRESHOLD) return;

        // Max 7 creepy eyes per player at the same time
        long existingCount = level.getEntitiesOfClass(
                CreepyEyesEntity.class,
                player.getBoundingBox().inflate(200),
                e -> e.isAlive() && player.equals(e.getTargetPlayer())
        ).size();
        if (existingCount >= 7) return;

        // Try to find a dark spawn position (up to 20 attempts)
        Vec3 spawnPos = findDarkSpawnPos(player, level);
        if (spawnPos == null) return;

        // Determine texture index based on sanity
        int textureIndex = pickTextureIndex(sanity, player);

        // Randomise lifetime: 20-30 s
        int lifetimeTicks = 400 + player.getRandom().nextInt(201); // 400-600

        CreepyEyesEntity entity = new CreepyEyesEntity(ModEntities.CREEPY_EYES.get(), level);
        entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        entity.setTargetPlayer(player);
        entity.setTextureIndex(textureIndex);
        entity.setMaxLifetimeTicks(lifetimeTicks);

        level.addFreshEntity(entity);
    }

    // -------------------------------------------------------------------------

    /**
     * Pick a random horizontal position 10-15 blocks from the player that is
     * in the dark (block light <= 5). Returns null if no valid position found.
     */
    private static Vec3 findDarkSpawnPos(ServerPlayer player, ServerLevel level) {
        Vec3 playerPos = player.position();

        for (int attempt = 0; attempt < 20; attempt++) {
            double angle  = player.getRandom().nextDouble() * Math.PI * 2.0;
            double radius = MIN_SPAWN_RADIUS +
                    player.getRandom().nextDouble() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

            double spawnX = playerPos.x + Math.cos(angle) * radius;
            double spawnZ = playerPos.z + Math.sin(angle) * radius;
            double spawnY = playerPos.y + (player.getRandom().nextDouble() * 4.0 - 1.0); // ±1-3 y

            BlockPos bp = BlockPos.containing(spawnX, spawnY, spawnZ);
            int blockLight = level.getBrightness(LightLayer.BLOCK, bp);

            if (blockLight <= 5) {
                return new Vec3(spawnX, spawnY, spawnZ);
            }
        }
        return null;
    }

    /**
     * Lower sanity �?higher chance of texture 1 (creepyeyes_1.png).
     *
     * sanity ratio r = sanity / 160 (0..1 within threshold range)
     *   r=0 (sanity=0)   �?weight1=80%, weight2=15%, weight3=5%
     *   r=1 (sanity=160) �?weight1=33%, weight2=33%, weight3=34%
     */
    private static int pickTextureIndex(float sanity, ServerPlayer player) {
        float r = Math.max(0f, Math.min(1f, sanity / SANITY_THRESHOLD));

        // Lerp weights
        float w1 = 0.80f - r * (0.80f - 0.33f); // 80% �?33%
        float w2 = 0.15f + r * (0.33f - 0.15f); // 15% �?33%
        // w3 = remainder

        float roll = player.getRandom().nextFloat();
        if (roll < w1) return 1;
        if (roll < w1 + w2) return 2;
        return 3;
    }
}
