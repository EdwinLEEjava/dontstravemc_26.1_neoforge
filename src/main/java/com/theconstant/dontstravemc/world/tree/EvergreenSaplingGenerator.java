package com.theconstant.dontstravemc.world.tree;

import com.theconstant.dontstravemc.TheConstant;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class EvergreenSaplingGenerator {
    
    public static final TreeGrower EVERGREEN = new TreeGrower(
        "evergreen",
        Optional.empty(),
        Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, 
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "evergreen_tree"))),
        Optional.empty()
    );
}
