package com.theconstant.dontstravemc.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> MEAT_FOODS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("dontstravemc", "meat_foods")
    );
    public static final TagKey<Item> MONSTER_FOODS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("dontstravemc", "monster_food")
    );
    public static final TagKey<Item> VEGETARIAN_FOODS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("dontstravemc", "vegetarian_foods")
    );
    public static final TagKey<Item> FLINT_TOOL_MATERIALS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("dontstravemc", "flint_tool_materials")
    );
    public static final TagKey<Item> RAW_GOLDEN_TOOL_MATERIALS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("dontstravemc", "raw_golden_tool_materials")
    );
}
