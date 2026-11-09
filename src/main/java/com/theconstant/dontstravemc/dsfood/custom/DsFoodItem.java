package com.theconstant.dontstravemc.dsfood.custom;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.theconstant.dontstravemc.damagesource.ModDamageTypes;
import com.theconstant.dontstravemc.item.ModItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DsFoodItem extends Item {
    private final float healthMod;  // 生命变化
    private final float sanityMod;  // 理智变化

    public DsFoodItem(Properties properties, float healthMod, float sanityMod) {
        super(properties);
        this.healthMod = healthMod;
        this.sanityMod = sanityMod;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide()) {
            // 1. 处理生命值
            if (healthMod > 0) {
                entity.heal(healthMod);
            } else if (healthMod < 0) {
                entity.hurt(stack.is(ModItemTags.MONSTER_FOODS)
                        ? level.damageSources().source(ModDamageTypes.EATEN_MONSTER_FOOD)
                        : level.damageSources().generic(), -healthMod);
            }

            // 2. 处理理智值
            if (entity instanceof ServerPlayer player) {
                ModAttachments.getSanity(player).addSanity(sanityMod);
            }
        }
        return result;
    }
}
