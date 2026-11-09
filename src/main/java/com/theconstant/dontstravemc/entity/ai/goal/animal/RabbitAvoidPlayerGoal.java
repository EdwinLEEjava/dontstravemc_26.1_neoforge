package com.theconstant.dontstravemc.entity.ai.goal.animal;

import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RabbitAvoidPlayerGoal extends Goal {
    private final RabbitEntity rabbit;
    private final double fleeSpeed;
    private Player targetPlayer;
    private boolean hasScreamed = false;

    public RabbitAvoidPlayerGoal(RabbitEntity rabbit, double fleeSpeed) {
        this.rabbit = rabbit;
        this.fleeSpeed = fleeSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.rabbit.isSleeping() && this.rabbit.getLastHurtByMob() == null) {
            return false;
        }

        this.targetPlayer = this.rabbit.level().getNearestPlayer(this.rabbit, 12.0D);
        if (this.targetPlayer == null) {
            this.rabbit.setAlert(false);
            return false;
        }

        return this.rabbit.distanceTo(this.targetPlayer) < 8.0D;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) return false;
        return this.rabbit.distanceTo(this.targetPlayer) < 10.0D;
    }

    @Override
    public void start() {
        this.hasScreamed = false;
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        if (!this.hasScreamed) {
            this.rabbit.triggerScream();
            this.hasScreamed = true;
        }

        this.rabbit.setAlert(false);

        if (this.rabbit.getNavigation().isDone()) {
            Vec3 fleePos = DefaultRandomPos.getPosAway(this.rabbit, 16, 7, this.targetPlayer.position());

            if (fleePos == null) {
                Vec3 away = this.rabbit.position().subtract(this.targetPlayer.position()).normalize();
                if (away.lengthSqr() < 1.0E-6) {
                    double angle = this.rabbit.getRandom().nextDouble() * Math.PI * 2;
                    away = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
                }
                fleePos = this.rabbit.position().add(away.scale(12.0));
            }

            this.rabbit.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, this.fleeSpeed);
        }
    }

    @Override
    public void stop() {
        this.rabbit.setAlert(false);
        this.targetPlayer = null;
    }
}
