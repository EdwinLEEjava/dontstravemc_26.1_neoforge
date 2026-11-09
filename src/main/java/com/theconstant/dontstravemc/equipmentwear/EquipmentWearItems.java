package com.theconstant.dontstravemc.equipmentwear;

import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.world.entity.EquipmentSlot;

public final class EquipmentWearItems {

    private EquipmentWearItems() {}

    public static void register() {
        EquipmentWearRegistry.register(ModItems.STRAW_HAT.get(), EquipmentSlot.HEAD, 120000);
    }
}
