package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.item.ModItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public class ModsToolMaterial {

    public static final ToolMaterial FLINT_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            95,
            4.5F,
            0.0F,
            5,
            ModItemTags.FLINT_TOOL_MATERIALS
    );
    public static final ToolMaterial RAW_GOLDEN_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            300,
            7.5F,
            2.5F,
            25,
            ModItemTags.RAW_GOLDEN_TOOL_MATERIALS
    );
}
