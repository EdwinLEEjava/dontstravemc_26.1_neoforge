package com.theconstant.dontstravemc.block.custom;

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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Natural grass block (Don't Starve style).
 * - Right-click to harvest: drops CUT_GRASS, becomes HARVESTED=true
 * - Random Tick regrowth: after some time, regrows to HARVESTED=false
 * - Shovel: drops GRASS_WAXED + CUT_GRASS (if unharvested)
 * - Non-shovel break: drops CUT_GRASS
 * - No growth limit for natural grass
 * Uses tinted_cross model.
 */
public class DsGrassBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<DsGrassBlock> CODEC = simpleCodec(DsGrassBlock::new);

    public static final BooleanProperty HARVESTED = BooleanProperty.create("harvested");

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public DsGrassBlock(BlockBehaviour.Properties properties) {
        super(properties.randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(HARVESTED, false));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HARVESTED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            boolean isHarvested = state.getValue(HARVESTED);

            if (!isHarvested) {
                // Change state to harvested
                level.setBlock(pos, state.setValue(HARVESTED, true), 3);

                // Drop cut grass
                Block.popResource(level, pos, new ItemStack(ModItems.CUT_GRASS.get(), 1));

                return InteractionResult.SUCCESS;
            }
        }

        return state.getValue(HARVESTED) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    /**
     * Random tick growth logic.
     */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HARVESTED) && level.getMaxLocalRawBrightness(pos.above()) >= 0 && random.nextInt(3) == 0) {
            this.performBonemeal(level, random, pos, state);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HARVESTED) || super.isRandomlyTicking(state);
    }

    /**
     * Bonemeal implementation (direct regrowth).
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(HARVESTED);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(HARVESTED, false), 3);
    }

    protected Block getWaxedDrop() {
        return ModBlocks.GRASS_WAXED.get();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return PlantDropHelper.getDrops(params,
                !state.getValue(HARVESTED),
                getWaxedDrop(), 1,
                ModItems.CUT_GRASS.get(), 1,
                ModItems.CUT_GRASS.get(), 1);
    }
}
