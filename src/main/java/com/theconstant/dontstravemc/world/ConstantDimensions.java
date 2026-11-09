package com.theconstant.dontstravemc.world;

import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ConstantDimensions {

    public static final ResourceKey<Level> THE_CONSTANT = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "the_constant")
    );

    public static final ResourceKey<Level> THE_CAVES = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "the_caves")
    );

    private ConstantDimensions() {
    }
}
