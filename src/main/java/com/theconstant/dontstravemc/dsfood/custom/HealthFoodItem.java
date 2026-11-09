package com.theconstant.dontstravemc.dsfood.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HealthFoodItem extends Item {
    private final float healAmount;

    public HealthFoodItem(Properties properties, float healAmount) {
        super(properties);
        this.healAmount = healAmount;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity.isAlive() && healAmount > 0f) {
            entity.heal(healAmount);
        }

        return result;
    }

    public float getHealAmount() {
        return healAmount;
    }
}
