package com.theconstant.dontstravemc.block.util;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared helper for transplanted-plant style drops.
 *
 * Logic:
 * - If broken with a shovel: drop main transplant block/item
 * - If the plant is currently harvestable: also drop harvest item
 * - If not broken with a shovel: drop optional fallback item (if provided)
 */
public final class PlantDropHelper {

    private PlantDropHelper() {
    }

    public static List<ItemStack> getDrops(
            LootParams.Builder params,
            boolean hasHarvestable,
            ItemLike shovelMainDrop,
            int shovelMainCount,
            ItemLike harvestDrop,
            int harvestCount,
            ItemLike nonShovelDrop,
            int nonShovelCount
    ) {
        List<ItemStack> drops = new ArrayList<>();
        ItemInstance tool = params.getOptionalParameter(LootContextParams.TOOL);

        if (UsedShovel.isShovel(tool)) {
            drops.add(new ItemStack(shovelMainDrop, shovelMainCount));
            if (hasHarvestable && harvestDrop != null && harvestCount > 0) {
                drops.add(new ItemStack(harvestDrop, harvestCount));
            }
        } else if (nonShovelDrop != null && nonShovelCount > 0) {
            drops.add(new ItemStack(nonShovelDrop, nonShovelCount));
        }

        return drops;
    }
}
