package com.theconstant.dontstravemc.equipmentwear;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 磨损进度存于 {@link DataComponents#CUSTOM_DATA} 的模组专用根键。
 */
public final class EquipmentWearData {

    private static final String ROOT = "EquipmentWear";
    private static final String TICKS = "Ticks";

    private EquipmentWearData() {}

    public static int getTicksWorn(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return 0;
        }
        CompoundTag root = customData.copyTag();
        if (!root.contains(ROOT)) {
            return 0;
        }
        return root.getCompoundOrEmpty(ROOT).getIntOr(TICKS, 0);
    }

    public static void setTicksWorn(ItemStack stack, int ticks) {
        int clamped = Math.max(0, ticks);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag wear = root.getCompoundOrEmpty(ROOT);
            wear.putInt(TICKS, clamped);
            root.put(ROOT, wear);
        });
    }
}
