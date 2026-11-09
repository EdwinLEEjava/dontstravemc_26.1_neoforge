package com.theconstant.dontstravemc.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DsMushroomBlock extends MushroomBlock {

    protected static final VoxelShape SHAPE = Block.column((double)6.0F, (double)0.0F, (double)6.0F);
    private final ResourceKey<ConfiguredFeature<?, ?>> feature;

    public DsMushroomBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, BlockBehaviour.Properties properties) {
        super(feature, properties);
        this.feature = feature;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        TriState soilDecision = below.canSustainPlant(level, belowPos, Direction.UP, state);

        return below.is(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT)
                ? true
                : (soilDecision.isDefault()
                ? this.mayPlaceOn(below, level, belowPos)
                : soilDecision.isTrue());
    }
}
