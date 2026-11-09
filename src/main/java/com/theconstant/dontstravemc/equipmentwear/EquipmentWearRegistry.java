package com.theconstant.dontstravemc.equipmentwear;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * 登记「仅在指定装备槽佩戴时累积磨损」的物品。
 */
public final class EquipmentWearRegistry {

    private static final Map<Item, WearConfig> ENTRIES = new HashMap<>();

    private EquipmentWearRegistry() {}

    public record WearConfig(EquipmentSlot slot, int maxWearTicks) {}

    public static void register(Item item, EquipmentSlot slot, int maxWearTicks) {
        ENTRIES.put(item, new WearConfig(slot, maxWearTicks));
    }

    public static boolean wearsOn(Item item) {
        return ENTRIES.containsKey(item);
    }

    @Nullable
    public static WearConfig get(Item item) {
        return ENTRIES.get(item);
    }
}
