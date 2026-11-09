package com.theconstant.dontstravemc.render;

import com.theconstant.dontstravemc.block.ModBlocks;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

public class ConstantGrassBlockColors {
    private ConstantGrassBlockColors() {}

    public static void register(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grassBlock()), ModBlocks.CONSTANT_GRASS_BLOCK.get());
    }
}
