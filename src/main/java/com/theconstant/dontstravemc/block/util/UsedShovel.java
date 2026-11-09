package com.theconstant.dontstravemc.block.util;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for checking whether the tool used is a shovel.
 */
public class UsedShovel {

    private UsedShovel() {}

    public static boolean isShovel(@Nullable ItemInstance tool) {
        return tool != null && (
                tool.is(Items.WOODEN_SHOVEL) ||
                tool.is(Items.STONE_SHOVEL) ||
                tool.is(Items.IRON_SHOVEL) ||
                tool.is(Items.GOLDEN_SHOVEL) ||
                tool.is(Items.DIAMOND_SHOVEL) ||
                tool.is(Items.NETHERITE_SHOVEL));
    }
}
