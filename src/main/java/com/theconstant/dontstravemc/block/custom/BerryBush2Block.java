package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Natural berry bush block - variant 2 (Don't Starve style).
 * Identical behaviour to BerryBushBlock but drops BERRY_BUSH_2_WAXED when shovelled.
 */
public class BerryBush2Block extends BerryBushBlock {
    public static final MapCodec<BerryBush2Block> CODEC = simpleCodec(BerryBush2Block::new);

    public BerryBush2Block(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block getWaxedDrop() {
        return ModBlocks.BERRY_BUSH_2_WAXED.get();
    }
}
