package com.theconstant.dontstravemc.equipmentwear;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * 客户端耐久条与 {@link net.minecraft.world.item.Item#getBarWidth(ItemStack)} /
 * {@link net.minecraft.world.item.Item#getBarColor(ItemStack)} 一致。
 */
public final class EquipmentWearBar {

    private EquipmentWearBar() {}

    public static boolean shouldShowBar(ItemStack stack) {
        if (!EquipmentWearRegistry.wearsOn(stack.getItem())) {
            return false;
        }
        return EquipmentWearData.getTicksWorn(stack) > 0;
    }

    public static int barWidth(ItemStack stack) {
        EquipmentWearRegistry.WearConfig config = EquipmentWearRegistry.get(stack.getItem());
        if (config == null) {
            return 0;
        }
        int max = config.maxWearTicks();
        if (max <= 0) {
            return 0;
        }
        int worn = EquipmentWearData.getTicksWorn(stack);
        return Mth.clamp(Math.round(13.0F - worn * 13.0F / max), 0, 13);
    }

    public static int barColor(ItemStack stack) {
        EquipmentWearRegistry.WearConfig config = EquipmentWearRegistry.get(stack.getItem());
        if (config == null) {
            return -1;
        }
        int max = config.maxWearTicks();
        float f = Math.max(0.0F, ((float) max - EquipmentWearData.getTicksWorn(stack)) / max);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }
}
