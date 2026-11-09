package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Waxed (transplanted) berry bush - variant 2.
 * Identical behaviour to WaxedBerryBushBlock but returns BERRY_BUSH_2_WAXED as self drop.
 */
public class WaxedBerryBush2Block extends WaxedBerryBushBlock {
    public static final MapCodec<WaxedBerryBush2Block> CODEC = simpleCodec(WaxedBerryBush2Block::new);

    public WaxedBerryBush2Block(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block getSelfDrop() {
        return ModBlocks.BERRY_BUSH_2_WAXED.get();
    }
}
