package com.theconstant.dontstravemc.tag;

import com.theconstant.dontstravemc.TheConstant;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {

    public static final TagKey<Block> PIGMAN_CHOPPABLE = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "pigman_choppable")
    );

    public static final TagKey<Block> PIGMAN_MINEABLE = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "pigman_mineable")
    );

    /** 需用锤子高效挖掘的方块（对应 Item.Properties.tool 的 effectiveBlocks） */
    public static final TagKey<Block> MINEABLE_HAMMER = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "mineable/hammer")
    );

    public static final TagKey<Block> PIGMAN_HOUSE_DOORS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "pigman_house_doors")
    );

    public static final TagKey<Block> PIGMAN_HOUSE_LIGHT_BLOCKS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "pigman_house_light_blocks")
    );
}
