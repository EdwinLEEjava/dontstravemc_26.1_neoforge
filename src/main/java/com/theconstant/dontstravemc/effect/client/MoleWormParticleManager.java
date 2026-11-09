package com.theconstant.dontstravemc.effect.client;

import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端鼹鼠挖土粒子管理器。
 * - 粒子类型使用脚下方块（类似疾跑扬尘）
 * - enter: 动画开始立即喷发
 * - exit: 动画开始 1.25 秒（25 tick）后喷发
 * - walk_loop: 持续少量飞溅
 */
@OnlyIn(Dist.CLIENT)
public final class MoleWormParticleManager {

    // ===== 粒子数量与频率（可直接调这里） =====
    private static final int ENTER_BURST_COUNT = 18;
    private static final int EXIT_BURST_COUNT = 18;
    private static final int WALK_BURST_COUNT = 3;
    private static final int WALK_BURST_INTERVAL_TICKS = 2;

    private static final Map<UUID, PerEntityState> STATES = new HashMap<>();

    private MoleWormParticleManager() {
    }

    public static void tick(MoleWormEntity mole) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PerEntityState state = STATES.computeIfAbsent(mole.getUUID(), k -> new PerEntityState());

        int transition = mole.getTransitionAnimPublic(); // 0=none 1=enter 2=exit

        // ENTER: 动画开始瞬间喷发
        if (transition == 1) {
            if (!state.enterBurstPlayed) {
                spawnBurst(mole, ENTER_BURST_COUNT);
                state.enterBurstPlayed = true;
            }
            state.exitBurstPlayed = false;
            state.exitTicks = 0;
            state.walkCooldown = 0;
            return;
        } else {
            state.enterBurstPlayed = false;
        }

        // EXIT: 延迟 1.25s（25 tick）喷发
        if (transition == 2) {
            state.exitTicks++;
            if (!state.exitBurstPlayed && state.exitTicks >= 25) {
                spawnBurst(mole, EXIT_BURST_COUNT);
                state.exitBurstPlayed = true;
            }
            state.walkCooldown = 0;
            return;
        } else {
            state.exitBurstPlayed = false;
            state.exitTicks = 0;
        }

        // WALK_LOOP: 持续少量飞溅（沿移动方向向后抛）
        if (mole.isInWalkLoopAnim()) {
            if (state.walkCooldown-- <= 0) {
                spawnWalkTrail(mole, WALK_BURST_COUNT);
                state.walkCooldown = WALK_BURST_INTERVAL_TICKS;
            }
        } else {
            state.walkCooldown = 0;
        }
    }

    private static void spawnBurst(MoleWormEntity mole, int count) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 模型底部中心（略高于地面，避免粒子埋进方块）
        double x = mole.getX();
        double y = mole.getY() + 0.06;
        double z = mole.getZ();

        BlockParticleOption groundParticle = resolveGroundParticle(mole, x, z);
        if (groundParticle == null) return;

        for (int i = 0; i < count; i++) {
            double angle = mole.getRandom().nextDouble() * (Math.PI * 2.0);
            double spread = 0.02 + mole.getRandom().nextDouble() * 0.10;

            // 水平随机散射 + 向上初速度，形成抛物线下落
            double vx = Math.cos(angle) * spread;
            double vz = Math.sin(angle) * spread;
            double vy = 0.16 + mole.getRandom().nextDouble() * 0.18;

            mc.level.addParticle(groundParticle, x, y, z, vx, vy, vz);
        }
    }

    private static void spawnWalkTrail(MoleWormEntity mole, int count) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double x = mole.getX();
        double y = mole.getY() + 0.06;
        double z = mole.getZ();

        BlockParticleOption groundParticle = resolveGroundParticle(mole, x, z);
        if (groundParticle == null) return;

        Vec3 velocity = mole.getDeltaMovement();
        double vxMove = velocity.x;
        double vzMove = velocity.z;
        double speedSqr = vxMove * vxMove + vzMove * vzMove;
        if (speedSqr < 1.0E-6) {
            spawnBurst(mole, count);
            return;
        }

        double speed = Math.sqrt(speedSqr);
        double backX = -vxMove / speed;
        double backZ = -vzMove / speed;

        for (int i = 0; i < count; i++) {
            double lateral = (mole.getRandom().nextDouble() - 0.5) * 0.10;
            double forwardNoise = mole.getRandom().nextDouble() * 0.04;

            double px = x + (-backZ) * lateral;
            double pz = z + ( backX) * lateral;

            double pxVel = backX * (0.06 + forwardNoise) + (mole.getRandom().nextDouble() - 0.5) * 0.02;
            double pzVel = backZ * (0.06 + forwardNoise) + (mole.getRandom().nextDouble() - 0.5) * 0.02;
            double pyVel = 0.12 + mole.getRandom().nextDouble() * 0.10;

            mc.level.addParticle(groundParticle, px, y, pz, pxVel, pyVel, pzVel);
        }
    }

    private static BlockParticleOption resolveGroundParticle(MoleWormEntity mole, double x, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        BlockState groundState = mc.level.getBlockState(BlockPos.containing(x, mole.getY() - 0.2, z));
        if (groundState.isAir() || !groundState.getFluidState().isEmpty()) {
            return null;
        }
        return new BlockParticleOption(ParticleTypes.BLOCK, groundState);
    }

    private static final class PerEntityState {
        boolean enterBurstPlayed;
        boolean exitBurstPlayed;
        int exitTicks;
        int walkCooldown;
    }
}
