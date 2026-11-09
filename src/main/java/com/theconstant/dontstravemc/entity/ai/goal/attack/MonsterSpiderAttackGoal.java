package com.theconstant.dontstravemc.entity.ai.goal.attack;

import com.theconstant.dontstravemc.entity.monster.MonsterSpiderEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class MonsterSpiderAttackGoal extends MeleeAttackGoal {
    private final MonsterSpiderEntity spider;
    private int customCooldown = 0;

    public MonsterSpiderAttackGoal(MonsterSpiderEntity spider, double speed, boolean pause) {
        super(spider, speed, pause);
        this.spider = spider;
    }

    @Override
    public void tick() {
        if (this.customCooldown > 0) {
            this.customCooldown--;
        }

        super.tick();

        LivingEntity target = this.spider.getTarget();
        if (target != null && this.customCooldown > 5) {
            this.spider.setAggressive(true);
        } else {
            this.spider.setAggressive(false);
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        // MeleeAttackGoal 的标准距离检查
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            if (this.spider.level() instanceof ServerLevel serverLevel) {
                this.spider.doHurtTarget(serverLevel, target);
            }
        }
    }

    @Override
    protected boolean isTimeToAttack() {
        return this.customCooldown <= 0;
    }

    @Override
    protected void resetAttackCooldown() {
        this.customCooldown = this.adjustedTickDelay(this.spider.getAttackDuration());
    }

    @Override
    public void stop() {
        super.stop();
        this.customCooldown = 0;
        this.spider.setAggressive(false);
    }
}