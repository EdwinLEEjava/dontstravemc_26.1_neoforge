package com.theconstant.dontstravemc.entity.ai.goal.animal;

import com.theconstant.dontstravemc.entity.ai.sensing.ItemSensing;
import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * 鼹鼠收集地面掉落物目标。
 * 行为流程：
 * 1) 地下接近目标
 * 2) 到达后 enter（地下->地上）
 * 3) idle 观察
 * 4) action 收集（此刻物品消失）
 * 5) idle 40 tick
 * 6) exit（地上->地下）
 */
public class MoleWormCollectItemGoal extends Goal {

    private static final int ENTER_TICKS = 45; // 2.25s
    private static final int EXIT_TICKS = 55;  // 2.75s
    private static final int PRE_ACTION_IDLE_TICKS = 10;
    private static final int ACTION_TICKS = 30;
    private static final int POST_ACTION_IDLE_TICKS = 40;
    private static final double DIRECT_APPROACH_DIST_SQ = 4.0D;

    private final MoleWormEntity mole;
    private ItemEntity target;
    private int scanTick = 0;
    private int repathTick = 0;
    private double lastHorizontalDistSq = Double.MAX_VALUE;

    // 0=approach underground, 1=enter anim, 2=pre idle, 3=action, 4=post idle, 5=exit anim
    private int phase = 0;
    private int phaseTicks = 0;

    public MoleWormCollectItemGoal(MoleWormEntity mole) {
        this.mole = mole;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mole.isUnderground()) return false;
        if (mole.isInventoryFull()) return false;
        if (--scanTick > 0) return false;
        scanTick = 20;
        target = ItemSensing.findNearestFood(mole, 8.0D, MoleWormCollectItemGoal::isCollectible);
        phase = 0;
        phaseTicks = 0;
        resetApproachTracking();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (phase > 0) return true;
        if (mole.isInventoryFull()) return false;
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        resetApproachTracking();
        if (target != null) {
            navigateToTarget();
        }
    }

    @Override
    public void tick() {
        boolean targetInvalid = target == null || !target.isAlive();
        if (targetInvalid && phase == 0) {
            if (!mole.isUnderground()) mole.setUnderground(true);
            resetApproachTracking();
            phase = 0;
            return;
        }

        if (!targetInvalid && !mole.isStunned()) {
            mole.getLookControl().setLookAt(target, 30F, 30F);
        }

        if (phase == 0) {
            double dx = target.getX() - mole.getX();
            double dz = target.getZ() - mole.getZ();
            double horizontalDistSq = dx * dx + dz * dz;
            double approachDistSq = Math.pow(mole.getBbWidth() * 0.5 + 0.6, 2);

            if (horizontalDistSq <= approachDistSq) {
                mole.getNavigation().stop();
                mole.setUnderground(false);
                phase = 1;
                phaseTicks = ENTER_TICKS;
                return;
            }

            if (horizontalDistSq <= DIRECT_APPROACH_DIST_SQ) {
                // 进入近距离后直接用 MoveControl 推进，绕开 PathNavigation 的到点容差
                mole.getNavigation().stop();
                mole.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.2D);
                return;
            }

            tickApproachRepath(horizontalDistSq);
            return;
        }

        if (phase == 1) {
            if (--phaseTicks <= 0) {
                phase = 2;
                phaseTicks = PRE_ACTION_IDLE_TICKS;
            }
            return;
        }

        if (phase == 2) {
            if (--phaseTicks <= 0) {
                double collectDistSq = Math.pow(mole.getBbWidth() * 0.5 + 1.2, 2);
                boolean picked = false;
                if (!targetInvalid && mole.distanceToSqr(target) <= collectDistSq) {
                    ItemStack stack = target.getItem().copy();
                    if (mole.tryPickupItem(stack)) {
                        target.discard();
                        picked = true;
                    }
                }

                if (picked) {
                    mole.triggerActionAnim();
                    phase = 3;
                    phaseTicks = ACTION_TICKS;
                } else {
                    phase = 0;
                    navigateToTarget();
                    repathTick = 2;
                }
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
                target = null;
                phase = 0;
            }
        }
    }

    private void tickApproachRepath(double horizontalDistSq) {
        if (--repathTick > 0) {
            return;
        }

        boolean noPath = mole.getNavigation().isDone();
        boolean targetMoved = horizontalDistSq > lastHorizontalDistSq + 0.16D;

        if (noPath || targetMoved) {
            navigateToTarget();
        }

        repathTick = targetMoved ? 2 : 6;
        lastHorizontalDistSq = horizontalDistSq;
    }

    private void navigateToTarget() {
        if (target == null) return;
        mole.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0D);
    }

    private void resetApproachTracking() {
        repathTick = 0;
        lastHorizontalDistSq = Double.MAX_VALUE;
    }

    @Override
    public void stop() {
        target = null;
        phase = 0;
        phaseTicks = 0;
        resetApproachTracking();
        mole.getNavigation().stop();
        if (!mole.isUnderground()) {
            mole.setUnderground(true);
        }
    }

    public static boolean isCollectible(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.DIAMOND) || stack.is(Items.EMERALD)
                || stack.is(Items.AMETHYST_SHARD) || stack.is(Items.QUARTZ)
                || stack.is(Items.LAPIS_LAZULI) || stack.is(Items.PRISMARINE_CRYSTALS)) return true;
        if (stack.is(Items.IRON_INGOT) || stack.is(Items.GOLD_INGOT)
                || stack.is(Items.COPPER_INGOT) || stack.is(Items.NETHERITE_INGOT)
                || stack.is(Items.RAW_IRON) || stack.is(Items.RAW_GOLD)
                || stack.is(Items.RAW_COPPER)) return true;
        return stack.is(Items.COBBLESTONE) || stack.is(Items.STONE)
                || stack.is(Items.GRAVEL) || stack.is(Items.FLINT);
    }
}
