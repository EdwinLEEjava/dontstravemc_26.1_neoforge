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

/**
 * Waxed (transplanted) berry bush - variant 1.
 *
 * Lifecycle:
 * 1. Player plants -> withered=true
 * 2. Fertilize -> withered=false, stage=0, harvest_count=0, ripe=false
 * 3. Random tick -> grows to mature stage (based on harvest_count)
 * 4. Harvest -> drops 1 berry, harvest_count++, stage=0, ripe=false
 * 5. Repeat 3-4 until harvest_count >= 6
 * 6. When harvest_count >= 6 -> withered=true
 *
 * Mature stage depends on harvest_count:
 * - 0-1 harvests: stage 3
 * - 2-3 harvests: stage 2
 * - 4-5 harvests: stage 1
 *
 * Subclasses override getSelfDrop() to return their own variant block.
 */
public class WaxedBerryBushBlock extends VegetationBlock implements Fertilizable, BonemealableBlock {
    public static final MapCodec<WaxedBerryBushBlock> CODEC = simpleCodec(WaxedBerryBushBlock::new);

    public static final BooleanProperty WITHERED = BooleanProperty.create("withered");
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final IntegerProperty HARVEST_COUNT = IntegerProperty.create("harvest_count", 0, 6);
    public static final BooleanProperty RIPE = BooleanProperty.create("ripe");

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public WaxedBerryBushBlock(BlockBehaviour.Properties properties) {
        super(properties.randomTicks());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WITHERED, true)
                .setValue(STAGE, 0)
                .setValue(HARVEST_COUNT, 0)
                .setValue(RIPE, false));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WITHERED, STAGE, HARVEST_COUNT, RIPE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Returns this block itself (used in shovel drop).
     * Subclasses override to return their own variant.
     */
    protected Block getSelfDrop() {
        return ModBlocks.BERRY_BUSH_WAXED.get();
    }

    private int getMatureStage(int harvestCount) {
        if (harvestCount <= 1) return 3;
        if (harvestCount <= 3) return 2;
        return 1;
    }

    // --- Fertilizable ---

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

    // --- BonemealableBlock ---

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(WITHERED) || (!state.getValue(WITHERED) && state.getValue(STAGE) == 0);
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
                    .setValue(STAGE, 0)
                    .setValue(HARVEST_COUNT, 0)
                    .setValue(RIPE, false), 3);
        } else if (state.getValue(STAGE) == 0) {
            int matureStage = getMatureStage(state.getValue(HARVEST_COUNT));
            level.setBlock(pos, state.setValue(STAGE, matureStage).setValue(RIPE, true), 3);
        }
    }

    // --- Random tick ---

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WITHERED)) return;
        if (state.getValue(STAGE) == 0 && random.nextInt(124) == 0) {
            int matureStage = getMatureStage(state.getValue(HARVEST_COUNT));
            level.setBlock(pos, state.setValue(STAGE, matureStage).setValue(RIPE, true), 3);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return (!state.getValue(WITHERED) && state.getValue(STAGE) == 0) || super.isRandomlyTicking(state);
    }

    // --- Harvest (right-click) ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (state.getValue(WITHERED)) return InteractionResult.PASS;
            if (state.getValue(RIPE)) {
                Block.popResource(level, pos, new ItemStack(ModItems.BERRIES.get(), 1));
                int newHarvestCount = state.getValue(HARVEST_COUNT) + 1;
                if (newHarvestCount >= 6) {
                    level.setBlock(pos, state
                            .setValue(WITHERED, true)
                            .setValue(STAGE, 0)
                            .setValue(HARVEST_COUNT, 0)
                            .setValue(RIPE, false), 3);
                } else {
                    level.setBlock(pos, state
                            .setValue(STAGE, 0)
                            .setValue(HARVEST_COUNT, newHarvestCount)
                            .setValue(RIPE, false), 3);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    // --- Drops ---

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(WITHERED)) {
            return List.of(new ItemStack(ModItems.TWIGS.get(), 2));
        }
        return PlantDropHelper.getDrops(params,
                state.getValue(RIPE),
                getSelfDrop(), 1,
                ModItems.BERRIES.get(), 1,
                ModItems.TWIGS.get(), 2);
    }
}
