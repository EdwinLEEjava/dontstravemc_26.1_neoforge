package com.theconstant.dontstravemc.render;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.custom.ConstantBirchLeavesBlock;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;
import java.util.Set;

/**
 * 永恒桦木叶方块：按 {@link ConstantBirchLeavesBlock#HUE} 着色（与原版桦木同纹理，三色来自方块状态）。
 */
public final class ConstantBirchLeavesColors {

    private static final int VANILLA_BIRCH = -8345771;

    private ConstantBirchLeavesColors() {
    }

    public static void register(RegisterColorHandlersEvent.BlockTintSources event) {
        BlockTintSource tint = new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                if (!state.is(ModBlocks.CONSTANT_BIRCH_LEAVES.get())) {
                    return VANILLA_BIRCH;
                }
                return state.getValue(ConstantBirchLeavesBlock.HUE).tintArgb();
            }

            @Override
            public Set<Property<?>> relevantProperties() {
                return Set.of(ConstantBirchLeavesBlock.HUE);
            }
        };
        event.register(List.of(tint), ModBlocks.CONSTANT_BIRCH_LEAVES.get());
    }
}
