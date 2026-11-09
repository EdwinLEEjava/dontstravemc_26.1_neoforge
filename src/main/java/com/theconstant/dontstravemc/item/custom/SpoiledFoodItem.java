package com.theconstant.dontstravemc.item.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 腐烂食物：
 * - 食用后减少 1 点饱食度（Food Level）
 * - 堆肥概率与熔炉燃料时长由数据映射配置：{@code data/dontstravemc/data_maps/item/compostables.json}、
 *   {@code data/dontstravemc/data_maps/item/furnace_fuels.json}
 */
public class SpoiledFoodItem extends Item {
    public SpoiledFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof Player player) {
            if (player.getFoodData().getFoodLevel() > 0) {
                player.getFoodData().eat(-1, 0.0F);
            }
        }

        return result;
    }
}

