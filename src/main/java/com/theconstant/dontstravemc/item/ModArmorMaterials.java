package com.theconstant.dontstravemc.item;

import com.theconstant.dontstravemc.TheConstant;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * 草甲 / 木甲 / 花环 / 草帽：装备资源键与铁砧修复用标签。胸甲由 {@link FoliageChestplateItems} 配置；花环见 {@link com.theconstant.dontstravemc.item.custom.FlowerHatItem}；草帽见 {@link com.theconstant.dontstravemc.item.custom.StrawHatItem}；比例逻辑见 {@link ModItems}。
 */
public final class ModArmorMaterials {

    public static final ResourceKey<EquipmentAsset> GRASS_ARMOR_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "grass_armor")
    );
    public static final ResourceKey<EquipmentAsset> WOOD_ARMOR_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "wood_armor")
    );
    public static final ResourceKey<EquipmentAsset> FLOWER_HAT_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "flower_hat")
    );
    public static final ResourceKey<EquipmentAsset> STRAW_HAT_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "straw_hat")
    );

    public static final TagKey<Item> REPAIRS_GRASS_ARMOR = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "repairs_grass_armor")
    );
    public static final TagKey<Item> REPAIRS_WOOD_ARMOR = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "repairs_wood_armor")
    );

    private ModArmorMaterials() {}
}
