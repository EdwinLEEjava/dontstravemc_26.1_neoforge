package com.theconstant.dontstravemc.entity.ai.goal.animal;

import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 被攻击后的逃跑目标：向攻击者反方向逃离
 */
public class RabbitHurtFleeGoal extends Goal {
    private final RabbitEntity rabbit;
    private final double fleeSpeed;
    private LivingEntity attacker;
    private int panicTicks;
    private int repathCooldown;

    public RabbitHurtFleeGoal(RabbitEntity rabbit, double fleeSpeed) {
        this.rabbit = rabbit;
        this.fleeSpeed = fleeSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity lastHurt = this.rabbit.getLastHurtByMob();
        if (lastHurt == null || !lastHurt.isAlive()) {
            return false;
        }

        this.attacker = lastHurt;
        return true;
    }

    @Override
    public void start() {
        this.panicTicks = 100;
        this.repathCooldown = 0;
        this.rabbit.setAlert(true);
        this.moveAway();
    }

    @Override
    public boolean canContinueToUse() {
        return this.panicTicks > 0 && this.attacker != null && this.attacker.isAlive();
    }

    @Override
    public void tick() {
        this.panicTicks--;
        this.rabbit.setAlert(true);

        if (this.repathCooldown > 0) this.repathCooldown--;
        // 只在路径走完、或攻击者追得很近且冷却结束时才重新寻路，避免频繁重算导致掉头
        boolean attackerClose = this.attacker != null && this.rabbit.distanceToSqr(this.attacker) < 16.0D;
        if (this.rabbit.getNavigation().isDone() || (attackerClose && this.repathCooldown <= 0)) {
            this.moveAway();
        }
    }

    @Override
    public void stop() {
        this.attacker = null;
        this.rabbit.setAlert(false);
    }

    private void moveAway() {
        if (this.attacker == null) return;

        this.repathCooldown = 20;
        Vec3 fleeTarget = DefaultRandomPos.getPosAway(this.rabbit, 16, 7, this.attacker.position());
        if (fleeTarget == null) {
            Vec3 rabbitPos = this.rabbit.position();
            Vec3 away = rabbitPos.subtract(this.attacker.position()).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() < 1.0E-6) {
                double angle = this.rabbit.getRandom().nextDouble() * Math.PI * 2;
                away = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            }
            fleeTarget = rabbitPos.add(away.normalize().scale(12.0));
        }
        this.rabbit.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, this.fleeSpeed);
    }
}
