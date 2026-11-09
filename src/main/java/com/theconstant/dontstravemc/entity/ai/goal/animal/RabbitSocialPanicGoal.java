package com.theconstant.dontstravemc.entity.ai.goal.animal;

import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;

import java.util.EnumSet;
import java.util.List;

public class RabbitSocialPanicGoal extends Goal {
    private final RabbitEntity rabbit;
    private int panicTicks = 0;

    public RabbitSocialPanicGoal(RabbitEntity rabbit) {
        this.rabbit = rabbit;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.rabbit.getLastHurtByMob() != null) return false;

        // Social alarm: check nearby rabbits
        List<RabbitEntity> neighbors = this.rabbit.level().getEntitiesOfClass(
                RabbitEntity.class,
                this.rabbit.getBoundingBox().inflate(16.0D),
                r -> r != this.rabbit && (r.getLastHurtByMob() != null || r.isAlert())
        );

        return !neighbors.isEmpty();
    }

    @Override
    public void start() {
        this.panicTicks = 100; // 5 seconds of panic
    }

    @Override
    public void tick() {
        this.panicTicks--;
        if (this.rabbit.getNavigation().isDone()) {
            // 远离真正的威胁（最近的玩家 / 同伴的攻击者），而不是远离同伴本身
            net.minecraft.world.phys.Vec3 fleeFrom = null;
            net.minecraft.world.entity.player.Player player = this.rabbit.level().getNearestPlayer(this.rabbit, 16.0D);
            if (player != null) {
                fleeFrom = player.position();
            } else {
                List<RabbitEntity> neighbors = this.rabbit.level().getEntitiesOfClass(
                        RabbitEntity.class,
                        this.rabbit.getBoundingBox().inflate(16.0D),
                        r -> r != this.rabbit && r.getLastHurtByMob() != null
                );
                if (!neighbors.isEmpty()) fleeFrom = neighbors.getFirst().getLastHurtByMob().position();
            }
            net.minecraft.world.phys.Vec3 flee = fleeFrom != null
                    ? DefaultRandomPos.getPosAway(this.rabbit, 8, 4, fleeFrom)
                    : null;
            if (flee != null) {
                this.rabbit.getNavigation().moveTo(flee.x, flee.y, flee.z, 1.5D);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.panicTicks > 0;
    }
}
