package com.theconstant.dontstravemc.entity.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 鼹鼠路过方块的原版开裂特效管理器。
 *
 * 规则：
 * - 当前有鼹鼠停留的格子固定为 70% 进度
 * - 鼹鼠离开后每秒衰减 40%（每 tick 衰减 0.02）直到清除
 */
public final class MoleWormBurrowCrackManager {

    private static final float HOLD_PROGRESS = 0.70f;
    private static final float DECAY_PER_TICK = 0.40f / 20.0f;
    private static final int BREAKER_ID_SALT = 0x4D57_0000;

    private static final Map<ServerLevel, PerLevelState> STATES = new WeakHashMap<>();

    private MoleWormBurrowCrackManager() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MoleWormBurrowCrackManager::onLevelTickPost);
    }

    private static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        tickWorld(level);
    }

    public static void markOccupied(ServerLevel level, BlockPos pos) {
        BlockPos crackPos = resolveCrackPos(level, pos);
        if (crackPos == null) return;

        PerLevelState state = STATES.computeIfAbsent(level, k -> new PerLevelState());
        state.occupiedThisTick.add(crackPos.immutable());
    }

    private static void tickWorld(ServerLevel level) {
        PerLevelState state = STATES.get(level);
        if (state == null) return;

        // 占用中的格子保持在 70%
        for (BlockPos pos : state.occupiedThisTick) {
            state.progressByPos.put(pos, HOLD_PROGRESS);
        }

        Iterator<Map.Entry<BlockPos, Float>> it = state.progressByPos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Float> entry = it.next();
            BlockPos pos = entry.getKey();
            float progress = entry.getValue();

            if (!isCrackable(level, pos)) {
                sendStage(level, pos, -1, state);
                it.remove();
                continue;
            }

            if (!state.occupiedThisTick.contains(pos)) {
                progress -= DECAY_PER_TICK;
            }

            if (progress <= 0.0f) {
                sendStage(level, pos, -1, state);
                it.remove();
                continue;
            }

            progress = Math.min(progress, HOLD_PROGRESS);
            entry.setValue(progress);
            sendStage(level, pos, toStage(progress), state);
        }

        state.occupiedThisTick.clear();

        if (state.progressByPos.isEmpty()) {
            STATES.remove(level);
        }
    }

    private static int toStage(float progress) {
        // 原版破坏阶段是 0..9
        return Mth.clamp((int) Math.floor(progress * 10.0f), 0, 9);
    }

    private static void sendStage(ServerLevel level, BlockPos pos, int stage, PerLevelState state) {
        Integer last = state.lastStageByPos.get(pos);
        if (last != null && last == stage) return;

        level.destroyBlockProgress(breakerId(pos), pos, stage);

        if (stage < 0) {
            state.lastStageByPos.remove(pos);
        } else {
            state.lastStageByPos.put(pos, stage);
        }
    }

    private static int breakerId(BlockPos pos) {
        return BREAKER_ID_SALT ^ pos.hashCode();
    }

    private static BlockPos resolveCrackPos(ServerLevel level, BlockPos pos) {
        if (isCrackable(level, pos)) return pos;

        BlockPos below = pos.below();
        if (isCrackable(level, below)) return below;

        BlockPos twoBelow = below.below();
        if (isCrackable(level, twoBelow)) return twoBelow;

        return null;
    }

    private static boolean isCrackable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) >= 0.0f;
    }

    private static final class PerLevelState {
        private final Set<BlockPos> occupiedThisTick = new HashSet<>();
        private final Map<BlockPos, Float> progressByPos = new HashMap<>();
        private final Map<BlockPos, Integer> lastStageByPos = new HashMap<>();
    }
}
