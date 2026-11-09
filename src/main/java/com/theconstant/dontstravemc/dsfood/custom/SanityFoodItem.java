package com.theconstant.dontstravemc.dsfood.custom;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SanityFoodItem extends Item {
    private final float sanityModifier;

    public SanityFoodItem(Properties properties, float sanityModifier) {
        super(properties);
        this.sanityModifier = sanityModifier;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            if (sanityModifier != 0f) {
                ModAttachments.getSanity(player).addSanity(sanityModifier);
            }
        }

        return result;
    }
}
