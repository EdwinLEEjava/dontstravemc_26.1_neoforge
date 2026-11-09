package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.Fertilizable;
import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.util.PlantDropHelper;
import com.theconstant.dontstravemc.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;

import java.util.List;

public class WaxedGrassBlock extends VegetationBlock implements Fertilizable, BonemealableBlock {
    public static final MapCodec<WaxedGrassBlock> CODEC = simpleCodec(WaxedGrassBlock::new);

    public static final BooleanProperty WITHERED = BooleanProperty.create("withered");
    public static final BooleanProperty HARVESTED = BooleanProperty.create("harvested");
    public static final IntegerProperty GROWTH_COUNT = IntegerProperty.create("growth_count", 0, 20);

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public WaxedGrassBlock(BlockBehaviour.Properties properties) {
        super(properties.randomTicks());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WITHERED, true)
                .setValue(HARVESTED, true)
                .setValue(GROWTH_COUNT, 0));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WITHERED, HARVESTED, GROWTH_COUNT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canBeFertilized(BlockState state, Level level, BlockPos pos) {
        return state.getValue(WITHERED);
    }

    @Override
    public void fertilize(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            this.performBonemeal(serverLevel, level.getRandom(), pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(WITHERED) || (state.getValue(HARVESTED) && state.getValue(GROWTH_COUNT) > 0);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(WITHERED)) {
            level.setBlock(pos, state
                    .setValue(WITHERED, false)
                    .setValue(HARVESTED, true)
                    .setValue(GROWTH_COUNT, 20), 3);
        } else if (state.getValue(HARVESTED)) {
            int growthCount = state.getValue(GROWTH_COUNT);
            if (growthCount > 0) {
                level.setBlock(pos, state
                        .setValue(HARVESTED, false)
                        .setValue(GROWTH_COUNT, growthCount - 1), 3);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WITHERED)) return;
        if (state.getValue(HARVESTED) && state.getValue(GROWTH_COUNT) > 0) {
            if (level.getMaxLocalRawBrightness(pos.above()) >= 0 && random.nextInt(3) == 0) {
                this.performBonemeal(level, random, pos, state);
            }
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return (!state.getValue(WITHERED) && state.getValue(HARVESTED) && state.getValue(GROWTH_COUNT) > 0) || super.isRandomlyTicking(state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (state.getValue(WITHERED)) return InteractionResult.PASS;
            if (!state.getValue(HARVESTED)) {
                int growthCount = state.getValue(GROWTH_COUNT);
                Block.popResource(level, pos, new ItemStack(ModItems.CUT_GRASS.get(), 1));
                if (growthCount <= 0) {
                    level.setBlock(pos, state.setValue(WITHERED, true).setValue(HARVESTED, true).setValue(GROWTH_COUNT, 0), 3);
                } else {
                    level.setBlock(pos, state.setValue(HARVESTED, true), 3);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    protected Block getSelfDrop() {
        return ModBlocks.GRASS_WAXED.get();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(WITHERED)) {
            return List.of();
        }
        return PlantDropHelper.getDrops(params,
                !state.getValue(HARVESTED),
                getSelfDrop(), 1,
                ModItems.CUT_GRASS.get(), 1,
                ModItems.CUT_GRASS.get(), 1);
    }
}
