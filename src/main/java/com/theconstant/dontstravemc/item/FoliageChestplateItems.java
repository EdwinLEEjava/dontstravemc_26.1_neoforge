package com.theconstant.dontstravemc.item;

import com.theconstant.dontstravemc.sound.ModSoundEvents;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;

/**
 * 草甲 / 木甲胸甲物品属性：用 {@link DataComponents#EQUIPPABLE} 注册槽位与纹理；
 * {@code damageOnHurt=false} 避免原版再扣耐久（由 Mixin 按自定义比例扣）。
 */
public final class FoliageChestplateItems {

    private FoliageChestplateItems() {}

    public static Item.Properties grassProperties(Item.Properties base) {
        return chestplateProperties(base, ModArmorMaterials.GRASS_ARMOR_ASSET, 21, ModArmorMaterials.REPAIRS_GRASS_ARMOR);
    }

    public static Item.Properties woodProperties(Item.Properties base) {
        return chestplateProperties(base, ModArmorMaterials.WOOD_ARMOR_ASSET, 109, ModArmorMaterials.REPAIRS_WOOD_ARMOR);
    }

    private static Item.Properties chestplateProperties(
            Item.Properties base,
            ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset,
            int durability,
            TagKey<Item> repairTag
    ) {
        Holder<net.minecraft.sounds.SoundEvent> equipSound = ModSoundEvents.PICKUP_ITEM;
        Equippable equippable = new Equippable(
                EquipmentSlot.CHEST,
                equipSound,
                Optional.of(asset),
                Optional.empty(),
                Optional.empty(),
                true,
                true,
                false,
                false,
                false,
                equipSound
        );
        return base
                .durability(durability)
                .enchantable(15)
                .repairable(repairTag)
                .component(DataComponents.EQUIPPABLE, equippable);
    }
}
