package com.theconstant.dontstravemc.render;

import com.theconstant.dontstravemc.block.ModBlocks;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

public class UmbelledRockJasmineColors {
    private UmbelledRockJasmineColors() {}

    public static void register(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grass()), ModBlocks.UMBELLED_ROCK_JASMINE.get());
    }
}
