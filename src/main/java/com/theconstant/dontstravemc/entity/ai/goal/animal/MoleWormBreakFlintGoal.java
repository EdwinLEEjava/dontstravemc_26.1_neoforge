package com.theconstant.dontstravemc.entity.ai.goal.animal;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 鼹鼠破坏燧石方块目标。
 * 行为流程：
 * 1) 地下接近目标
 * 2) 到达后 enter（地下->地上）
 * 3) idle 观察
 * 4) action 破坏（此刻燧石消失）
 * 5) idle 40 tick
 * 6) exit（地上->地下）
 */
public class MoleWormBreakFlintGoal extends Goal {

    private static final int SCAN_RANGE = 12;
    private static final int ENTER_TICKS = 45; // 2.25s
    private static final int EXIT_TICKS = 55;  // 2.75s
    private static final int PRE_ACTION_IDLE_TICKS = 10;
    private static final int ACTION_TICKS = 30;
    private static final int POST_ACTION_IDLE_TICKS = 40;

    private final MoleWormEntity mole;
    private BlockPos targetPos = null;
    private int scanTick = 0;

    // 0=approach underground, 1=enter anim, 2=pre idle, 3=action, 4=post idle, 5=exit anim
    private int phase = 0;
    private int phaseTicks = 0;

    public MoleWormBreakFlintGoal(MoleWormEntity mole) {
        this.mole = mole;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mole.isUnderground()) return false;
        if (mole.isInventoryFull()) return false;
        if (--scanTick > 0) return false;
        scanTick = 20;
        targetPos = findFlintBlock();
        phase = 0;
        phaseTicks = 0;
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (phase > 0) return true;
        if (mole.isInventoryFull()) return false;
        if (targetPos == null) return false;
        return mole.level().getBlockState(targetPos).is(ModBlocks.FLINT.get());
    }

    @Override
    public void start() {
        if (targetPos != null) navigateTo(targetPos);
    }

    @Override
    public void tick() {
        boolean targetInvalid = targetPos == null || !mole.level().getBlockState(targetPos).is(ModBlocks.FLINT.get());
        if (targetInvalid && phase <= 1) {
            if (!mole.isUnderground()) mole.setUnderground(true);
            targetPos = null;
            phase = 0;
            return;
        }

        if (!targetInvalid && !mole.isStunned()) {
            mole.getLookControl().setLookAt(
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 0.5,
                    targetPos.getZ() + 0.5,
                    30F, 30F);
        }

        if (phase == 0) {
            double distSq = mole.distanceToSqr(Vec3.atCenterOf(targetPos));
            if (distSq <= 2.5D) {
                mole.getNavigation().stop();
                mole.setUnderground(false);
                phase = 1;
                phaseTicks = ENTER_TICKS;

            } else if (mole.getNavigation().isDone()) {
                navigateTo(targetPos);
            }
            return;
        }

        if (phase == 1) { // enter
            if (--phaseTicks <= 0) {
                phase = 2;
                phaseTicks = PRE_ACTION_IDLE_TICKS;
            }
            return;
        }

        if (phase == 2) { // pre idle
            if (--phaseTicks <= 0) {
                breakFlint(); // action 发生时才破坏
                mole.triggerActionAnim();
                phase = 3;
                phaseTicks = ACTION_TICKS;
            }
            return;
        }

        if (phase == 3) { // action
            if (--phaseTicks <= 0) {
                phase = 4;
                phaseTicks = POST_ACTION_IDLE_TICKS;
            }
            return;
        }

        if (phase == 4) { // post idle
            if (--phaseTicks <= 0) {
                mole.setUnderground(true); // 触发 exit
                phase = 5;
                phaseTicks = EXIT_TICKS;
            }
            return;
        }

        if (phase == 5) { // exit
            if (--phaseTicks <= 0) {
                targetPos = null;
                phase = 0;
            }
        }
    }

    @Override
    public void stop() {
        targetPos = null;
        phase = 0;
        phaseTicks = 0;
        mole.getNavigation().stop();
        if (!mole.isUnderground()) {
            mole.setUnderground(true);
        }
    }

    private void breakFlint() {
        if (!(mole.level() instanceof ServerLevel serverLevel)) return;
        BlockState state = serverLevel.getBlockState(targetPos);
        if (!state.is(ModBlocks.FLINT.get())) return;

        serverLevel.removeBlock(targetPos, false);

        ItemStack flint = new ItemStack(Items.FLINT, 1);
        mole.tryPickupItem(flint);
    }

    private BlockPos findFlintBlock() {
        BlockPos center = mole.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SCAN_RANGE, -2, -SCAN_RANGE),
                center.offset(SCAN_RANGE, 2, SCAN_RANGE))) {
            if (mole.level().getBlockState(pos).is(ModBlocks.FLINT.get())) {
                return pos.immutable();
            }
        }
        return null;
    }

    private void navigateTo(BlockPos pos) {
        mole.getNavigation().moveTo(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                1.0D);
    }
}
