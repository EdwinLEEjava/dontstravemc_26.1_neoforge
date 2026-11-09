package com.theconstant.dontstravemc.entity.ai.goal.animal;

import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * 玩家靠近后的警惕观察：停止移动并盯着玩家
 */
public class RabbitAlertPlayerGoal extends Goal {
    private final RabbitEntity rabbit;
    private Player targetPlayer;

    public RabbitAlertPlayerGoal(RabbitEntity rabbit) {
        this.rabbit = rabbit;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.rabbit.getLastHurtByMob() != null) return false;
        if (this.rabbit.isSleeping()) return false;

        this.targetPlayer = this.rabbit.level().getNearestPlayer(this.rabbit, 12.0D);
        if (this.targetPlayer == null) {
            this.rabbit.setAlert(false);
            return false;
        }

        double dist = this.rabbit.distanceTo(this.targetPlayer);
        return dist >= 8.0D && dist < 12.0D;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) return false;
        if (this.rabbit.getLastHurtByMob() != null) return false;

        double dist = this.rabbit.distanceTo(this.targetPlayer);
        return dist >= 8.0D && dist < 12.0D;
    }

    @Override
    public void start() {
        this.rabbit.setAlert(true);
        this.rabbit.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        this.rabbit.setAlert(true);
        this.rabbit.getNavigation().stop();
        this.rabbit.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        this.rabbit.setAlert(false);
        this.targetPlayer = null;
    }
}
